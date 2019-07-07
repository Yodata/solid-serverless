package io.yodata.ldp.solid.server.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.exception.EncodingNotSupportedException;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.exception.NotFoundException;
import io.yodata.ldp.solid.server.exception.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandler extends BasicHttpHandler {

    private final transient Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    private HttpHandler h;

    public ExceptionHandler(HttpHandler h) {
        this.h = h;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            log.info("HTTP Request {}: Start", exchange.hashCode());
            h.handleRequest(exchange);
        } catch (IllegalArgumentException e) {
            writeBody(exchange, 400, GsonUtil.makeObj("error", e.getMessage()));
        } catch (UnauthenticatedException e) {
            writeBody(exchange, 401, GsonUtil.makeObj("error", e.getMessage()));
        } catch (ForbiddenException e) {
            writeBody(exchange, 403, GsonUtil.makeObj("error", e.getMessage()));
        } catch (NotFoundException e) {
            writeBody(exchange, 404, GsonUtil.makeObj("error", e.getMessage()));
        } catch (EncodingNotSupportedException e) {
            writeBody(exchange, 415, GsonUtil.makeObj("error", e.getMessage()));
        } catch (RuntimeException e) {
            e.printStackTrace();
            writeBody(exchange, 500, GsonUtil.makeObj("error", "An internal server occurred"));
        } finally {
            exchange.endExchange();
            log.info("HTTP Request {}: End", exchange.hashCode());
        }
    }

}
