package io.yodata.ldp.solid.server.aws.handler;

import com.google.gson.JsonElement;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.exception.EncodingNotSupportedException;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.model.Exchange;
import io.yodata.ldp.solid.server.model.Request;
import io.yodata.ldp.solid.server.model.Response;
import io.yodata.ldp.solid.server.model.Store;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class GenericHandler {

    private static final Logger log = LoggerFactory.getLogger(GenericHandler.class);

    protected Store store;

    public GenericHandler(Store store) {
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

    protected void addIdIfPossible(Request in, String id) {
        if (!StringUtils.equals(MimeTypes.APPLICATION_JSON, in.getContentType().orElse(""))) {
            // Ignoring content which is not JSON
            return;
        }

        // We try to add the ID to the object
        try {
            JsonElement el = GsonUtil.parse(in.getBody());
            if (!el.isJsonObject()) {
                log.info("JSON is not an object, skipping adding ID");
                return;
            }

            el.getAsJsonObject().addProperty("id", id);
            in.setBody(el);
        } catch (RuntimeException e) {
            log.warn("Content type is JSON, but we could not parse it to add ID");
        }
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
