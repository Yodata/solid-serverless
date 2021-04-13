package io.yodata.ldp.solid.server.model.processor;

import io.yodata.ldp.solid.server.model.Exchange;
import io.yodata.ldp.solid.server.model.ResponseLogAction;

public interface OutputValidationProcessor {

    ResponseLogAction get(Exchange ex);

    ResponseLogAction post(Exchange ex);

    ResponseLogAction put(Exchange ex);

    ResponseLogAction delete(Exchange ex);

}
