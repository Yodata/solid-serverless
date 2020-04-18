package io.yodata.ldp.solid.server;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class LogAction {

    private String actionStatus;
    private String object;
    private String target;
    private JsonObject config;
    private JsonObject result;
    private JsonObject error;

    public LogAction() {
        result = new JsonObject();
    }

    public LogAction setObject(String s) {
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
        error = GsonUtil.makeObj("stacktrace", ExceptionUtils.getStackTrace(t));
        setSuccess(false);

        return this;
    }

}
