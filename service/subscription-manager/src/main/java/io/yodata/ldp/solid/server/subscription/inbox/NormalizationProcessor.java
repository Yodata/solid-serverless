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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;
import java.util.function.Consumer;

public class NormalizationProcessor implements Consumer<InboxService.Wrapper> {

    private final Logger log = LoggerFactory.getLogger(NormalizationProcessor.class);

    private ContainerHandler storeHandler;
    private TransformService transform;

    public NormalizationProcessor() {
        storeHandler = new ContainerHandler(S3Store.getDefault());
        transform = new AWSTransformService();
    }

    @Override
    public void accept(InboxService.Wrapper c) {
        log.info("Normalizing event {}", c.ev.getId());
        if (Objects.isNull(c.scope) || c.scope.isEmpty()) {
            log.info("Skipping message normalization: no scope");
        } else {
            TransformMessage msg = new TransformMessage();
            msg.setSecurity(c.ev.getRequest().getSecurity());
            msg.setScope(c.scope);
            msg.setPolicy(S3Store.getDefault().getPolicies(c.ev.getRequest().getTarget().getId()));
            msg.setObject(c.message);
            JsonObject data = transform.transform(msg);
            if (data.keySet().isEmpty()) {
                log.info("Transform returned an empty object, skipping");
                return;
            }

            c.message = data;
        }

        c.message.addProperty("sameAs", c.ev.getId());

        Target target = Target.forPath(new Target(URI.create(c.ev.getId())), "/event/");
        Request r = new Request();
        r.setMethod("POST");
        r.setTarget(target);
        r.setSecurity(c.ev.getRequest().getSecurity()); // We use the original agent and instrument
        r.setBody(c.message);

        Response res = storeHandler.post(r);
        String eventId = GsonUtil.parseObj(res.getBody().get()).get("id").getAsString();
        log.info("Normalized event was saved at {}", eventId);
    }

}
