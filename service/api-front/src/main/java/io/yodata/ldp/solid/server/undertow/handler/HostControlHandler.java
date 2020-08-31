package io.yodata.ldp.solid.server.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.model.SolidServer;

public class HostControlHandler extends BasicHttpHandler {

    private final SolidServer srv;
    private final HttpHandler h;

    public HostControlHandler(SolidServer srv, HttpHandler h) {
        this.srv = srv;
        this.h = h;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (!srv.isServingDomain(exchange.getHostName())) {
            throw new ForbiddenException();
        }

        h.handleRequest(exchange);
    }

}
