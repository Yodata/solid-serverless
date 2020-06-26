package io.yodata.ldp.solid.server.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.yodata.ldp.solid.server.aws.Configs;
import io.yodata.ldp.solid.server.config.Config;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class HostControlHandler extends BasicHttpHandler {

    private List<String> domains = new ArrayList<>();
    private HttpHandler h;

    public HostControlHandler(Config cfg, HttpHandler h) {
        String baseDomain = Configs.get().findOrBlank("reflex.domain.base"); // FIXME use store to store config

        if (StringUtils.isBlank(baseDomain)) {
            throw new RuntimeException("reflex.domain.base cannot be empty/bank");
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
