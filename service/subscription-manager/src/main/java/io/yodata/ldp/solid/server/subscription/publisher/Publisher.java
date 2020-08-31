package io.yodata.ldp.solid.server.subscription.publisher;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.model.Request;
import io.yodata.ldp.solid.server.model.Response;
import io.yodata.ldp.solid.server.model.SolidServer;
import io.yodata.ldp.solid.server.model.Target;
import io.yodata.ldp.solid.server.model.event.StorageAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Publisher {

    public static final String ORIGINAL_RECIPIENT = "originalRecipient";

    private static final Logger log = LoggerFactory.getLogger(Publisher.class);

    private final SolidServer srv;

    public Publisher(SolidServer srv) {
        this.srv = srv;
    }

    public void handle(JsonObject event) {
        StorageAction action = GsonUtil.get().fromJson(event, StorageAction.class);
        if (!StorageAction.isAddOrUpdate(action.getType())) {
            log.debug("Storage action is not Add or Update, so not for us");
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

        Set<String> recipients = new HashSet<>();
        if (rRaw.isJsonArray()) {
            recipients.addAll(GsonUtil.asList(rRaw.getAsJsonArray(), String.class));
        }
        if (rRaw.isJsonPrimitive()) {
            recipients.add(rRaw.getAsJsonPrimitive().getAsString());
        }

        GsonUtil.findElement(message, "source").ifPresent(sEl -> {
            if (sEl.isJsonArray()) {
                recipients.addAll(GsonUtil.asList(sEl.getAsJsonArray(), String.class));
            }
            if (sEl.isJsonPrimitive()) {
                recipients.add(sEl.getAsJsonPrimitive().getAsString());
            }
        });

        if (recipients.isEmpty()) {
            log.warn("Message did not contain any recipient to send to, ignoring");
            return;
        }

        if (!message.has(ORIGINAL_RECIPIENT)) {
            message.add(ORIGINAL_RECIPIENT, rRaw);
        }
        message.remove("recipient");

        publish(from, recipients, message);
    }

    public void publish(URI from, Collection<String> recipients, JsonObject message) {
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
                Response res = srv.post(r);
                String eventId = GsonUtil.parseObj(res.getBody()
                        .orElse("{\"id\":\"<NOT RETURNED>\"}".getBytes(StandardCharsets.UTF_8))).get("id").getAsString();
                log.info("Publish to {} - Data was saved at {}", recipient, eventId);
            } catch (RuntimeException e) {
                log.error("Unable to produce notification about {} for {}", from, recipient, e);
            }
        }

        Request d = new Request();
        d.setMethod("DELETE");
        d.setTarget(new Target(from));
        Response dRes = srv.delete(d);
        log.info("{} delete status: {}", from, dRes.getStatus());
    }

}
