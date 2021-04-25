package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonObject;

public class BlackholePusher implements Pusher {

    @Override
    public void send(JsonObject data, String targetRaw, JsonObject config) {
        // no-op
    }

}
