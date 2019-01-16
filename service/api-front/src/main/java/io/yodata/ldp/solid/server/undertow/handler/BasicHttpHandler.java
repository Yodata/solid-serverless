package io.yodata.ldp.solid.server.undertow.handler;

import com.google.gson.JsonElement;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.yodata.ldp.solid.server.model.Response;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.StreamSupport;

public abstract class BasicHttpHandler implements HttpHandler {

    private Logger log = LoggerFactory.getLogger(BasicHttpHandler.class);

    protected Map<String, List<String>> getHeaders(HttpServerExchange exchange) {
        Map<String, List<String>> headers = new HashMap<>();

        StreamSupport.stream(exchange.getRequestHeaders().spliterator(), false).forEach(k -> {
            k.forEach(v -> headers.computeIfAbsent(k.getHeaderName().toString().toLowerCase(), k1 -> new ArrayList<>()).add(v));
        });

        return headers;
    }

    protected void writeBody(HttpServerExchange ex, int statusCode, JsonElement e) {
        Response r = new Response();
        r.setStatus(statusCode);
        r.setBody(e);
        writeBody(ex, r);
    }

    protected void writeBody(HttpServerExchange ex, Response response) {
        ex.setStatusCode(response.getStatus());
        response.getHeaders().forEach((k, v) -> ex.getResponseHeaders().putAll(HttpString.tryFromString(k), Collections.singletonList(v)));
        response.getBody().ifPresent(outBytes -> {
            try {
                ex.setResponseContentLength(outBytes.length);
                IOUtils.write(outBytes, ex.getOutputStream());
                log.info("HTTP Request {}: Response written", ex.hashCode());
            } catch (IOException e) {
                if (ex.getConnection().isOpen()) {
                    log.warn("We failed to write response back to client: {}", e.getMessage());
                } else {
                    log.debug("Connection was closed before we could flush data");
                }
            }
        });
    }

}
