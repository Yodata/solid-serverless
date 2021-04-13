package io.yodata.ldp.solid.server.model.processor;

import com.google.gson.JsonObject;
import io.yodata.ldp.solid.server.model.Exchange;

public interface InputValidationProcessor {

    JsonObject get(Exchange ex);

    JsonObject post(Exchange ex);

    JsonObject put(Exchange ex);

    JsonObject delete(Exchange ex);

}
