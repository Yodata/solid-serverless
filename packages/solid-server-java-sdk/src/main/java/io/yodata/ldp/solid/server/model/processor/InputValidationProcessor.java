package io.yodata.ldp.solid.server.model.processor;

import io.yodata.ldp.solid.server.model.Exchange;

public interface InputValidationProcessor {

    void get(Exchange ex);

    void post(Exchange ex);

    void put(Exchange ex);

    void delete(Exchange ex);

}
