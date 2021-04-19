package io.yodata.ldp.solid.server.model.resource;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.model.Request;
import io.yodata.ldp.solid.server.model.Response;
import io.yodata.ldp.solid.server.model.ResponseLogAction;
import io.yodata.ldp.solid.server.model.ServerBackend;

public class ResourceStoreProcessor {

    private final ServerBackend backend;

    public ResourceStoreProcessor(ServerBackend backend) {
        this.backend = backend;
    }

    public Response head(Request in) {
        return backend.store().head(in.getTarget());
    }

    public Response get(Request in) {
        return backend.store().get(in.getTarget());
    }

    public ResponseLogAction put(Request in) {
        ResponseLogAction result = new ResponseLogAction();

        JsonObject backendResult = backend.store().save(in);
        result.setResult(backendResult);

        backend.eventBus().sendStoreEvent(in);

        Response out = new Response();
        out.setStatus(GsonUtil.giveBool(backendResult, "replaced", false) ? 204 : 201);
        result.withResponse(out);
        return result;
    }

    public JsonObject delete(Request in) {
        JsonObject result = backend.store().delete(in);
        backend.eventBus().sendStoreEvent(in);
        return result;
    }

}
