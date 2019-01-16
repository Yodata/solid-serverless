package io.yodata.ldp.solid.server.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.yodata.ldp.solid.server.undertow.handler.ExceptionHandler;
import io.yodata.ldp.solid.server.aws.SecurityProcessor;
import io.yodata.ldp.solid.server.aws.UndertorwRequest;
import io.yodata.ldp.solid.server.aws.handler.container.ContainerHandler;
import io.yodata.ldp.solid.server.aws.handler.resource.ResourceHandler;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.undertow.handler.BasicHttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class UndertowSolidServer {

    private static final Logger log = LoggerFactory.getLogger(UndertowSolidServer.class);

    public static void main(String[] args) {
        log.info("-------/ Frontd is starting \\-------");

        ContainerHandler folder = new ContainerHandler();
        ResourceHandler file = new ResourceHandler();
        SecurityProcessor auth = new SecurityProcessor();

        Undertow.builder().addHttpListener(9000, "0.0.0.0").setHandler(Handlers.routing()
                .get("/status", exchange -> {
                    exchange.setStatusCode(200);
                    exchange.endExchange();
                })

                .get("/**", new BlockingHandler(new ExceptionHandler(new BasicHttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) {
                        UndertowTarget target = UndertowTarget.build(exchange, AclMode.Read);
                        Map<String, List<String>> headers = getHeaders(exchange);
                        SecurityContext context = auth.authenticate(headers);
                        Acl rights = auth.authorize(context, target);
                        Request request = UndertorwRequest.build(exchange, context, target, rights, headers);

                        Response r;
                        if (exchange.getRequestPath().endsWith("/")) {
                            r = folder.get(request);
                        } else {
                            r = file.get(request);
                        }

                        writeBody(exchange, r);
                    }
                })))

                .post("/**", new BlockingHandler(new ExceptionHandler(new BasicHttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) {
                        UndertowTarget target = UndertowTarget.build(exchange, AclMode.Append);
                        Map<String, List<String>> headers = getHeaders(exchange);
                        SecurityContext context = auth.authenticate(headers);
                        Acl rights = auth.authorize(context, target);
                        Request request = UndertorwRequest.build(exchange, context, target, rights, headers);

                        Response r;
                        if (exchange.getRequestPath().endsWith("/")) {
                            r = folder.post(request);
                        } else {
                            r = file.post(request);
                        }

                        writeBody(exchange, r);
                    }
                })))

                .put("/**", new BlockingHandler(new ExceptionHandler(new BasicHttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) {
                        UndertowTarget target = UndertowTarget.build(exchange, AclMode.Write);
                        Map<String, List<String>> headers = getHeaders(exchange);
                        SecurityContext context = auth.authenticate(headers);
                        Acl rights = auth.authorize(context, target);
                        Request request = UndertorwRequest.build(exchange, context, target, rights, headers);

                        Response r;
                        if (exchange.getRequestPath().endsWith("/")) {
                            r = folder.put(request);
                        } else {
                            r = file.put(request);
                        }

                        writeBody(exchange, r);
                    }
                })))

                .delete("/**", new BlockingHandler(new ExceptionHandler(new BasicHttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) {
                        UndertowTarget target = UndertowTarget.build(exchange, AclMode.Write);
                        Map<String, List<String>> headers = getHeaders(exchange);
                        SecurityContext context = auth.authenticate(headers);
                        Acl rights = auth.authorize(context, target);
                        Request request = UndertorwRequest.build(exchange, context, target, rights, headers);

                        Response r;
                        if (exchange.getRequestPath().endsWith("/")) {
                            r = folder.delete(request);
                        } else {
                            r = file.delete(request);
                        }

                        writeBody(exchange, r);
                    }
                })))).build().start();

        log.info("-------\\ Frontd is running /-------");
    }

}
