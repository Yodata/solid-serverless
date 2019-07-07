package io.yodata.ldp.solid.server.model.data;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;

import java.util.Optional;

public enum ActionPropertyKey {

    Id("id"),
    Type("type"),
    Object("object"),
    Agent("agent"),
    Instrument("instrument"),
    Target("target"),
    Timestamp("timestamp");

    private String id;

    ActionPropertyKey(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Optional<String> find(JsonObject o) {
        return GsonUtil.findString(o, id);
    }

}
