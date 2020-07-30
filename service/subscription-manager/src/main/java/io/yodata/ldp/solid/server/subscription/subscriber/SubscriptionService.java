package io.yodata.ldp.solid.server.subscription.subscriber;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.model.Store;
import io.yodata.ldp.solid.server.model.SubscriptionEvent;
import io.yodata.ldp.solid.server.model.Subscriptions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;

public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private Store store;

    public SubscriptionService(Store store) {
        this.store = store;
    }

    public SubscriptionService() {
        this(S3Store.getDefault());
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

        if (StringUtils.equals(action.getType(), SubscriptionEvent.SubscriptionAction.Revoke)) {
            return revoke(action);
        }

        throw new IllegalArgumentException(action.getType() + " is not a supported action type");
    }

    public JsonObject authorize(SubscriptionEvent.SubscriptionAction action) {
        URI pod = URI.create(action.getAgent());
        log.info("Setting new permissions for {} on {}", action.getObject().getAgent(), pod.getHost());
        log.info("New permissions: {}", GsonUtil.toJson(action));
        Subscriptions subs = store.getSubscriptions(pod);
        log.info("Current subs: {}", GsonUtil.toJson(subs));
        subs.getItems().put(action.getObject().getAgent(), action.getObject());
        log.info("New subs: {}", GsonUtil.toJson(subs));
        JsonObject newSubs = subs.toJson();
        log.info("New subs file: {}", GsonUtil.toJson(newSubs));
        store.setEntitySubscriptions(pod, newSubs);

        return new JsonObject();
    }

    public JsonObject revoke(SubscriptionEvent.SubscriptionAction action) {
        URI pod = URI.create(action.getAgent());
        log.info("Revoking permissions for {} on {}", action.getObject().getAgent(), pod.getHost());
        Subscriptions subs = store.getSubscriptions(pod);
        subs.getItems().remove(action.getObject().getAgent());
        store.setEntitySubscriptions(pod, subs.toJson());

        return new JsonObject();
    }

}
