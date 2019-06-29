package io.yodata.ldp.solid.server.model.event;

import com.google.gson.JsonObject;
import io.yodata.ldp.solid.server.model.Request;

import java.util.Optional;

public class StorageAction {

    public static final String Add = "AddAction";
    public static final String Update = "UpdateAction";
    public static final String Delete = "DeleteAction";

    private String type;
    private String id;
    private String target;
    private JsonObject object;
    private Request request;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Optional<JsonObject> getObject() {
        return Optional.ofNullable(object);
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

}
