package io.yodata.ldp.solid.server.model.processor;

import io.yodata.ldp.solid.server.model.Exchange;
import io.yodata.ldp.solid.server.model.ResponseLogAction;

public class StubOutValidationProcessor implements OutputValidationProcessor {

    private ResponseLogAction process(Exchange ex) {
        return ResponseLogAction.response(ex.getResponse());
    }

    @Override
    public ResponseLogAction get(Exchange ex) {
        return process(ex);
    }

    @Override
    public ResponseLogAction post(Exchange ex) {
        return process(ex);
    }

    @Override
    public ResponseLogAction put(Exchange ex) {
        return process(ex);
    }

    @Override
    public ResponseLogAction delete(Exchange ex) {
        return process(ex);
    }

}
