package io.yodata.ldp.solid.server.model.resource;

import io.yodata.ldp.solid.server.ServerBackend;
import io.yodata.ldp.solid.server.model.Request;
import io.yodata.ldp.solid.server.model.Response;

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

    public Response put(Request in) {
        boolean replaced = backend.store().save(in);
        backend.eventBus().sendStoreEvent(in);

        Response out = new Response();
        out.setStatus(replaced ? 204 : 201);
        return out;
    }

    public void delete(Request in) {
        backend.store().delete(in);
        backend.eventBus().sendStoreEvent(in);
    }

}
