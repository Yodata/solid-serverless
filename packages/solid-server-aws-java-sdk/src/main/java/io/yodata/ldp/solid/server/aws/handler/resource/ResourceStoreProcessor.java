package io.yodata.ldp.solid.server.aws.handler.resource;

import io.yodata.ldp.solid.server.model.Core;
import io.yodata.ldp.solid.server.model.data.Request;
import io.yodata.ldp.solid.server.model.data.Response;
import io.yodata.ldp.solid.server.notification.EventBus;

public class ResourceStoreProcessor {

    private Core store;
    private EventBus evBus;

    public ResourceStoreProcessor(Core store, EventBus evBus) {
        this.store = store;
        this.evBus = evBus;
    }

    public Response head(Request in) {
        return store.head(in.getTarget());
    }

    public Response get(Request in) {
        return store.get(in.getTarget());
    }

    public Response put(Request in) {
        boolean replaced = store.save(in);
        evBus.sendStoreEvent(in);

        Response out = new Response();
        out.setStatus(replaced ? 204 : 201);
        return out;
    }

    public void delete(Request in) {
        store.delete(in);
        evBus.sendStoreEvent(in);
    }

}
