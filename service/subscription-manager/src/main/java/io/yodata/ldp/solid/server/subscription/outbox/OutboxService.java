package io.yodata.ldp.solid.server.subscription.outbox;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.Configs;
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
            if (!StringUtils.endsWithAny(recipient, "/profile/card", "/profile/card#me")) {
                return Optional.of(target);
            }

            if (StringUtils.equalsAny(target.getScheme(), "http", "https")) {
                target = new URIBuilder(target).setPath("/inbox/").setFragment(null).build();
                log.info("Discovering inbox");
                URI subUri = URI.create(target.toString());
                HttpGet profileReq = new HttpGet(subUri);
                try (CloseableHttpResponse profileRes = client.execute(profileReq)) {
                    int sc = profileRes.getStatusLine().getStatusCode();
                    if (sc != 200) {
                        log.info("No profile info. Status code: {}", sc);
                    } else {
                        try {
                            JsonObject body = GsonUtil.parseObj(profileRes.getEntity().getContent());
                            Optional<String> profileInboxUri = GsonUtil.findString(body, "inbox");
                            if (profileInboxUri.isPresent()) {
                                try {
                                    target = new URI(profileInboxUri.get());
                                    log.info("Found advertised inbox URI: {}", target.toString());
                                } catch (URISyntaxException e) {
                                    log.warn("Invalid advertised Inbox URI: {}", profileInboxUri.get());
                                }
                            } else {
                                log.info("No advertised Inbox URI found, using default");
                            }
                        } catch (JsonSyntaxException e) {
                            log.info("Received data was not JSON, ignoring");
                        }
                    }
                } catch (IOException e) {
                    log.warn("Unable to discover inbox location due to I/O Error: {}", e.getMessage());
                    log.debug("Exception stacktrace", e);
                    log.warn("Using default inbox location: {}", target.toString());
                }
            }
        } catch (URISyntaxException e) {
            log.warn("Recipient {} is not a valid URI, skipping", recipient);
            return Optional.empty();
        }

        return Optional.of(target);
    }

    public void process(JsonObject event) {
        log.debug("Processing event data: {}", GsonUtil.toJson(event));

        StorageAction action = GsonUtil.get().fromJson(event, StorageAction.class);
        if (!StorageAction.isAddOrUpdate(action.getType())) {
            log.warn("Event is not about new/updated data, nothing to do, skipping");
            return;
        }
        log.info("Processing {}", action.getId());

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

        String dataRaw = GsonUtil.toJson(data);

        log.debug("Push content: {}", dataRaw);
        Target recipientProfile = Target.forProfileCard(recipientUri);
        String recipientHost = recipientProfile.getHost();

        String baseDomain = StringUtils.defaultIfBlank(Configs.get().get("reflex.domain.base"), "");
        boolean isLocal = StringUtils.equalsIgnoreCase(recipientHost, baseDomain) || StringUtils.endsWithIgnoreCase(recipientHost, "." + baseDomain);

        // We are sending something internally, using the store directly
        if (StringUtils.isNotBlank(baseDomain) && isLocal) {
            log.info("Domain {} is local, bypassing API", recipientHost);

            // We build the security context to identify the sending pod
            SecurityContext sc = new SecurityContext();
            sc.setInstrument(Target.forProfileCard(action.getId()));
            sc.setAgent(sc.getInstrument());

            // We build an internal request
            Request r = Request.post();
            r.setSecurity(sc);
            r.setTarget(new Target(recipientUri));
            r.setBody(data);

            // We send
            Response res = srv.post(r);
            String eventId = GsonUtil.parseObj(res.getBody().orElseGet("{}"::getBytes)).get("id").getAsString();
            log.info("Message was saved at {}", eventId);
        } else {
            log.info("Domain {} is external, sending using pusher", recipientHost);
            pusher.send(data, recipientUri.toString(), new JsonObject());
            log.info("Outbox item was successfully sent via pusher");
        }
    }

}
