package io.yodata.ldp.solid.server.model.processor;

import com.google.gson.JsonObject;
import io.yodata.ldp.solid.server.model.Exchange;

public class StubInValidationProcessor implements InputValidationProcessor {

    @Override
    public JsonObject get(Exchange ex) {
        // no-op
        return new JsonObject();
    }

    @Override
    public JsonObject post(Exchange ex) {
        // no-op
        return new JsonObject();
    }

    @Override
    public JsonObject put(Exchange ex) {
        // no-op
        return new JsonObject();
    }

    @Override
    public JsonObject delete(Exchange ex) {
        // no-op
        return new JsonObject();
    }

}
