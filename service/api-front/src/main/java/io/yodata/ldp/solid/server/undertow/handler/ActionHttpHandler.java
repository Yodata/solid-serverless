package io.yodata.ldp.solid.server.undertow.handler;

import io.undertow.server.HttpServerExchange;
import io.yodata.ldp.solid.server.LogAction;

public abstract class ActionHttpHandler extends BasicHttpHandler {

    @Override
    public final void handleRequest(HttpServerExchange exchange) {
        throw new UnsupportedOperationException();
    }

    public abstract LogAction actionRequest(HttpServerExchange exchange) throws Exception;

}
