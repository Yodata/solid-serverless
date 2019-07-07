package io.yodata.ldp.solid.server.subscription.publisher;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.handler.container.ContainerHandler;
import io.yodata.ldp.solid.server.aws.handler.resource.ResourceHandler;
import io.yodata.ldp.solid.server.model.Core;
import io.yodata.ldp.solid.server.model.data.Request;
import io.yodata.ldp.solid.server.model.data.Response;
import io.yodata.ldp.solid.server.model.data.Target;
import io.yodata.ldp.solid.server.model.event.StorageAction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Publisher {

    private static final Logger log = LoggerFactory.getLogger(Publisher.class);

    private ContainerHandler dirHandler;
    private ResourceHandler fileHandler;

    public Publisher(Core store) {
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
        Optional<JsonElement> rOpt = GsonUtil.findElement(message, "recipient");
        if (!rOpt.isPresent()) {
            log.warn("Message did not contain any recipient to send to, ignoring");
            return;
        }
        JsonElement rRaw = rOpt.get();

        List<String> recipients = new ArrayList<>();
        if (rRaw.isJsonArray()) {
            recipients.addAll(GsonUtil.asList(rRaw.getAsJsonArray(), String.class));
        }
        if (rRaw.isJsonPrimitive()) {
            recipients.add(rRaw.getAsJsonPrimitive().getAsString());
        }
        if (recipients.isEmpty()) {
            log.warn("Message did not contain any recipient to send to, ignoring");
            return;
        }
        message.remove("recipient");

        publish(from, recipients, message);
    }

    public void publish(URI from, List<String> recipients, JsonObject message) {
        log.info("From: {}", from.toString());
        message.addProperty("instrument", from.resolve("/profile/card#me").toString());

        JsonObject publication = new JsonObject();
        publication.addProperty("type", "ReflexPublishAction");
        publication.add("object", message);

        for (String recipient : recipients) {
            try {
                publication.addProperty("@to", recipient);
                // We build the store request
                Request r = new Request();
                r.setMethod("POST");
                r.setTarget(Target.forPath(new Target(from), "/outbox/"));
                r.setBody(publication);

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
