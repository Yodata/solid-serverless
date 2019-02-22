package io.yodata.ldp.solid.server.aws.event;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.handler.container.ContainerHandler;
import io.yodata.ldp.solid.server.aws.transform.AWSTransformService;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.model.Event.StorageAction;
import io.yodata.ldp.solid.server.model.transform.TransformMessage;
import io.yodata.ldp.solid.server.model.transform.TransformService;
import io.yodata.ldp.solid.server.subscription.pusher.LambdaPusher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GenericProcessor {

    private static final Logger log = LoggerFactory.getLogger(GenericProcessor.class);

    private Store store;
    private ContainerHandler storeHandler;
    private TransformService transform;
    private LambdaPusher pusher;

    public GenericProcessor(Store store) {
        this.store = store;
        this.storeHandler = new ContainerHandler(store);
        this.transform = new AWSTransformService();
        this.pusher = new LambdaPusher();
    }

    public void handleEvent(JsonObject event) {
        StorageAction action = GsonUtil.get().fromJson(event, StorageAction.class);
        URI id = URI.create(action.getId());
        URI target = URI.create(action.getTarget());
        log.info("Processing storage event {} about {}", action.getType(), action.getId());

        List<Subscription> subs = store.getSubscriptions(id);
        if (subs.isEmpty()) {
            log.info("No subscription found");
            return;
        }
        log.info("Processing {} subscription(s)", subs.size());

        for (Subscription sub : subs) {
            log.info("Processing subscription ID {} on target {}", sub.getId(), target.toString());

            URI subTarget = URI.create(sub.getObject());
            String host = subTarget.getHost();
            if (StringUtils.isBlank(host)) {
                log.info("No host specified");
            } else {
                if (host.startsWith("*.")) {
                    host = host.substring(1);
                    if (!target.getHost().endsWith(host)) {
                        log.info("Subscription ID {} does not match the object host pattern, skipping", sub.getId());
                        continue;
                    }

                    if (target.getHost().equalsIgnoreCase(subTarget.getHost())) {
                        log.info("Subscription target host is the same as event source host. Not allowing with a wildcard subscription");
                        continue;
                    }
                } else {
                    if (!StringUtils.equals(target.getHost(), host)) {
                        log.info("Subscription ID {} does not match the object host, skipping", sub.getId());
                        continue;
                    }
                }
            }

            if (!target.getPath().startsWith(subTarget.getPath())) {
                log.info("Subscription ID {} does not match the object, skipping", sub.getId());
                continue;
            }

            if (!action.getObject().isPresent()) {
                log.info("Object is not present in the event: Fetching data from store to process");
                Optional<String> obj = store.findEntityData(id, id.getPath());
                if (!obj.isPresent()) {
                    log.info("We got a notification about {} which doesn't exist anymore, skipping filtering", id);
                    action.setObject(new JsonObject());
                } else {
                    action.setObject(GsonUtil.parseObj(obj.get()));
                }
            }

            if (Objects.nonNull(sub.getScope())) {
                log.info("Subscription has a scope, processing");
                TransformMessage msg = new TransformMessage();
                msg.setSecurity(action.getRequest().getSecurity());
                msg.setScope(action.getRequest().getScope());
                msg.setPolicy(store.getPolicies(id));
                msg.setObject(action.getObject().get());
                JsonObject rawData = transform.transform(msg);
                if (rawData.keySet().isEmpty()) {
                    log.info("Transform removed all data, not sending notification");
                    continue;
                }

                action.setObject(rawData);
            }
            log.info("Data after scope: {}", GsonUtil.toJson(action));

            if (!StringUtils.isEmpty(sub.getAgent())) {
                log.info("Subscription is external, we'll send to outbox");

                if (!action.getObject().isPresent()) {
                    log.info("No content to send");
                    continue;
                }

                // We a Notification event
                JsonObject notification = new JsonObject();
                notification.addProperty(ActionPropertyKey.Type.getId(), "Notification");
                notification.addProperty(ActionPropertyKey.Timestamp.getId(), Instant.now().toEpochMilli());
                notification.addProperty(ActionPropertyKey.Instrument.getId(), target.resolve("/profile/card#me").toString());
                notification.addProperty(ActionPropertyKey.Target.getId(), id.toString());
                action.getObject().ifPresent(obj -> notification.add(ActionPropertyKey.Object.getId(), obj));

                // We add the audience
                notification.addProperty("@to", sub.getAgent());

                // We build the store request
                Request r = new Request();
                r.setMethod("POST");
                r.setTarget(Target.forPath(new Target(id), "/outbox/"));
                r.setBody(notification);

                // We send to store
                Response res = storeHandler.post(r);
                String eventId = GsonUtil.parseObj(res.getBody()
                        .orElse("{\"id\":\"<NOT RETURNED>\"".getBytes(StandardCharsets.UTF_8))).get("id").getAsString();
                log.info("Data was saved at {}", eventId);
            } else {
                log.info("Subscription is internal");

                JsonObject notification = new JsonObject();
                if (sub.needsContext()) {
                    log.info("Context is needed, sending full version to endpoint");
                    notification = GsonUtil.get().toJsonTree(action).getAsJsonObject();
                } else {
                    log.info("Context is not needed, sending content only");

                    if (action.getObject().isPresent()) {
                        notification = action.getObject().get();
                    } else {
                        log.warn("No content, sending empty object");
                    }
                }

                pusher.send(notification, sub.getTarget());
            }
        }

        log.info("All subscriptions processed");
    }

}
