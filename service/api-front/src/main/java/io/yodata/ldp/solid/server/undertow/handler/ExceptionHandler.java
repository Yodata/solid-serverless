package io.yodata.ldp.solid.server.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.exception.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandler extends BasicHttpHandler {

    private final transient Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    private static final String CorsAllValue = "*";
    private static final String CorsOriginName = "Access-Control-Allow-Origin";
    private static final String CorsMethodsName = "Access-Control-Allow-Methods";
    private static final String CorsMethodsValue = "GET, POST, PUT, DELETE, OPTIONS";
    private static final String CorsHeadersName = "Access-Control-Allow-Headers";
    private static final String CorsCredName = "Access-Control-Allow-Credentials";
    private static final String CorsReqHeadName = "Access-Control-Request-Headers";

    private final HttpHandler h;

    public ExceptionHandler(HttpHandler h) {
        this.h = h;
    }

    private String getOrigin(HttpServerExchange ex) {
        return StringUtils.defaultIfBlank(ex.getRequestHeaders().getFirst("Origin"), CorsAllValue);
    }

    private String getCorsRequestHeader(HttpServerExchange ex) {
        return StringUtils.defaultIfBlank(ex.getRequestHeaders().getFirst(CorsReqHeadName), CorsAllValue);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            log.info("HTTP Request {}: Start", exchange.hashCode());

            putHeader(exchange, CorsOriginName, getOrigin(exchange));
            putHeader(exchange, CorsCredName, "true");
            putHeader(exchange, CorsMethodsName, CorsMethodsValue);
            putHeader(exchange, CorsHeadersName, getCorsRequestHeader(exchange));

            putHeader(exchange, "Cache-control", "no-store");
            putHeader(exchange, "Pragma", "no-cache");

            h.handleRequest(exchange);
        } catch (IllegalArgumentException | BadRequestException e) {
            writeBody(exchange, 400, GsonUtil.makeObj("error", e.getMessage()));
        } catch (UnauthorizedException e) {
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
