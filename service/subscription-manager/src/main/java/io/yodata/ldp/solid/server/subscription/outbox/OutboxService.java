package io.yodata.ldp.solid.server.subscription.outbox;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.SqsPusher;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.model.event.StorageAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

public class OutboxService {

    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);

    private final CloseableHttpClient client = HttpClients.createMinimal();
    private final SqsPusher pusher = new SqsPusher();
    private final SolidServer srv;

    public OutboxService(SolidServer srv) {
        this.srv = srv;
    }

    private Optional<URI> findTarget(String recipient) {
        URI target;
        try {
            target = new URI(recipient);
            if (!Target.isProfileCard(recipient)) {
                return Optional.of(target);
            }

            if (StringUtils.equalsAny(target.getScheme(), "http", "https")) {
                target = new URIBuilder(target).setPath("/inbox/").setFragment(null).build();
                log.debug("Discovering inbox");
                URI subUri = URI.create(target.toString());
                HttpGet profileReq = new HttpGet(subUri);
                try (CloseableHttpResponse profileRes = client.execute(profileReq)) {
                    int sc = profileRes.getStatusLine().getStatusCode();
                    if (sc != 200) {
                        log.debug("No profile info. Status code: {}", sc);
                    } else {
                        try {
                            JsonObject body = GsonUtil.parseObj(profileRes.getEntity().getContent());
                            Optional<String> profileInboxUri = GsonUtil.findString(body, "inbox");
                            if (profileInboxUri.isPresent()) {
                                try {
                                    target = new URI(profileInboxUri.get());
                                    log.debug("Found advertised inbox URI: {}", target.toString());
                                } catch (URISyntaxException e) {
                                    log.debug("Invalid advertised Inbox URI: {}", profileInboxUri.get());
                                }
                            } else {
                                log.debug("No advertised Inbox URI found, using default");
                            }
                        } catch (JsonSyntaxException e) {
                            log.debug("Received data was not JSON, ignoring");
                        }
                    }
                } catch (IOException e) {
                    log.debug("Unable to discover inbox location due to I/O Error: {}", e.getMessage());
                    log.debug("Exception stacktrace", e);
                    log.debug("Using default inbox location: {}", target.toString());
                }
            }
        } catch (URISyntaxException e) {
            log.warn("Recipient {} is not a valid URI, skipping", recipient);
            return Optional.empty();
        }

        return Optional.of(target);
    }

    public OutboxSettings buildSettings(String raw) {
        Optional<JsonObject> cfgJsonOpt = GsonUtil.tryParseObj(raw);
        if (!cfgJsonOpt.isPresent()) {
            return new OutboxSettings();
        }

        // FIXME check for type and schema version before building
        JsonObject cfgJson = cfgJsonOpt.get();
        return GsonUtil.get().fromJson(cfgJson, OutboxSettings.class);
    }

    public OutboxSettings getGlobalConfig() {
        // FIXME global path should be prefixed by the store, not the caller
        return buildSettings(srv.store().getData("global/settings/outbox").orElse("{}"));
    }

    public OutboxSettings getPodConfig(URI pod) {
        return buildSettings(srv.store().findEntityData(pod, "/settings/outbox").orElse("{}"));
    }

    public boolean match(String toMatch, List<String> matches) {
        return matches.stream().anyMatch(match -> SolidServer.DomainPatternMatching.apply(toMatch, match));
    }

    public boolean isAllowedToSend(URI podId, String recipientHost) {
        OutboxSettings global = getGlobalConfig();
        OutboxSettings pod = getPodConfig(podId);

        if (global.getAuthorizedDomains().hasBlacklist()) {
            if (match(recipientHost, global.getAuthorizedDomains().getBlacklist())) {
                return false;
            }
        }

        if (pod.getAuthorizedDomains().hasBlacklist()) {
            if (match(recipientHost, pod.getAuthorizedDomains().getBlacklist())) {
                return false;
            }
        }

        if (pod.getAuthorizedDomains().hasWhitelist()) {
            return match(recipientHost, pod.getAuthorizedDomains().getWhiteList());
        }

        if (global.getAuthorizedDomains().hasWhitelist()) {
            return match(recipientHost, global.getAuthorizedDomains().getWhiteList());
        }

        return true;
    }

    public void process(JsonObject event) {
        log.debug("Processing event data: {}", GsonUtil.toJson(event));

        StorageAction action = GsonUtil.get().fromJson(event, StorageAction.class);
        URI eventId;
        try {
            eventId = URI.create(action.getId());
        } catch (IllegalArgumentException e) {
            log.error("Invalid event: {}", event);
            return;
        }

        if (!StorageAction.isAddOrUpdate(action.getType())) {
            log.debug("Event is not about new/updated data, nothing to do, skipping");
            return;
        }
        log.info("Processing {}", eventId);

        if (!action.getObject().isPresent()) {
            log.warn("Event has no data, assuming non-RDF for now and skipping");
            return;
        }

        JsonObject data = action.getObject().get();
        String recipient = GsonUtil.getStringOrNull(data, "@to");
        if (StringUtils.isBlank(recipient)) {
            log.warn("Destination is invalid, skipping - Value: {}", recipient);
            return;
        }
        data.remove("@to"); // We get rid of the special @to key for the outbox

        // We clean out a possible leftover # at the end of the URL to keep things clean
        if (recipient.endsWith("#")) {
            recipient = recipient.substring(0, recipient.length() - 1);
        }

        Optional<URI> recipientOpt = findTarget(recipient);
        if (!recipientOpt.isPresent()) {
            log.warn("Recipient {} is not a valid URI, skipping", recipient);
            return;
        }
        URI recipientUri = recipientOpt.get();
        Target recipientProfile = Target.forProfileCard(recipientUri);
        String recipientHost = recipientProfile.getHost();

        if (!isAllowedToSend(eventId, recipientHost)) {
            log.info("{} is not allowed to send to {}, skipping", eventId.getHost(), recipientHost);
            return;
        }

        String dataRaw = GsonUtil.toJson(data);
        log.debug("Push content: {}", dataRaw);

        // We are sending something internally, using the store directly
        if (srv.isServingDomain(recipientHost)) {
            log.info("Domain {} is local, bypassing API", recipientHost);

            // We build the security context to identify the sending pod
            SecurityContext sc = new SecurityContext();
            sc.setInstrument(Target.forProfileCard(action.getId()));
            sc.setAgent(sc.getInstrument());

            // We build an internal request
            Request r = Request.post().internal();
            r.setSecurity(sc);
            r.setTarget(new Target(recipientUri));
            r.setBody(data);

            // We send
            Response res = srv.post(r).getResponse();
            String messageId = res.getFileId();
            log.info("Message was saved at {}", messageId);
        } else {
            log.info("Domain {} is external, sending using pusher", recipientHost);
            pusher.send(data, recipientUri.toString(), new JsonObject());
            log.info("Outbox item was successfully sent via pusher");
        }
    }

}
