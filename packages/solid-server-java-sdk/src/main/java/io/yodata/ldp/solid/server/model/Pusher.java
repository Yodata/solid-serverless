package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonObject;

public interface Pusher {

    void send(JsonObject data, String targetRaw, JsonObject config);

}
