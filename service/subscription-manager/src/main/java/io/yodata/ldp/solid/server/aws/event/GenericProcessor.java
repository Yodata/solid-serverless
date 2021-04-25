package io.yodata.ldp.solid.server.aws.event;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.Action;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.model.event.StorageAction;
import io.yodata.ldp.solid.server.model.transform.TransformMessage;
import io.yodata.ldp.solid.server.model.transform.TransformService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GenericProcessor {

    private static final Logger log = LoggerFactory.getLogger(GenericProcessor.class);

    private final SolidServer srv;
    private final TransformService transform;
    private final Pusher pusher;

    public GenericProcessor(SolidServer srv, TransformService transform, Pusher pusher) {
        this.srv = srv;
        this.transform = transform;
        this.pusher = pusher;
    }

    private Optional<Action> logOpt(Action a) {
        return log.isDebugEnabled() ? Optional.of(a) : Optional.empty();
    }

    public void handleEvent(Action resultAction, JsonObject event) {
        resultAction.withResult(new JsonObject());
        StorageAction action;

        try {
            action = GsonUtil.get().fromJson(event, StorageAction.class);
        } catch (JsonSyntaxException e) {
            resultAction.skipped("Invalid input", event).setError(e);
            return;
        }

        if (StringUtils.isBlank(action.getId())) {
            resultAction.skipped("ID not found/blank", event);
            return;
        }

        if (StringUtils.isBlank(action.getTarget())) {
            resultAction.skipped("Target not found/blank", event);
            return;
        }

        URI id;
        try {
            id = new URI(action.getId());
        } catch (URISyntaxException e) {
            resultAction.skipped("ID invalid, not a URI", action.getId()).setError(e);
            return;
        }
        resultAction.setObject(id);

        URI target;
        try {
            target = new URI(action.getTarget());
        } catch (URISyntaxException e) {
            resultAction.skipped("Target invalid, not a URI", action.getTarget()).setError(e);
            return;
        }
        log.debug("Processing storage event {} about {}", action.getType(), action.getId());

        List<Subscription> subs = srv.store().getAllSubscriptions(id);
        if (subs.isEmpty()) {
            JsonObject result = new JsonObject();
            result.addProperty("status", "done");
            result.addProperty("message", "No subscription found");
            resultAction.withResult(result);
            return;
        }
        log.debug("Processing {} subscription(s)", subs.size());

        for (Subscription sub : subs) {
            try {
                process(sub, target, event.deepCopy()).ifPresent(resultAction::addChild);
            } catch (RuntimeException e) {
                Action subAction = new Action();
                subAction.setError(e);
                subAction.withResult(GsonUtil.makeObj("subId", sub.getId()));
            }
        }

        JsonObject result = new JsonObject();
        result.addProperty("status", "done");
        resultAction.withResult(result);
    }

    public Optional<Action> process(Subscription sub, URI target, JsonObject event) {
        Action result = new Action();
        result.withResult(new JsonObject());
        result.getResult().addProperty("subId", sub.getId());
        log.debug("Processing subscription ID {} on target {}", sub.getId(), target.toString());

        if (StringUtils.isBlank(sub.getObject())) {
            return Optional.of(result.failed("Sub object is blank", GsonUtil.toJson(sub)));
        }

        URI subObj;
        try {
            subObj = URI.create(sub.getObject());
        } catch (IllegalArgumentException e) {
            return Optional.of(result.failed("Object is not a valid URI", sub.getObject()));
        }

        String host = subObj.getHost();
        if (!StringUtils.isBlank(host)) {
            if (host.startsWith("*.")) {
                host = host.substring(1);
                if (!target.getHost().endsWith(host)) {
                    return logOpt(result.skipped("Does not match the object host pattern"));
                }

                if (target.getHost().equalsIgnoreCase(subObj.getHost())) {
                    return logOpt(result.skipped("Target host is the same as event source host, not allowing with a wildcard subscription"));
                }
            } else {
                if (!StringUtils.equals(target.getHost(), host)) {
                    return logOpt(result.skipped("Not a match"));
                }
            }
        }

        if (!target.getPath().startsWith(subObj.getPath())) {
            return logOpt(result.skipped("Not a match"));
        }

        StorageAction action = GsonUtil.get().fromJson(event, StorageAction.class);
        URI id = URI.create(action.getId());

        if (!action.getObject().isPresent()) {
            log.debug("Object is not present in the event: Fetching data from store to process");
            Action child = result.addChild(new Action().withResult());

            child.setType("DownloadFromStore");
            Optional<String> obj = srv.store().findEntityData(id, id.getPath());
            if (!obj.isPresent()) {
                action.setObject(new JsonObject());
                child.skipped("Not found in store, using empty object");
            } else {
                action.setObject(GsonUtil.parseObj(obj.get()));
                child.done();
            }
        }

        Action child = result.addChild(new Action().withResult());
        child.setType("TransformForScope");
        if (Objects.nonNull(sub.getScope())) {
            log.debug("Subscription has a scope, processing");

            TransformMessage msg = new TransformMessage();
            msg.setSecurity(action.getRequest().getSecurity());
            msg.setScope(sub.getScope());
            msg.setPolicy(srv.store().getPolicies(id));
            msg.setObject(action.getObject().get());
            JsonObject rawData = transform.transform(msg);
            child.done();
            if (rawData.keySet().isEmpty()) {
                return Optional.of(result.skipped("Transform removed all data"));
            }

            action.setObject(rawData);
        } else {
            child.skipped("no scope");
        }

        log.debug("Data after scope: {}", GsonUtil.toJson(action));

        boolean isExternal = !StringUtils.isEmpty(sub.getAgent());
        result.getResult().addProperty("isExternal", isExternal);
        if (isExternal) {
            if (!action.getObject().isPresent()) {
                return Optional.of(result.skipped("No content to send"));
            }

            boolean isEvent = target.getPath().startsWith("/event/");
            result.getResult().addProperty("isEvent", isEvent);
            if (isEvent) {
                // This is the event bus container, we consider the message final
                JsonObject msg = action.getObject().get();
                msg.addProperty("@to", sub.getAgent());

                // We build the store request
                Request r = Request.post().internal();
                r.setTarget(Target.forPath(new Target(id), "/outbox/"));
                r.setBody(msg);

                // We send to store
                Response res = srv.post(r).getResponse();
                String eventId = res.getFileId();
                result.getResult().addProperty("savedAt", eventId);
            } else {
                // We rebuild the storage action to be sure only specific fields are there
                JsonObject actionNew = new JsonObject();
                actionNew.addProperty(ActionPropertyKey.Type.getId(), action.getType());
                actionNew.addProperty(ActionPropertyKey.Timestamp.getId(), Instant.now().toEpochMilli());
                actionNew.addProperty(ActionPropertyKey.Instrument.getId(), action.getRequest().getSecurity().getInstrument());
                action.getRequest().getSecurity().getAgent().ifPresent(a -> actionNew.addProperty(ActionPropertyKey.Agent.getId(), a));
                if (action.getObject().isPresent()) {
                    actionNew.add(ActionPropertyKey.Object.getId(), action.getObject().get());
                } else {
                    actionNew.addProperty(ActionPropertyKey.Object.getId(), action.getId());
                }

                // We notify about the event - Notifier will handle the wrapping and routing for us
                JsonObject publication = new JsonObject();
                publication.add("recipient", GsonUtil.asArray(sub.getAgent()));
                publication.add("payload", actionNew);

                // We build the store request
                Request r = Request.post().internal();
                r.setTarget(Target.forPath(new Target(id), "/notify/"));
                r.setBody(publication);

                // We send to store
                Response res = srv.post(r).getResponse();
                String eventId = res.getFileId();
                result.getResult().addProperty("savedAt", eventId);
            }
        } else {
            JsonObject notification = new JsonObject();
            result.getResult().addProperty("withContext", sub.needsContext());
            if (sub.needsContext()) {
                notification = GsonUtil.get().toJsonTree(action).getAsJsonObject();
            } else {
                if (action.getObject().isPresent()) {
                    notification = action.getObject().get();
                } else {
                    log.warn("No content, sending empty object");
                }
            }

            pusher.send(notification, sub.getTarget(), sub.getConfig());
            result.getResult().addProperty("sentToPusher", true);
        }

        return Optional.of(result.done());
    }

}
