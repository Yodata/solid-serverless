package io.yodata.ldp.solid.server;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Action {

    private String type;
    private String object;
    private List<Action> child;
    private JsonObject result;
    private JsonObject error;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public void setObject(URI object) {
        setObject(object.toString());
    }

    public List<Action> getChild() {
        return child;
    }

    public void setChild(List<Action> child) {
        this.child = child;
    }


    public Action addChild(Action child) {
        if (Objects.isNull(this.child)) {
            this.child = new ArrayList<>();
        }

        this.child.add(child);
        return child;
    }

    public JsonObject getResult() {
        return result;
    }

    public void setResult(JsonObject result) {
        this.result = result;
    }

    public Action withResult() {
        return withResult(new JsonObject());
    }

    public Action withResult(JsonObject result) {
        setResult(result);
        return this;
    }

    public Action setError(Throwable t) {
        error = new JsonObject();
        error.addProperty("message", t.getMessage());
        if (t instanceof RuntimeException && Objects.nonNull(t.getCause())) {
            t = t.getCause();
        }

        error.addProperty("type", t.getClass().getCanonicalName());
        error.add("stack", GsonUtil.asArray(ExceptionUtils.getStackFrames(t)));

        return this;
    }

    public Action setError(String message, Object object) {
        error = new JsonObject();
        error.addProperty("message", message);
        error.addProperty("object", GsonUtil.toJson(object));

        return this;
    }

    public boolean isSuccessful() {
        return !Objects.isNull(error);
    }

    public Action withStatus(String status) {
        getResult().addProperty("status", status);

        return this;
    }

    public Action done() {
        withStatus("done");

        return this;
    }

    public Action skipped() {
        withStatus("skipped");

        return this;
    }

    public Action failed() {
        withStatus("failed");

        return this;
    }

    public Action failed(String reason) {
        failed();
        getResult().addProperty("reason", reason);
        getResult().remove("object");

        return this;
    }

    public Action failed(String reason, Object object) {
        failed(reason);
        getResult().addProperty("object", GsonUtil.toJson(object));

        return this;
    }

    public Action skipped(String reason) {
        skipped();
        getResult().addProperty("reason", reason);
        getResult().remove("object");

        return this;
    }

    public Action skipped(String reason, JsonObject data) {
        skipped(reason);
        getResult().add("object", data);

        return this;
    }

    public Action skipped(String reason, String data) {
        skipped(reason);
        getResult().addProperty("object", data);

        return this;
    }

}
