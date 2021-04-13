package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonObject;
import io.yodata.ldp.solid.server.LogAction;

public class ResponseLogAction extends LogAction {

    public static ResponseLogAction response(Response r) {
        return new ResponseLogAction().withResponse(r);
    }

    public static ResponseLogAction success(Response r) {
        ResponseLogAction a = new ResponseLogAction();
        a.withResponse(r);
        a.setSuccess(true);
        return a;
    }

    private transient Response response;

    public ResponseLogAction withResponse(Response response) {
        this.response = response;

        return this;
    }

    public Response getResponse() {
        return response;
    }

    @Override
    public ResponseLogAction addChild(JsonObject child) {
        super.addChild(child);
        return this;
    }

}
