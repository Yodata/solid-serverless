package io.yodata.ldp.solid.server.subscription.subscriber;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.model.Store;
import io.yodata.ldp.solid.server.model.SubscriptionEvent;
import io.yodata.ldp.solid.server.model.SubscriptionsEditor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;

public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final Store store;

    public SubscriptionService(Store store) {
        this.store = store;
    }

    public JsonObject process(JsonObject evJson) {
        try {
            SubscriptionEvent event = GsonUtil.get().fromJson(evJson, SubscriptionEvent.class);
            if (Objects.isNull(event.getData())) {
                throw new IllegalArgumentException("data has no value");
            }

            return process(event.getData());
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("JSON object is not a valid Subscription event", e);
        }
    }

    public JsonObject process(SubscriptionEvent.SubscriptionAction action) {
        if (StringUtils.isBlank(action.getAgent())) {
            throw new IllegalArgumentException("Agent is blank");
        }

        if (StringUtils.equals(action.getType(), SubscriptionEvent.SubscriptionAction.Authorize)) {
            return authorize(action);
        }

        if (StringUtils.equals(action.getType(), SubscriptionEvent.SubscriptionAction.Update)) {
            return authorize(action);
        }

        if (StringUtils.equals(action.getType(), SubscriptionEvent.SubscriptionAction.Revoke)) {
            return revoke(action);
        }

        throw new IllegalArgumentException(action.getType() + " is not a supported action type");
    }

    public JsonObject authorize(SubscriptionEvent.SubscriptionAction action) {
        URI pod = URI.create(action.getAgent());
        //log.info("Setting new permissions for {} on {}", action.getObject().getAgent(), pod.getHost());
        log.info("New permissions: {}", GsonUtil.toJson(action));
        SubscriptionsEditor subs = new SubscriptionsEditor(store.getRawSubscriptions(pod));
        log.info("Current subs: {}", GsonUtil.toJson(subs.getRaw()));
        subs.updateOrAdd(action.getObject());
        log.info("New subs: {}", GsonUtil.toJson(subs.getRaw()));
        store.setEntitySubscriptions(pod, subs.getRaw());

        return new JsonObject();
    }

    public JsonObject revoke(SubscriptionEvent.SubscriptionAction action) {
        URI pod = URI.create(action.getAgent());
        log.info("Will revoke {}", GsonUtil.toJson(action.getObject()));
        SubscriptionsEditor subs = new SubscriptionsEditor(store.getRawSubscriptions(pod));
        log.info("Current subs: {}", GsonUtil.toJson(subs.getRaw()));
        subs.remove(action.getObject());
        log.info("New subs: {}", GsonUtil.toJson(subs.getRaw()));
        store.setEntitySubscriptions(pod, subs.getRaw());

        return new JsonObject();
    }

}
