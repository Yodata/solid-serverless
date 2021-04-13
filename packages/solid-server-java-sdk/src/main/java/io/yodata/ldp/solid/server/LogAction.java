package io.yodata.ldp.solid.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LogAction {

    public static LogAction withType() {
        return new LogAction().setType(LogAction.class.getSimpleName());
    }

    private String type;
    private String actionStatus;
    private JsonElement object;
    private String target;
    private JsonObject config;
    private JsonObject result;
    private Boolean willRetry;
    private JsonObject error;
    private List<JsonObject> children = new ArrayList<>();

    public LogAction() {
        result = new JsonObject();
    }

    public LogAction setType(String t) {
        type = t;

        return this;
    }

    public LogAction setObject(JsonElement s) {
        object = s;

        return this;
    }

    public LogAction setTarget(String s) {
        target = s;

        return this;
    }

    public LogAction setConfig(JsonObject o) {
        config = o;

        return this;
    }

    public LogAction setSuccess(boolean isSuccess) {
        actionStatus = isSuccess ? "CompletedActionStatus" : "FailedActionStatus";

        return this;
    }

    public LogAction setSuccess(boolean isSuccess, boolean willRetry) {
        setSuccess(isSuccess);
        this.willRetry = willRetry;

        return this;
    }

    public LogAction setResult(Object o) {
        setResult(GsonUtil.makeObj(o));

        return this;
    }

    public LogAction setResult(JsonObject o) {
        result = o;
        setSuccess(true);

        return this;
    }

    public LogAction setError(Throwable t) {
        error = new JsonObject();
        error.addProperty("message", t.getMessage());
        if (t instanceof RuntimeException && Objects.nonNull(t.getCause())) {
            t = t.getCause();
        }

        error.addProperty("type", t.getClass().getCanonicalName());
        error.add("stack", GsonUtil.asArray(ExceptionUtils.getStackFrames(t)));
        setSuccess(false);

        return this;
    }

    public LogAction setErrorHandled(Throwable t) {
        setError(t);
        willRetry = false;

        return this;
    }

    public LogAction success(Object o) {
        setResult(o);
        setSuccess(true);

        return this;
    }

    public LogAction addChild(LogAction child, Object type) {
        return addChild(child, type.getClass().getSimpleName());
    }

    public LogAction addChild(LogAction child, String type) {
        JsonObject childJson = GsonUtil.makeObj(child);
        childJson.addProperty("type", type);
        return addChild(childJson);
    }

    public LogAction addChild(JsonObject child) {
        children.add(child);
        return this;
    }

}
