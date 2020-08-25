package io.yodata.ldp.solid.server.undertow;

import com.google.gson.JsonObject;
import com.onelogin.saml2.settings.SettingsBuilder;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.RedirectHandler;
import io.undertow.util.StatusCodes;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.Configs;
import io.yodata.ldp.solid.server.aws.UndertorwRequest;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.config.EnvConfig;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.model.container.ContainerHandler;
import io.yodata.ldp.solid.server.model.resource.ResourceHandler;
import io.yodata.ldp.solid.server.saml.ReflexSamlResponse;
import io.yodata.ldp.solid.server.undertow.handler.BasicHttpHandler;
import io.yodata.ldp.solid.server.undertow.handler.ExceptionHandler;
import io.yodata.ldp.solid.server.undertow.handler.HostControlHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static io.yodata.ldp.solid.server.aws.Configs.DOM_BASE;

public class UndertowSolidServer {

    private static final Logger log = LoggerFactory.getLogger(UndertowSolidServer.class);

    public static final String AUTH_COOKIE_TY_NAME = "reflex_session_type";
    public static final String AUTH_COOKIE_TO_NAME = "reflex_session_token";

    public static void main(String[] args) {
        log.info("-------/ Frontd is starting \\-------");

        int multiplier = Integer.parseInt(StringUtils.defaultIfBlank(EnvConfig.get().findOrBlank("FRONTD_LOAD_MULTIPLIER"), "1"));
        String samlIdpUrl = Configs.get().findOrBlank("reflex.auth.saml.idp.url");
        String baseDomain = Configs.get().findOrBlank(DOM_BASE);
        String samlAppRedirectUrl = Configs.get().findOrBlank("reflex.auth.saml.acs.url");
        int workerThreads = multiplier * 2 * 8;

        int port = 9000;
        String host = "0.0.0.0";

        if (StringUtils.isBlank(baseDomain)) {
            throw new RuntimeException(DOM_BASE + " cannot be empty/bank");
        }

        if (StringUtils.isBlank(samlIdpUrl)) {
            throw new RuntimeException("reflex.auth.saml.idp.url cannot be empty/bank");
        }

        if (StringUtils.isBlank(samlAppRedirectUrl)) {
            throw new RuntimeException("reflex.auth.saml.acs.url cannot be empty/bank");
        }

        log.info("Load multiplier: {}", multiplier);
        log.info("Will use {} HTTP worker threads", workerThreads);

        EntityBasedStore store = S3Store.getDefault();
        ContainerHandler folder = new ContainerHandler(store);
        ResourceHandler file = new ResourceHandler(store);
        SecurityProcessor auth = new SecurityProcessor(store);

        HttpHandler samlWhoamiHandler = new BlockingHandler(new ExceptionHandler(new HostControlHandler(Configs.get(), new BasicHttpHandler() {

            @Override
            public void handleRequest(HttpServerExchange ex) {
                Cookie sessionTypeCookie = ex.getRequestCookies().getOrDefault(AUTH_COOKIE_TY_NAME, new CookieImpl(AUTH_COOKIE_TY_NAME));
                Cookie sessionTokenCookie = ex.getRequestCookies().getOrDefault(AUTH_COOKIE_TO_NAME, new CookieImpl(AUTH_COOKIE_TO_NAME));

                String sessionType = sessionTypeCookie.getValue();
                String sessionToken = sessionTokenCookie.getValue();

                String path = "global/security/api/token/" + sessionType + "/" + sessionToken;
                Optional<String> sessionDataOpt = store.getData(path);
                JsonObject sessionData = sessionDataOpt.flatMap(GsonUtil::tryParseObj).orElseGet(JsonObject::new);

                if (StringUtils.isAnyBlank(sessionType, sessionToken) || sessionData.size() == 0) {
                    //FIXME clear any possible remaining cookie
                    ex.setStatusCode(401);
                    ex.endExchange();
                    return;
                }

                writeBody(ex, 200, sessionData);
            }

        })));

        HttpHandler redirectHandler = new RedirectHandler(samlIdpUrl);

        Undertow.builder().setWorkerThreads(workerThreads).addHttpListener(port, host).setHandler(Handlers.routing()
                .get("/status", exchange -> {
                    exchange.setStatusCode(200);
                    exchange.endExchange();
                })

                .add("OPTIONS", "/**", new ExceptionHandler(new HostControlHandler(Configs.get(), exchange -> {
                })))

                .get("/reflex/auth/saml/login", new ExceptionHandler(new HostControlHandler(Configs.get(), exchange -> {
                    log.info("Got a GET for /reflex/auth/saml/login");
                    log.info("Redirecting to the SAML IDP URL");
                    redirectHandler.handleRequest(exchange);
                })))

                .get("/reflex/auth/logout", new BlockingHandler(new ExceptionHandler(new HostControlHandler(Configs.get(), exchange -> {
                    log.info("Got a GET for /reflex/auth/logout");

                    Cookie sessionTypeCookie = exchange.getRequestCookies().getOrDefault(AUTH_COOKIE_TY_NAME, new CookieImpl(AUTH_COOKIE_TY_NAME));
                    Cookie sessionTokenCookie = exchange.getRequestCookies().getOrDefault(AUTH_COOKIE_TO_NAME, new CookieImpl(AUTH_COOKIE_TO_NAME));

                    String sessionType = sessionTypeCookie.getValue();
                    String sessionToken = sessionTokenCookie.getValue();

                    if (StringUtils.isNotBlank(sessionType)) {
                        log.info("Clearing all active sessions");

                        store.delete("global/security/api/token/" + sessionType + "/" + sessionToken);
                    }

                    sessionTypeCookie = sessionTypeCookie.setDiscard(true).setExpires(Date.from(Instant.EPOCH));
                    sessionTokenCookie = sessionTokenCookie.setDiscard(true).setExpires(Date.from(Instant.EPOCH));

                    exchange.getResponseCookies().put(sessionTypeCookie.getName(), sessionTypeCookie);
                    exchange.getResponseCookies().put(sessionTokenCookie.getName(), sessionTokenCookie);
                }))))

                .post("/reflex/auth/saml/acs", new BlockingHandler(new ExceptionHandler(new HostControlHandler(Configs.get(), exchange -> {
                    log.info("Got a POST for /reflex/auth/saml/acs");
                    String body = UndertorwRequest.getBodyString(exchange);
                    log.debug("Body: {}", body);
                    ReflexSamlResponse saml = new ReflexSamlResponse(new SettingsBuilder().build(), exchange.getRequestURL(), body);
                    log.debug("SAML XML: \n{}", saml.getSAMLResponseXml());
                    log.debug("-----------");
                    log.debug("Attributes: {}", GsonUtil.getPrettyForLog(saml.getAttributes()));

                    JsonObject samlAttributes = GsonUtil.makeObj(saml.getAttributes());
                    String contactId = GsonUtil.extractString(samlAttributes, "contact_id", "");
                    if (StringUtils.isBlank(contactId)) {
                        throw new RuntimeException("IDP did not include mandatory data - key: contact_id");
                    }

                    // FIXME we should sign this
                    String sessionToken = UUID.randomUUID().toString().replace("-", "");
                    Instant expiresAt = Instant.now().plusSeconds(24 * 60 * 60); // 24H
                    Cookie sessionTypeCookie = new ReflexCookieImpl(AUTH_COOKIE_TY_NAME, "saml")
                            .setSecure(true)
                            .setSameSiteMode("None")
                            .setPath("/")
                            .setExpires(Date.from(expiresAt));
                    Cookie sessionTokenCookie = new ReflexCookieImpl(AUTH_COOKIE_TO_NAME, sessionToken)
                            .setSecure(true)
                            .setSameSiteMode("None")
                            .setPath("/")
                            .setExpires(Date.from(expiresAt));

                    JsonObject sessionData = new JsonObject();
                    sessionData.add("raw", samlAttributes);
                    sessionData.addProperty("profile_id", "https://" + contactId + "." + baseDomain + "/profile/card#me");
                    sessionData.addProperty("valid_not_after", expiresAt.toEpochMilli());
                    store.save("global/security/api/token/saml/" + sessionToken, sessionData);

                    exchange.getResponseCookies().put(sessionTypeCookie.getName(), sessionTypeCookie);
                    exchange.getResponseCookies().put(sessionTokenCookie.getName(), sessionTokenCookie);

                    new RedirectHandler(samlAppRedirectUrl).handleRequest(exchange);
                    log.info("Redirected to App URL");
                }))))

                .put("/reflex/auth/saml/acs", new ExceptionHandler(ex -> ex.setStatusCode(StatusCodes.METHOD_NOT_ALLOWED)))

                .get("/reflex/auth/whoami", samlWhoamiHandler)
                .post("/reflex/auth/whoami", samlWhoamiHandler)

                .add("HEAD", "/**", new BlockingHandler(new ExceptionHandler(new HostControlHandler(Configs.get(), new BasicHttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) {
                        UndertowTarget target = UndertowTarget.build(exchange, AclMode.Read);
                        Map<String, List<String>> headers = getHeaders(exchange);
                        SecurityContext context = auth.authenticate(headers);
                        Acl rights = auth.authorize(context, target);
                        Request request = UndertorwRequest.build(exchange, context, target, rights, headers);

                        Response r;
                        if (exchange.getRequestPath().endsWith("/")) {
                            r = folder.head(request);
                        } else {
                            r = file.head(request);
                        }

                        writeBody(exchange, r);
                    }
                }))))

                .get("/**", new BlockingHandler(new ExceptionHandler(new HostControlHandler(Configs.get(), new BasicHttpHandler() {
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
                }))))

                .post("/**", new BlockingHandler(new ExceptionHandler(new HostControlHandler(Configs.get(), new BasicHttpHandler() {
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
                }))))

                .put("/**", new BlockingHandler(new ExceptionHandler(new HostControlHandler(Configs.get(), new BasicHttpHandler() {
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
                }))))

                .delete("/**", new BlockingHandler(new ExceptionHandler(new HostControlHandler(Configs.get(), new BasicHttpHandler() {
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
                }))))).build().start();

        log.info("-------\\ Frontd is running /-------");
    }

}
