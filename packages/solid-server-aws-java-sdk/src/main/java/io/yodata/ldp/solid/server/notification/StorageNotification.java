package io.yodata.ldp.solid.server.notification;

import io.yodata.ldp.solid.server.model.Request;

import java.net.URI;

public class StorageNotification {

    private String type = "StorageNotification";
    private String object;
    private Object scope;
    private Request request;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObject() {
        return object;
    }

    public void setObject(URI object) {
        setObject(object.toString());
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Object getScope() {
        return scope;
    }

    public void setScope(Object scope) {
        this.scope = scope;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

}
