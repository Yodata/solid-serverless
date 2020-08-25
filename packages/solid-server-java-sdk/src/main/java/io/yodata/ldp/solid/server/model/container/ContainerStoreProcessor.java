package io.yodata.ldp.solid.server.model.container;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.yodata.ldp.solid.server.ServerBackend;
import io.yodata.ldp.solid.server.model.Page;
import io.yodata.ldp.solid.server.model.Request;
import io.yodata.ldp.solid.server.model.Response;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ContainerStoreProcessor {

    private final ServerBackend backend;

    public ContainerStoreProcessor(ServerBackend backend) {
        this.backend = backend;
    }

    public Response get(Request request) {
        Response r = new Response();

        boolean isTemporal = StringUtils.equalsAny(request.getTarget().getPath(), "/inbox/", "/outbox/");
        boolean fullFormat = request.getSingleParameter("format")
                .map(f -> StringUtils.equals("full", f))
                // FIXME turn into configuration value
                .orElse(isTemporal);

        Page p = backend.store().getPage(
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
        backend.store().post(in);
        backend.eventBus().sendStoreEvent(in);
    }

}
