package io.yodata.ldp.solid.server.model;

public class Subscription {

    private String id;

    // Who subscribed
    private String agent;

    // On what
    private String object;

    // If in scope
    private Object scope;

    // Send to
    private String target;

    // If this subscription should be the only one doing the work
    private boolean isExclusive;

    private boolean needsContext;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isExclusive() {
        return isExclusive;
    }

    public void setExclusive(boolean exclusive) {
        isExclusive = exclusive;
    }

    public boolean needsContext() {
        return needsContext;
    }

    public void setNeedsContext(boolean needsContext) {
        this.needsContext = needsContext;
    }

}
