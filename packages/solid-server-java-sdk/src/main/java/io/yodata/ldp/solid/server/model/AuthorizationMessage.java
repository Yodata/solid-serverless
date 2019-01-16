package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonObject;

import java.util.List;

public class AuthorizationMessage {

    private String type;
    private String agent;
    private String accessTo;
    private List<String> mode;
    private JsonObject scope;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getAccessTo() {
        return accessTo;
    }

    public void setAccessTo(String accessTo) {
        this.accessTo = accessTo;
    }

    public List<String> getMode() {
        return mode;
    }

    public void setMode(List<String> mode) {
        this.mode = mode;
    }

    public JsonObject getScope() {
        return scope;
    }

    public void setScope(JsonObject scope) {
        this.scope = scope;
    }
}
