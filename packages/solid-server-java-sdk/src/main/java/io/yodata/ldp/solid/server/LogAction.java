package io.yodata.ldp.solid.server;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class LogAction {

    private String actionStatus;
    private JsonObject object;
    private JsonObject result;
    private JsonObject error;

    public LogAction() {
        object = new JsonObject();
        result = new JsonObject();
    }

    public LogAction setObject(JsonObject o) {
        object = o;

        return this;
    }

    public LogAction setSuccess(boolean isSuccess) {
        actionStatus = isSuccess ? "CompletedActionStatus" : "FailedActionStatus";

        return this;
    }

    public LogAction setResult(Object o) {
        setObject(GsonUtil.makeObj(o));

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
