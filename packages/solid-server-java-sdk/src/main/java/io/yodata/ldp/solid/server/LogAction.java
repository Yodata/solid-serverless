package io.yodata.ldp.solid.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Objects;

public class LogAction {

    private String actionStatus;
    private JsonElement object;
    private String target;
    private JsonObject config;
    private JsonObject result;
    private Boolean willRetry;
    private JsonObject error;

    public LogAction() {
        result = new JsonObject();
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

}
