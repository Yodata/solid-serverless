package io.yodata.ldp.solid.server.model.container;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.yodata.GsonUtil;
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
    public ResponseLogAction get(Request in) {
        ResponseLogAction result = new ResponseLogAction();
        Exchange ex = build(in);

        JsonObject inCheckStatus = inCheck.get(ex);
        result.addChild(inCheckStatus);

        if (Objects.nonNull(ex.getResponse())) {
            inCheckStatus.addProperty("type", "InputValidationProcessor");
            inCheckStatus.addProperty("hasResponse", true);
            result.withResponse(ex.getResponse());
            return result;
        }

        ex.setResponse(storeProc.get(in));

        ResponseLogAction outCheckResponse = outCheck.get(ex);
        result.addChild(outCheckResponse, outCheck);
        ex.setResponse(outCheckResponse.getResponse());

        return result.withResponse(ex.getResponse());
    }

    @Override
    public ResponseLogAction post(Request in) {
        ResponseLogAction result = new ResponseLogAction();

        ensureEncoding(in);

        Exchange ex = build(in);

        // We run any custom rules tailored for content
        JsonObject inCheckStatus = inCheck.post(ex);
        result.addChild(inCheckStatus);
        if (Objects.nonNull(ex.getResponse())) {
            inCheckStatus.addProperty("type", "InputValidationProcessor");
            inCheckStatus.addProperty("hasResponse", true);
            return result.withResponse(ex.getResponse());
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
        JsonObject storeProcResult = storeProc.post(in);
        storeProcResult.addProperty("type", storeProc.getClass().getSimpleName());
        result.addChild(storeProcResult);

        // We build the answer
        Response res = new Response();
        res.setStatus(201);
        res.getHeaders().put("Location", id);
        res.setBody(GsonUtil.makeObj("id", id));
        ex.setResponse(res);

        ResponseLogAction outCheckResult = outCheck.post(ex);
        outCheckResult.getResponse().setFileId(id);
        result.addChild(outCheckResult, outCheck);
        ex.setResponse(outCheckResult.getResponse());

        return result.withResponse(ex.getResponse());
    }

}
