package io.yodata.ldp.solid.server.subscription.inbox;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.aws.transform.AWSTransformService;
import io.yodata.ldp.solid.server.config.Configs;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.model.transform.TransformMessage;
import io.yodata.ldp.solid.server.model.transform.TransformService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class PublishProcessor implements Consumer<InboxService.Wrapper> {

    private final Logger log = LoggerFactory.getLogger(PublishProcessor.class);

    public static final String Type = "ReflexPublishAction";

    private final SolidServer srv;
    private final TransformService transform;

    public PublishProcessor(SolidServer srv) {
        this.srv = srv;
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
        String topic = GsonUtil.findString(message, "topic").orElse("");
        if (StringUtils.isBlank(topic)) {
            log.info("No topic found, ignoring as Topic event");
            return;
        }

        URI hostId;
        try {
            hostId = URI.create(c.ev.getId());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.info("Source {} is not a valid URI, skipping", c.ev.getId());
            return;
        }

        String identity = c.ev.getRequest().getSecurity().getIdentity();
        log.info("Checking for permissions of {}", identity);

        Subscriptions subs = srv.store().getSubscriptions(hostId);
        String subManager = Configs.get().find("reflex.subscription.manager.id").orElse("");
        if (StringUtils.isNotBlank(subManager)) {
            String subManagerId = Target.forProfileCard(subManager).getId().toString();
            Subscription sub = new Subscription();
            sub.setId("subscription-manager-onthefly-add");
            sub.setAgent(subManagerId);
            sub.getPublishes().add("yodata/subscription");
            subs.getItems().add(sub);
        }

        Optional<Subscription> subOpt = Optional.ofNullable(subs.toAgentMap().get(identity));
        Subscription sub;
        if (!subOpt.isPresent()) {
            log.info("No subscription(s) present for {}, we allow per default", identity);
        } else {
            sub = subOpt.get();
            if (sub.getPublishes().stream().noneMatch(t -> Topic.matches(t, topic))) {
                log.info("{} is not allowed to publish to the topic {}, skipping", identity, topic);
                return;
            }
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

        Response res = srv.post(r);
        String eventId = GsonUtil.parseObj(res.getBody().get()).get("id").getAsString();
        log.info("Topic event was saved at {}", eventId);
    }

}
