package io.yodata.ldp.solid.server.model.transform;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Policies {

    @SerializedName("default")
    private JsonObject def;
    private JsonObject local;
    private JsonObject global;

    public JsonObject getDef() {
        return def;
    }

    public void setDef(JsonObject def) {
        this.def = def;
    }

    public JsonObject getLocal() {
        return local;
    }

    public void setLocal(JsonObject local) {
        this.local = local;
    }

    public JsonObject getGlobal() {
        return global;
    }

    public void setGlobal(JsonObject global) {
        this.global = global;
    }

}
