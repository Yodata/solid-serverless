package io.yodata.ldp.solid.server.model.processor;

import io.yodata.ldp.solid.server.model.data.Exchange;
import io.yodata.ldp.solid.server.model.data.Response;

public interface OutputValidationProcessor {

    Response get(Exchange ex);

    Response post(Exchange ex);

    Response put(Exchange ex);

    Response delete(Exchange ex);

}
