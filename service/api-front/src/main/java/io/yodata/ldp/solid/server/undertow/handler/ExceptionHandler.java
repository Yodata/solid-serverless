/*
 * Copyright 2018 YoData, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.yodata.ldp.solid.server.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.exception.EncodingNotSupportedException;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.exception.NotFoundException;
import io.yodata.ldp.solid.server.exception.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ExceptionHandler extends BasicHttpHandler {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    private HttpHandler h;

    public ExceptionHandler(HttpHandler h) {
        this.h = h;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String exId = UUID.randomUUID().toString();
        try {
            exchange.getResponseHeaders().put(HttpString.tryFromString("X-Solid-Request-Id"), exId);
            log.info("HTTP Request {}: Start", exId);
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
            log.info("HTTP Request {}: End", exId);
        }
    }

}
