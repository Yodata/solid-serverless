package io.yodata.ldp.solid.server.subscription.outbox;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.model.Event.StorageAction;
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class OutboxService {

    private final Logger log = LoggerFactory.getLogger(OutboxService.class);
    private CloseableHttpClient client = HttpClients.createMinimal();

    public void process(JsonObject event) {
        log.info("Processing event data: {}", GsonUtil.toJson(event));

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
        String subscriber = GsonUtil.getStringOrNull(data, "@to");
        if (StringUtils.isBlank(subscriber)) {
            log.warn("Destination is invalid, skipping - Value: {}", subscriber);
            return;
        }
        data.remove("@to");
        String dataRaw = GsonUtil.toJson(data);

        log.info("Push content: {}", dataRaw);

        try {
            URI inboxUri = new URIBuilder(subscriber).setPath("/inbox/").build();
            log.info("Discovery inbox");
            URI subUri = URI.create(subscriber);
            HttpGet profileReq = new HttpGet(subUri);
            try (CloseableHttpResponse profileRes = client.execute(profileReq)) {
                int sc = profileRes.getStatusLine().getStatusCode();
                if (sc != 200) {
                    log.info("No profile info. Status code: {}", sc);
                } else {
                    JsonObject body = GsonUtil.parseObj(profileRes.getEntity().getContent());
                    Optional<String> profileInboxUri = GsonUtil.findString(body, "inbox");
                    if (profileInboxUri.isPresent()) {
                        try {
                            inboxUri = new URI(profileInboxUri.get());
                            log.info("Found advertised inbox URI: {}", inboxUri.toString());
                        } catch (URISyntaxException e) {
                            log.warn("Invalid advertised Inbox URI: {}", profileInboxUri.get());
                        }
                    } else {
                        log.info("No advertised Inbox URI found, using default");
                    }
                }
            } catch (IOException e) {
                log.warn("Unable to discover inbox location due to I/O Error: {}", e.getMessage());
                log.debug("Exception stacktrace", e);
                log.warn("Using default inbox location: {}", inboxUri.toString());
            }

            HttpPost req = new HttpPost(inboxUri);
            req.setHeader("Content-Type", MimeTypes.APPLICATION_JSON);
            // FIXME need to find a good solution
            req.setHeader("X-YoData-Instrument", Target.forPath(URI.create(action.getTarget()), "/profile/card#me").getId().toString());
            req.setEntity(new StringEntity(dataRaw, StandardCharsets.UTF_8));
            try (CloseableHttpResponse res = client.execute(req)) {
                int sc = res.getStatusLine().getStatusCode();
                if (sc < 200 || sc >= 300) {
                    log.error("Unable to send notification | sc: {}", sc);
                    log.error("Error: {}", res.getEntity().getContent());
                    throw new RuntimeException("Status code when sending to " + inboxUri.toString() + ": " + sc);
                }

                log.info("Notification was successfully sent to {}", inboxUri);
            } catch (UnknownHostException e) {
                log.warn("Unable to send notification, will NOT retry: Unknown host: {}", e.getMessage());
            } catch (IOException e) {
                log.error("Unable to send notification due to I/O error, will retry", e);
                throw new RuntimeException("Unable to send notification to " + inboxUri.toString(), e);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
