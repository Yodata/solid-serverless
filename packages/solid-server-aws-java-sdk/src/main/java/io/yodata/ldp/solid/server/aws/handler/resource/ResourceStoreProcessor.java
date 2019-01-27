package io.yodata.ldp.solid.server.aws.handler.resource;

import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.model.Request;
import io.yodata.ldp.solid.server.model.Response;
import io.yodata.ldp.solid.server.notification.EventBus;

public class ResourceStoreProcessor {

    private S3Store store;
    private EventBus evBus;

    public ResourceStoreProcessor(S3Store store, EventBus evBus) {
        this.store = store;
        this.evBus = evBus;
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