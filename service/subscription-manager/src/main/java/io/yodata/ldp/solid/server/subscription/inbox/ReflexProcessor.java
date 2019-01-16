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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.function.Consumer;

public class ReflexProcessor implements Consumer<InboxService.Wrapper> {

    private final Logger log = LoggerFactory.getLogger(ReflexProcessor.class);

    private ContainerHandler storeHandler;
    private AWSTransformService transform;

    public ReflexProcessor() {
        storeHandler = new ContainerHandler(S3Store.getDefault());
        transform = new AWSTransformService();
    }

    @Override
    public void accept(InboxService.Wrapper c) {
        log.info("Processing REflex Message {}", c.ev.getId());

        log.info("Normalizing event");
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
        data.addProperty("sameAs", c.ev.getId());

        c.message = data;

        Target target = Target.forPath(new Target(URI.create(c.ev.getId())), "/reflex/");

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
