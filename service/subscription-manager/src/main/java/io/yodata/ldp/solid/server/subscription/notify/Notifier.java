package io.yodata.ldp.solid.server.subscription.notify;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.model.event.StorageAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class Notifier {

    private static final Logger log = LoggerFactory.getLogger(Notifier.class);

    private final SolidServer srv;

    public Notifier(SolidServer srv) {
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
        notify(id, action.getObject().get());
    }

    public void notify(URI from, JsonObject message) {
        Optional<JsonObject> payloadOpt = GsonUtil.findObj(message, "payload");
        if (!payloadOpt.isPresent()) {
            log.warn("Message did not contain a payload, ignoring");
            return;
        }
        JsonObject payload = payloadOpt.get();

        List<String> recipients = GsonUtil.findArrayOrString(message, "recipient");
        if (recipients.isEmpty()) {
            log.warn("Message did not contain any recipient to send to, ignoring");
            return;
        }

        JsonObject meta = GsonUtil.findObj(message, "meta").orElseGet(JsonObject::new);

        notify(from, recipients, meta, payload);
    }

    public void notify(URI from, List<String> recipients, JsonObject meta, JsonObject payload) {
        JsonObject notification = new JsonObject();
        meta.entrySet().forEach(entry -> notification.add(entry.getKey(), entry.getValue()));
        notification.addProperty(ActionPropertyKey.Type.getId(), "Notification");
        notification.addProperty(ActionPropertyKey.Timestamp.getId(), Instant.now().toEpochMilli());
        notification.addProperty(ActionPropertyKey.Instrument.getId(), from.resolve("/profile/card#me").toString());
        notification.add(ActionPropertyKey.Object.getId(), payload);

        for (String recipient : recipients) {
            log.info("Send to recipient {}", recipient);

            try {
                notification.addProperty("@to", recipient);
                // We build the store request
                Request r = Request.post().internal();
                r.setTarget(Target.forPath(new Target(from), "/outbox/"));
                r.setBody(notification);

                // We send to store
                Response res = srv.post(r);
                String eventId = GsonUtil.parseObj(res.getBody()
                        .orElse("{\"id\":\"<NOT RETURNED>\"".getBytes(StandardCharsets.UTF_8))).get("id").getAsString();
                log.info("Data was saved at {}", eventId);

                Request d = new Request().internal();
                d.setMethod("DELETE");
                d.setTarget(new Target(from));
                Response dRes = srv.delete(d);
                log.info("{} delete status: {}", from, dRes.getStatus());
            } catch (RuntimeException e) {
                log.warn("Unable to produce notification from {} for {}", from, recipient, e);
            }
        }
    }

}
