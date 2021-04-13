package io.yodata.ldp.solid.server.undertow.handler;

import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.LogAction;
import io.yodata.ldp.solid.server.exception.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ExceptionHandler extends BasicHttpHandler {

    private final transient Logger log = LoggerFactory.getLogger("main");

    private static final String CorsAllValue = "*";
    private static final String CorsOriginName = "Access-Control-Allow-Origin";
    private static final String CorsMethodsName = "Access-Control-Allow-Methods";
    private static final String CorsMethodsValue = "GET, POST, PUT, DELETE, OPTIONS";
    private static final String CorsHeadersName = "Access-Control-Allow-Headers";
    private static final String CorsCredName = "Access-Control-Allow-Credentials";
    private static final String CorsReqHeadName = "Access-Control-Request-Headers";

    private final ActionHttpHandler h;

    public ExceptionHandler(ActionHttpHandler h) {
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
        JsonObject resultTop = new JsonObject();
        LogAction logged = LogAction.withType().setResult(resultTop);
        try {
            logged.setTarget(exchange.getRequestURL());

            putHeader(exchange, CorsOriginName, getOrigin(exchange));
            putHeader(exchange, CorsCredName, "true");
            putHeader(exchange, CorsMethodsName, CorsMethodsValue);
            putHeader(exchange, CorsHeadersName, getCorsRequestHeader(exchange));

            putHeader(exchange, "Cache-control", "no-store");
            putHeader(exchange, "Pragma", "no-cache");

            LogAction result = h.actionRequest(exchange);
            if (!Objects.isNull(result)) {
                logged.addChild(result, "RequestHandler");
            }
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
            logged.setError(e);
            writeBody(exchange, 500, GsonUtil.makeObj("error", "An internal server occurred"));
        } finally {
            exchange.endExchange();
            resultTop.addProperty("statusCode", exchange.getStatusCode());
            log.info(GsonUtil.toJson(logged));
        }
    }

}
