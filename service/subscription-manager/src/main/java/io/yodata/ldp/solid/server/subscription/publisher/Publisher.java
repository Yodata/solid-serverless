package io.yodata.ldp.solid.server.subscription.publisher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.handler.container.ContainerHandler;
import io.yodata.ldp.solid.server.aws.handler.resource.ResourceHandler;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.model.Event.StorageAction;
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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class Publisher {

    private static final Logger log = LoggerFactory.getLogger(Publisher.class);

    private ContainerHandler dirHandler;
    private ResourceHandler fileHandler;
    private CloseableHttpClient client = HttpClients.createMinimal();

    public Publisher(Store store) {
        this.dirHandler = new ContainerHandler(store);
        this.fileHandler = new ResourceHandler(store);
    }

    public void handle(JsonObject event) {
        StorageAction action = GsonUtil.get().fromJson(event, StorageAction.class);
        if (!StringUtils.equals(StorageAction.Add, action.getType())) {
            log.debug("Storage action is not Add, so not for us");
            return;
        }

        if (!action.getObject().isPresent()) {
            log.warn("No object provided");
            return;
        }

        URI id = URI.create(action.getId());
        publish(id, action.getObject().get());
    }

    public void publish(URI from, JsonObject message) {
        Optional<JsonObject> payloadOpt = GsonUtil.findObj(message,"payload");
        if (!payloadOpt.isPresent()) {
            log.warn("Message did not contain a payload, ignoring");
            return;
        }
        JsonObject payload = payloadOpt.get();

        Optional<JsonArray> recipientsOpt = GsonUtil.findArray(message, "recipient");
        if (!recipientsOpt.isPresent() || recipientsOpt.get().size() < 1) {
            log.warn("Message did not contain any recipient to send to, ignoring");
            return;
        }
        List<String> recipients = GsonUtil.asList(recipientsOpt.get(), String.class);

        JsonObject meta = GsonUtil.findObj(message, "meta").orElseGet(JsonObject::new);

        publish(from, recipients, meta, payload);
    }

    public void publish(URI from, List<String> recipients, JsonObject meta, JsonObject payload) {
        JsonObject notification = new JsonObject();
        notification.addProperty(ActionPropertyKey.Type.getId(), "Notification");
        notification.addProperty(ActionPropertyKey.Timestamp.getId(), Instant.now().toEpochMilli());
        notification.addProperty(ActionPropertyKey.Agent.getId(), from.resolve("/profile/card#me").toString());
        meta.entrySet().forEach(entry -> notification.add(entry.getKey(), entry.getValue()));
        notification.add(ActionPropertyKey.Object.getId(), payload);

        for (String recipient : recipients) {
            try {
                notification.addProperty("@to", recipient);
                // We build the store request
                Request r = new Request();
                r.setMethod("POST");
                r.setTarget(Target.forPath(new Target(from), "/outbox/"));
                r.setBody(notification);

                // We send to store
                Response res = dirHandler.post(r);
                String eventId = GsonUtil.parseObj(res.getBody()
                        .orElse("{\"id\":\"<NOT RETURNED>\"".getBytes(StandardCharsets.UTF_8))).get("id").getAsString();
                log.info("Data was saved at {}", eventId);

                Request d = new Request();
                d.setMethod("DELETE");
                d.setTarget(new Target(from));
                Response dRes = fileHandler.delete(d);
                log.info("{} delete status: {}", from, dRes.getStatus());
            } catch (RuntimeException e) {
                log.warn("Unable to produce notification from {} for {}", from, recipient, e);
            }
        }
    }

}
