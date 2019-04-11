package io.yodata.ldp.solid.server.subscription.inbox;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.handler.container.ContainerHandler;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.aws.transform.AWSTransformService;
import io.yodata.ldp.solid.server.model.Request;
import io.yodata.ldp.solid.server.model.Response;
import io.yodata.ldp.solid.server.model.Target;
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

    private ContainerHandler storeHandler;
    private TransformService transform;

    public PublishProcessor() {
        storeHandler = new ContainerHandler(S3Store.getDefault());
        transform = new AWSTransformService();
    }

    @Override
    public void accept(InboxService.Wrapper c) {
        if (!StringUtils.equals("ReflexPublishAction", GsonUtil.getStringOrNull(c.message, "type"))) {
            log.info("type is not REflex Publish Action, ignoring");
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
            if (!StringUtils.endsWith(topic, "/")) {
                topic = topic + "/";
            }

            Target target = Target.forPath(new Target(URI.create(c.ev.getId())), "/event/" + topic);
            Request r = new Request();
            r.setMethod("POST");
            r.setTarget(target);
            r.setSecurity(c.ev.getRequest().getSecurity()); // We use the original agent and instrument
            r.setBody(message);

            Response res = storeHandler.post(r);
            String eventId = GsonUtil.parseObj(res.getBody().get()).get("id").getAsString();
            log.info("Topic event was saved at {}", eventId);
        }
    }

}
