package io.yodata.ldp.solid.server.model.processor;

import io.yodata.ldp.solid.server.model.data.Exchange;
import io.yodata.ldp.solid.server.model.data.Response;

public class StubOutValidationProcessor implements OutputValidationProcessor {

    private Response process(Exchange ex) {
        return ex.getResponse();
    }

    @Override
    public Response get(Exchange ex) {
        return process(ex);
    }

    @Override
    public Response post(Exchange ex) {
        return process(ex);
    }

    @Override
    public Response put(Exchange ex) {
        return process(ex);
    }

    @Override
    public Response delete(Exchange ex) {
        return process(ex);
    }

}
