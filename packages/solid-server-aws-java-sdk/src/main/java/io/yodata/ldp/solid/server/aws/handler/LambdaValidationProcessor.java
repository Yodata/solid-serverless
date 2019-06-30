package io.yodata.ldp.solid.server.aws.handler;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.store.S3Core;
import io.yodata.ldp.solid.server.model.Exchange;
import io.yodata.ldp.solid.server.model.Request;

public class LambdaValidationProcessor {

    protected JsonObject toJson(Exchange ex) {
        Request r = ex.getRequest();
        JsonObject exJson = GsonUtil.makeObj(ex);
        r.getSecurity().getAgent().ifPresent(v -> exJson.addProperty("agent", v));
        exJson.addProperty("instrument", r.getSecurity().getInstrument());
        exJson.add("scope", GsonUtil.asArray(r.getScope()));
        exJson.add("policy", GsonUtil.makeObj(S3Core.getDefault().getPolicies(r.getTarget().getId())));
        return exJson;
    }

}
