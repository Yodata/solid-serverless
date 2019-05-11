package io.yodata.ldp.solid.server.aws.handler.resource;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.handler.GenericHandler;
import io.yodata.ldp.solid.server.aws.handler.RequestCheckProcessor;
import io.yodata.ldp.solid.server.aws.handler.ResponseCheckProcessor;
import io.yodata.ldp.solid.server.aws.handler.resource.input.ResourceRequestCheckProcessor;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.notification.EventBus;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ResourceHandler extends GenericHandler {

    private RequestCheckProcessor inCheck;
    private ResponseCheckProcessor outCheck;
    private ResourceStoreProcessor storeProc;

    public ResourceHandler() {
        this(S3Store.getDefault());
    }

    public ResourceHandler(Store store) {
        super(store);
        this.inCheck = new ResourceRequestCheckProcessor();
        this.outCheck = new ResponseCheckProcessor();
        this.storeProc = new ResourceStoreProcessor(store, new EventBus());
    }

    @Override
    public Response get(Request in) {
        Exchange ex = build(in);

        inCheck.get(ex);
        if (Objects.nonNull(ex.getResponse())) {
            return ex.getResponse();
        }

        Request inProcessed = ex.getRequest();
        ex.setResponse(storeProc.get(inProcessed));

        // Temp solution about custom ACL format
        if (inProcessed.getTarget().getPath().endsWith(".acl")) {
            String target = StringUtils.removeEndIgnoreCase(inProcessed.getTarget().getId().toString(), ".acl");
            ex.getResponse().getBody().ifPresent(aclJson -> {
                Acl acl = GsonUtil.parse(aclJson, Acl.class);
                Map<String, JsonObject> entities = new HashMap<>();

                URI pod = inProcessed.getTarget().getId();
                for (Map.Entry<String, Acl.Entry> pattern : acl.getPatterns().entrySet()) {
                    String baseUrl = pod.getScheme() + "://" + pod.getHost() + (pod.getPort() != -1 ? pod.getPort() : "");
                    String resolvedPattern = pattern.getKey().replace("%BASE_URL%", baseUrl);
                    JsonObject entryView = new JsonObject();
                    entryView.addProperty("type", "Authorization");
                    entryView.addProperty("agent", resolvedPattern);
                    entryView.addProperty("accessTo", target);
                    entryView.add("mode", GsonUtil.asStringArray(pattern.getValue().getModes()));
                    entryView.add("scope", GsonUtil.asStringArray(pattern.getValue().getScope()));
                    entities.put("#" + resolvedPattern, entryView);
                }
                acl.getEntities().forEach((entity, entry) -> {
                    JsonObject entryView = new JsonObject();
                    entryView.addProperty("type", "Authorization");
                    entryView.addProperty("agent", entity);
                    entryView.addProperty("accessTo", target);
                    entryView.add("mode", GsonUtil.asStringArray(entry.getModes()));
                    entryView.add("scope", GsonUtil.asStringArray(entry.getScope()));
                    entities.put("#" + entity, entryView);
                });

                ex.getResponse().setJsonBody(entities);
            });
        }

        return outCheck.get(ex);
    }

    @Override
    public Response put(Request in) {
        Exchange ex = build(in);

        inCheck.put(ex);
        if (Objects.nonNull(ex.getResponse())) {
            return ex.getResponse();
        }

        addIdIfPossible(in, in.getTarget().getId().toString());

        ex.setResponse(storeProc.put(in));
        ex.setResponse(outCheck.put(ex));
        return ex.getResponse();
    }

    @Override
    public Response delete(Request in) {
        Exchange ex = build(in);

        inCheck.delete(ex);
        if (Objects.nonNull(ex.getResponse())) {
            return ex.getResponse();
        }

        storeProc.delete(in);
        ex.setResponse(Response.successful());
        ex.setResponse(outCheck.delete(ex));

        return ex.getResponse();
    }

}
