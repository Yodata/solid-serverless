package io.yodata.ldp.solid.server.model.container;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.ServerBackend;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.model.processor.InputValidationProcessor;
import io.yodata.ldp.solid.server.model.processor.OutputValidationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ContainerHandler extends GenericHandler {

    private static final Logger log = LoggerFactory.getLogger(ContainerHandler.class);

    private final InputValidationProcessor inCheck;
    private final OutputValidationProcessor outCheck;
    private final ContainerStoreProcessor storeProc;

    public ContainerHandler(ServerBackend backend) {
        super(backend.store());
        this.storeProc = new ContainerStoreProcessor(backend);
        inCheck = backend.inValProc();
        outCheck = backend.outValProc();
    }

    @Override
    public Response get(Request in) {
        Exchange ex = build(in);

        inCheck.get(ex);
        if (Objects.nonNull(ex.getResponse())) {
            return ex.getResponse();
        }

        ex.setResponse(storeProc.get(in));
        return outCheck.get(ex);
    }

    @Override
    public Response post(Request in) {
        ensureEncoding(in);

        Exchange ex = build(in);

        // We run any custom rules tailored for content
        inCheck.post(ex);
        if (Objects.nonNull(ex.getResponse())) {
            return ex.getResponse();
        }

        // We ensure we use the latest middleware data
        in = ex.getRequest();

        // We produce a unique ID for this message
        String uuid = UUID.randomUUID().toString().replace("-", "");
        log.debug("Generated internal ID: {}", uuid);
        String id = in.getTarget().getId().toString() + uuid;
        log.debug("Data ID: {}", id);// We update the request with the relevant data to store
        in.setDestination(new Target(URI.create(id)));

        addIdIfPossible(in, id);
        addKeyIfPossible(in, false, "id", id);

        Map<String, JsonElement> keys = new HashMap<>();
        if (in.getTarget().getPath().startsWith("/inbox/")) {
            if (!in.getSecurity().isAnonymous()) {
                keys.put("agent", new JsonPrimitive(in.getSecurity().getIdentity()));
            }
        }
        if (in.getTarget().getPath().startsWith("/publish/")) {
            Instant ts = Instant.now();
            keys.put(ActionPropertyKey.Timestamp.getId(), new JsonPrimitive(ts.toEpochMilli()));
            keys.put("time", new JsonPrimitive(ts.toString()));
        }
        addKeysIfPossible(in, keys);

        // We store
        storeProc.post(in);

        // We build the answer
        Response res = new Response();
        res.setStatus(201);
        res.getHeaders().put("Location", id);
        res.setBody(GsonUtil.makeObj("id", id));
        ex.setResponse(res);

        return outCheck.post(ex);
    }

}
