package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Subscription {

    private String id;

    private String type;

    private String version;

    // Who subscribed
    private String agent;

    // On what
    private String object;

    // If in scope
    private Object scope;

    // Send to
    private String target;

    private List<String> publishes = new ArrayList<>();

    private List<String> subscribes = new ArrayList<>();

    // Configuration
    private JsonObject config = new JsonObject();

    // If this subscription should be the only one doing the work
    private boolean isExclusive;

    private Boolean needsContext;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getObject() {
        return object;
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

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public JsonObject getConfig() {
        return config;
    }

    public void setConfig(JsonObject config) {
        this.config = config;
    }

    public List<String> getPublishes() {
        return publishes;
    }

    public void setPublishes(List<String> publishes) {
        this.publishes = publishes;
    }

    public List<String> getSubscribes() {
        return subscribes;
    }

    public void setSubscribes(List<String> subscribes) {
        this.subscribes = subscribes;
    }

    public boolean isExclusive() {
        return isExclusive;
    }

    public void setExclusive(boolean exclusive) {
        isExclusive = exclusive;
    }

    public Boolean needsContext() {
        return needsContext;
    }

    public void setNeedsContext(boolean needsContext) {
        this.needsContext = needsContext;
    }

}
