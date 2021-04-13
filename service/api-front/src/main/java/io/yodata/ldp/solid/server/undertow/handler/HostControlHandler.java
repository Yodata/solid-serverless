package io.yodata.ldp.solid.server.undertow.handler;

import io.undertow.server.HttpServerExchange;
import io.yodata.ldp.solid.server.LogAction;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.model.SolidServer;

public class HostControlHandler extends ActionHttpHandler {

    private final SolidServer srv;
    private final ActionHttpHandler h;

    public HostControlHandler(SolidServer srv, ActionHttpHandler h) {
        this.srv = srv;
        this.h = h;
    }

    @Override
    public LogAction actionRequest(HttpServerExchange exchange) throws Exception {
        if (!srv.isServingDomain(exchange.getHostName())) {
            throw new ForbiddenException();
        }

        return h.actionRequest(exchange);
    }

}
