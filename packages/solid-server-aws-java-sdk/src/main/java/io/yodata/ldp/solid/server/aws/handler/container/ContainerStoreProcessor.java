package io.yodata.ldp.solid.server.aws.handler.container;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.yodata.ldp.solid.server.model.Core;
import io.yodata.ldp.solid.server.model.data.Page;
import io.yodata.ldp.solid.server.model.data.Request;
import io.yodata.ldp.solid.server.model.data.Response;
import io.yodata.ldp.solid.server.notification.EventBus;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ContainerStoreProcessor {

    protected Core store;
    private EventBus evBus;

    public ContainerStoreProcessor(Core store, EventBus evBus) {
        this.store = store;
        this.evBus = evBus;
    }

    public Response get(Request request) {
        Response r = new Response();

        boolean isTemporal = StringUtils.equalsAny(request.getTarget().getPath(), "/inbox/", "/outbox/");
        boolean fullFormat = request.getSingleParameter("format")
                .map(f -> StringUtils.equals("full", f))
                // FIXME turn into configuration value
                .orElse(isTemporal);

        Page p = store.getPage(
                request.getTarget(),
                request.getSingleParameter("by").orElse("token"),
                request.getSingleParameter("from").orElse(""),
                fullFormat,
                isTemporal
        );

        if (!fullFormat) {
            List<JsonElement> l = new ArrayList<>();
            p.getContains().forEach(el -> {
                l.add(new JsonPrimitive(request.getTarget().getId().toString() + el.getAsString()));
            });
            p.setContains(l);
        }

        r.setJsonBody(p);
        return r;
    }

    public void post(Request in) {
        store.post(in);
        evBus.sendStoreEvent(in);
    }

}
