package io.yodata.ldp.solid.server.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.yodata.ldp.solid.server.config.Config;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static io.yodata.ldp.solid.server.aws.Configs.DOM_BASE;

public class HostControlHandler extends BasicHttpHandler {

    private final List<String> domains = new ArrayList<>();
    private final HttpHandler h;

    public HostControlHandler(Config cfg, HttpHandler h) { // FIXME this does not handle runtime update of the config
        String baseDomain = cfg.findOrBlank(DOM_BASE);

        if (StringUtils.isBlank(baseDomain)) {
            throw new RuntimeException(DOM_BASE + " cannot be empty/bank");
        }

        domains.add(StringUtils.lowerCase(baseDomain));

        this.h = h;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (domains.stream().noneMatch(domain -> StringUtils.equalsIgnoreCase(domain, exchange.getHostName()) || exchange.getHostName().endsWith("." + domain))) {
            throw new ForbiddenException();
        }

        h.handleRequest(exchange);
    }

}
