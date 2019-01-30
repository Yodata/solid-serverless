package io.yodata.ldp.solid.server.aws.handler;

import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.exception.EncodingNotSupportedException;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.model.Exchange;
import io.yodata.ldp.solid.server.model.Request;
import io.yodata.ldp.solid.server.model.Response;

import java.util.Optional;

public class GenericHandler {

    protected S3Store store;

    public GenericHandler(S3Store store) {
        this.store = store;
    }

    protected Exchange build(Request in) {
        in.setPolicy(store.getPolicies(in.getTarget().getId()));
        Exchange ex = new Exchange();
        ex.setRequest(in);
        return ex;
    }

    protected String ensureEncoding(Request in) {
        Optional<String> contentType = in.getContentType();
        if (!contentType.isPresent()) {
            throw new EncodingNotSupportedException("No encoding was provided");
        }

        return contentType.get();
    }

    public Response get(Request in) {
        throw new ForbiddenException("This method is not allowed");
    }

    public Response delete(Request in) {
        throw new ForbiddenException("This method is not allowed");
    }

    public Response post(Request in) {
        throw new ForbiddenException("This method is not allowed");
    }

    public Response put(Request in) {
        throw new ForbiddenException("This method is not allowed");
    }

}
