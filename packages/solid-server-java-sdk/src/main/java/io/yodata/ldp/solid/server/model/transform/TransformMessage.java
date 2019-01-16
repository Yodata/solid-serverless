package io.yodata.ldp.solid.server.model.transform;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.yodata.ldp.solid.server.model.SecurityContext;

import java.util.Objects;

public class TransformMessage {

    private String agent;
    private String instrument;
    private Policies policy = new Policies();
    private JsonElement scope;
    private JsonObject object;

    public void setSecurity(SecurityContext sc) {
        setInstrument(sc.getInstrument());
        sc.getAgent().ifPresent(this::setAgent);
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public Policies getPolicy() {
        return policy;
    }

    public void setPolicy(Policies policy) {
        this.policy = policy;
    }

    public JsonElement getScope() {
        return scope;
    }

    public void setScope(JsonElement scope) {
        this.scope = scope;
    }

    public JsonObject getObject() {
        return object;
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

}
