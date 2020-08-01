package io.yodata.ldp.solid.server.subscription.outbox;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.aws.Configs;
import io.yodata.ldp.solid.server.aws.handler.container.ContainerHandler;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.model.Request;
import io.yodata.ldp.solid.server.model.Response;
import io.yodata.ldp.solid.server.model.SecurityContext;
import io.yodata.ldp.solid.server.model.Target;
import io.yodata.ldp.solid.server.model.event.StorageAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class OutboxService {

    private final Logger log = LoggerFactory.getLogger(OutboxService.class);
    private CloseableHttpClient client = HttpClients.createMinimal();
    private final ContainerHandler containers = new ContainerHandler(S3Store.getDefault());

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
        log.info("Processing event data: {}", GsonUtil.toJson(event));

        StorageAction action = GsonUtil.get().fromJson(event, StorageAction.class);
        if (!StorageAction.isAddOrUpdate(action.getType())) {
            log.warn("Event is not about new/updated data, nothing to do, skipping");
            return;
        }

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
            Response res = containers.post(r);
            String eventId = GsonUtil.parseObj(res.getBody().get()).get("id").getAsString();
            log.info("Message was saved at {}", eventId);
        } else {
            log.info("Domain {} is external, sending regular HTTP", recipientHost);

            // FIXME move this to pusher
            HttpPost req = new HttpPost(recipientUri);
            req.setHeader("Content-Type", MimeTypes.APPLICATION_JSON);
            req.setEntity(new StringEntity(dataRaw, StandardCharsets.UTF_8));
            try (CloseableHttpResponse res = client.execute(req)) {
                int sc = res.getStatusLine().getStatusCode();
                if (sc < 200 || sc >= 300) {
                    log.error("Unable to send notification | sc: {}", sc);
                    log.error("Error: {}", res.getEntity().getContent());
                    throw new RuntimeException("Status code when sending to " + recipientUri + ": " + sc);
                }

                log.info("Outbox item was successfully sent to {}", recipientUri);
            } catch (SSLPeerUnverifiedException e) {
                log.warn("Unable to send outbox item, will NOT retry: {}", e.getMessage());
            } catch (UnknownHostException e) {
                log.warn("Unable to send outbox item, will NOT retry: Unknown host: {}", e.getMessage());
            } catch (IOException e) {
                log.error("Unable to send outbox item due to I/O error, will retry", e);
                throw new RuntimeException("Unable to send notification to " + recipientUri, e);
            }
        }
    }

}
