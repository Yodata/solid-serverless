package io.yodata.ldp.solid.server.model.processor;

import io.yodata.ldp.solid.server.model.data.Exchange;

public class StubInValidationProcessor implements InputValidationProcessor {

    @Override
    public void get(Exchange ex) {
        // no-op
    }

    @Override
    public void post(Exchange ex) {
        // no-op
    }

    @Override
    public void put(Exchange ex) {
        // no-op
    }

    @Override
    public void delete(Exchange ex) {
        // no-op
    }

}
