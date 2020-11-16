package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.exception.EncodingNotSupportedException;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GenericHandler {

    private static final Logger log = LoggerFactory.getLogger(GenericHandler.class);

    protected Store store;

    public GenericHandler(Store store) {
        this.store = store;
    }

    protected Exchange build(Request in) {
        Target t = Objects.requireNonNull(in.getTarget());
        in.setPolicy(store.getPolicies(t.getId()));
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

    protected void addKeysIfPossible(Request in, Map<String, JsonElement> keys) {
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

            JsonObject obj = el.getAsJsonObject();
            keys.forEach(obj::add);
            in.setBody(obj);
        } catch (RuntimeException e) {
            log.warn("Content type is JSON, but we could not parse it to add keys");
        }
    }

    protected void addIdIfPossible(Request in, String id) {
        addKeyIfPossible(in, true, "@id", id);
    }

    protected void addKeyIfPossible(Request in, boolean force, String id, String value) {
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

            JsonObject obj = el.getAsJsonObject();
            if (obj.has(id) && !force) {
                return;
            }

            obj.addProperty(id, value);
            in.setBody(obj);
        } catch (RuntimeException e) {
            log.warn("Content type is JSON, but we could not parse it to add ID");
        }
    }

    public Response head(Request in) {
        throw new ForbiddenException("This method is not allowed");
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
