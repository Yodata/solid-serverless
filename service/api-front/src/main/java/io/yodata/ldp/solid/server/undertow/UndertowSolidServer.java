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

package io.yodata.ldp.solid.server.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.yodata.ldp.solid.server.aws.UndertorwRequest;
import io.yodata.ldp.solid.server.model.SolidServer;
import io.yodata.ldp.solid.server.model.core.Yolid;
import io.yodata.ldp.solid.server.model.data.Request;
import io.yodata.ldp.solid.server.model.data.Response;
import io.yodata.ldp.solid.server.model.security.AclMode;
import io.yodata.ldp.solid.server.undertow.handler.BasicHttpHandler;
import io.yodata.ldp.solid.server.undertow.handler.ExceptionHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndertowSolidServer {

    private static final Logger log = LoggerFactory.getLogger(UndertowSolidServer.class);

    public static void main(String[] args) {
        // Used in XNIO package, dependency of Undertow
        // We switch to slf4j
        System.setProperty("org.jboss.logging.provider", "slf4j");

        int multiplier = Integer.parseInt(StringUtils.defaultIfBlank(System.getenv("FRONTD_LOAD_MULTIPLIER"), "1"));
        int workerThreads = multiplier * 2 * 8;

        int port = 9000;
        String host = "0.0.0.0";

        log.info("-------/ Frontd is starting \\-------");
        log.info("Load multiplier: {}", multiplier);
        log.info("Will use {} HTTP worker threads", workerThreads);

        SolidServer srv = new Yolid();

        Undertow.builder().setWorkerThreads(workerThreads).addHttpListener(port, host).setHandler(Handlers.routing()
                .get("/status", exchange -> {
                    exchange.setStatusCode(200);
                    exchange.endExchange();
                })

                .add("HEAD", "/**", new BlockingHandler(new ExceptionHandler(new BasicHttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) {
                        UndertowTarget target = UndertowTarget.build(exchange, AclMode.Read);
                        Request request = UndertorwRequest.build(exchange, target);
                        Response r = srv.forPod(request.getTarget().getHost()).head(request);
                        writeBody(exchange, r);
                    }
                })))

                .get("/**", new BlockingHandler(new ExceptionHandler(new BasicHttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) {
                        UndertowTarget target = UndertowTarget.build(exchange, AclMode.Read);
                        Request request = UndertorwRequest.build(exchange, target);
                        Response r = srv.forPod(request.getTarget().getHost()).get(request);
                        writeBody(exchange, r);
                    }
                })))


                .post("/**", new BlockingHandler(new ExceptionHandler(new BasicHttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) {
                        UndertowTarget target = UndertowTarget.build(exchange, AclMode.Append);
                        Request request = UndertorwRequest.build(exchange, target);
                        Response r = srv.forPod(request.getTarget().getHost()).post(request);
                        writeBody(exchange, r);
                    }
                })))

                .put("/**", new BlockingHandler(new ExceptionHandler(new BasicHttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) {
                        UndertowTarget target = UndertowTarget.build(exchange, AclMode.Write);
                        Request request = UndertorwRequest.build(exchange, target);
                        Response r = srv.forPod(request.getTarget().getHost()).put(request);
                        writeBody(exchange, r);
                    }
                })))


                .delete("/**", new BlockingHandler(new ExceptionHandler(new BasicHttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) {
                        UndertowTarget target = UndertowTarget.build(exchange, AclMode.Write);
                        Request request = UndertorwRequest.build(exchange, target);
                        Response r = srv.forPod(request.getTarget().getHost()).delete(request);
                        writeBody(exchange, r);
                    }
                })))

        ).build().start();

        log.info("-------\\ Frontd is running /-------");
    }

}
