package io.yodata.ldp.solid.server.subscription.outbox;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.model.Event.StorageAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import java.nio.charset.StandardCharsets;

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
            // FIXME we should auto-discover the inbox instead of hardcoding
            URI id = new URIBuilder(URI.create(subscriber)).setPath("/inbox/").build();
            HttpPost req = new HttpPost(id);
            req.setHeader("Content-Type", MimeTypes.APPLICATION_JSON);
            req.setHeader("X-API-Key", "yodata-reflex"); // FIXME need to find a good solution
            req.setEntity(new StringEntity(dataRaw, StandardCharsets.UTF_8));
            try (CloseableHttpResponse res = client.execute(req)) {
                int sc = res.getStatusLine().getStatusCode();
                if (sc < 200 || sc >= 300) {
                    log.error("Unable to send notification | sc: {}", sc);
                    JsonObject error = GsonUtil.parseObj(res.getEntity().getContent());
                    System.out.println(GsonUtil.getPretty().toJson(error));
                    throw new RuntimeException("Status code when sending to " + id.toString() + ": " + sc);
                }

                log.info("Notification was successfully sent");
            } catch (IOException e) {
                log.error("Unable to send notification due to I/O error", e);
                throw new RuntimeException("Unable to send notification to " + id.toString(), e);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
