package io.yodata.ldp.solid.server.subscription.inbox;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.Configs;
import io.yodata.ldp.solid.server.aws.handler.container.ContainerHandler;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.aws.transform.AWSTransformService;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.model.transform.TransformMessage;
import io.yodata.ldp.solid.server.model.transform.TransformService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class PublishProcessor implements Consumer<InboxService.Wrapper> {

    private final Logger log = LoggerFactory.getLogger(PublishProcessor.class);

    public static final String Type = "ReflexPublishAction";

    private final Store store;
    private final ContainerHandler containers;
    private final TransformService transform;

    public PublishProcessor() {
        this(S3Store.getDefault());
    }

    public PublishProcessor(Store store) {
        this.store = store;
        this.containers = new ContainerHandler(store);
        this.transform = new AWSTransformService();
    }

    @Override
    public void accept(InboxService.Wrapper c) {
        if (!StringUtils.equals(Type, GsonUtil.getStringOrNull(c.message, "type"))) {
            log.info("type is not {}, ignoring", Type);
            return;
        }

        Optional<JsonObject> opt = GsonUtil.findObj(c.message, "object");
        if (!opt.isPresent()) {
            log.info("No object found, ignoring");
            return;
        }

        JsonObject message = opt.get();
        List<String> topics = GsonUtil.findArrayOrString(message, "topic");
        if (topics.isEmpty()) {
            log.info("No topic found, ignoring as Topic event");
            return;
        }

        URI hostId;
        try {
            hostId = URI.create(c.ev.getRequest().getSecurity().getIdentity());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.info("Identity is not a valid URI, skipping");
            return;
        }

        String identity = c.ev.getRequest().getSecurity().getIdentity();
        log.info("Checking for permissions of {}", identity);

        Subscriptions subs = store.getSubscriptions(hostId);
        String subManager = Configs.get().find("reflex.subscription.manager.domain").orElse("");
        if (StringUtils.isNotBlank(subManager)) {
            String subManagerId = Target.forProfileCard("https://" + subManager).getId().toString();
            SubscriptionEvent.Subscription sub = new SubscriptionEvent.Subscription();
            sub.getPublishes().add("yodata/subscription#authorize");
            sub.getPublishes().add("yodata/subscription#revoke");
            subs.getItems().put(subManagerId, sub);
        }

        Optional<SubscriptionEvent.Subscription> subOpt = Optional.ofNullable(subs.getItems().get(identity));
        if (!subOpt.isPresent()) {
            log.info("No subscription(s) present at {}, skipping", hostId);
            return;
        }

        SubscriptionEvent.Subscription sub = subOpt.get();
        if (sub.getPublishes().stream().noneMatch(topics::contains)) {
            log.info("{} is not allowed to publish to any of the event topics, skipping", identity);
            return;
        }

        log.info("Normalizing event {}", c.ev.getId());
        if (Objects.isNull(c.scope) || c.scope.isEmpty()) {
            log.info("Skipping message normalization: no scope");
        } else {
            TransformMessage msg = new TransformMessage();
            msg.setSecurity(c.ev.getRequest().getSecurity());
            msg.setScope(c.scope);
            msg.setPolicy(S3Store.getDefault().getPolicies(c.ev.getRequest().getTarget().getId()));
            msg.setObject(message);
            JsonObject data = transform.transform(msg);
            if (data.keySet().isEmpty()) {
                log.info("Transform returned an empty object, skipping");
                return;
            }

            message = data;
        }

        for (String topic : topics) {
            if (!sub.getPublishes().contains(topic)) {
                // Not allowed to publish to that topic, we skip
                continue;
            }

            String topicPath = StringUtils.defaultIfBlank(topic, "");
            if (topicPath.contains(":")) {
                String[] splitValues = StringUtils.split(topicPath, ":", 2);
                topicPath = splitValues[1];
            }

            if (topicPath.contains("#")) {
                String[] splitValues = StringUtils.split(topicPath, "#", 2);
                topicPath = splitValues[0];
            }

            if (!topicPath.endsWith("/")) {
                topicPath = topicPath + "/";
            }

            Target target = Target.forPath(new Target(URI.create(c.ev.getId())), "/event/topic/" + topicPath);
            Request r = Request.post();
            r.setSecurity(c.ev.getRequest().getSecurity()); // We use the original agent and instrument
            r.setTarget(target);
            r.setBody(message);

            Response res = containers.post(r);
            String eventId = GsonUtil.parseObj(res.getBody().get()).get("id").getAsString();
            log.info("Topic event was saved at {}", eventId);
        }
    }

}
