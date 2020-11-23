package io.yodata.ldp.solid.server.subscription.publisher;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.model.event.StorageAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;

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
        PublishContext c = srv.getPublishContext(message);

        if (c.getRecipients().isEmpty()) {
            log.warn("Message did not contain any recipient to send to, ignoring");
            return;
        }

        if (!message.has(ORIGINAL_RECIPIENT)) {
            message.add(ORIGINAL_RECIPIENT, c.getRecipientJson());
        }
        message.remove("recipient");

        publish(from, c.getRecipients(), message);
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
                Request r = Request.post().internal();
                r.setTarget(Target.forPath(new Target(from), "/outbox/"));
                r.setBody(publication);

                // We send to store
                Response res = srv.post(r);
                String eventId = res.getFileId();
                log.info("Publish to {} - Data was saved at {}", recipient, eventId);
            } catch (RuntimeException e) {
                log.error("Unable to produce notification about {} for {}", from, recipient, e);
            }
        }

        Request d = new Request().internal();
        d.setMethod("DELETE");
        d.setTarget(new Target(from));
        Response dRes = srv.delete(d);
        log.info("{} delete status: {}", from, dRes.getStatus());
    }

}
