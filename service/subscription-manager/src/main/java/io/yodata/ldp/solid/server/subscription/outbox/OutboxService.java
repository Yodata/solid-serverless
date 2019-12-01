package io.yodata.ldp.solid.server.subscription.outbox;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.model.event.StorageAction;
import io.yodata.ldp.solid.server.model.Target;
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

    private Optional<URI> findTarget(String recipient) {
        URI target;
        try {
            target = new URI(recipient);
            if (!StringUtils.endsWithAny(recipient, "/profile/card", "/profile/card#me")) {
                return Optional.of(target);
            }

            if (StringUtils.equalsAny(target.getScheme(), "http", "https")) {
                target = new URIBuilder(target).setPath("/inbox/").setFragment("").build();
                /*
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
                 */
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
        if (!StringUtils.equals(StorageAction.Add, action.getType())) {
            log.warn("Event is not about adding data, not supported for now, skipping");
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
        data.remove("@to");

        if (recipient.endsWith("#")) {
            recipient = recipient.substring(0, recipient.length() - 1);
        }

        Optional<URI> targetOpt = findTarget(recipient);
        if (!targetOpt.isPresent()) {
            log.warn("Recipient {} is not a valid URI, skipping", recipient);
            return;
        }
        URI target = targetOpt.get();

        String dataRaw = GsonUtil.toJson(data);

        log.debug("Push content: {}", dataRaw);

        HttpPost req = new HttpPost(target);
        req.setHeader("Content-Type", MimeTypes.APPLICATION_JSON);
        // FIXME need to find a good solution
        req.setHeader("X-YoData-Instrument", Target.forPath(URI.create(action.getTarget()), "/profile/card#me").getId().toString());
        req.setEntity(new StringEntity(dataRaw, StandardCharsets.UTF_8));
        try (CloseableHttpResponse res = client.execute(req)) {
            int sc = res.getStatusLine().getStatusCode();
            if (sc < 200 || sc >= 300) {
                log.error("Unable to send notification | sc: {}", sc);
                log.error("Error: {}", res.getEntity().getContent());
                throw new RuntimeException("Status code when sending to " + target + ": " + sc);
            }

            log.info("Outbox item was successfully sent to {}", target);
        } catch (SSLPeerUnverifiedException e) {
            log.warn("Unable to send outbox item, will NOT retry: {}", e.getMessage());
        } catch (UnknownHostException e) {
            log.warn("Unable to send outbox item, will NOT retry: Unknown host: {}", e.getMessage());
        } catch (IOException e) {
            log.error("Unable to send outbox item due to I/O error, will retry", e);
            throw new RuntimeException("Unable to send notification to " + target, e);
        }
    }

}
