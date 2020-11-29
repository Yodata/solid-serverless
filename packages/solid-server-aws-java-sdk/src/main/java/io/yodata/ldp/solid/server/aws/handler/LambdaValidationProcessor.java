package io.yodata.ldp.solid.server.aws.handler;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.model.Exchange;
import io.yodata.ldp.solid.server.model.Request;
import io.yodata.ldp.solid.server.model.Store;

public class LambdaValidationProcessor {

    private final Store store;

    public LambdaValidationProcessor(Store store) {
        this.store = store;
    }

    protected JsonObject toJson(Exchange ex) {
        Request r = ex.getRequest();
        JsonObject exJson = GsonUtil.makeObj(ex);
        r.getSecurity().getAgent().ifPresent(v -> exJson.addProperty("agent", v));
        exJson.addProperty("instrument", r.getSecurity().getInstrument());
        exJson.add("scope", GsonUtil.asArray(r.getScope()));
        exJson.add("policy", GsonUtil.makeObj(store.getPolicies(r.getTarget().getId())));
        return exJson;
    }

}
