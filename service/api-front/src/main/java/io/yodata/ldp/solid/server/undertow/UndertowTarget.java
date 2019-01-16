package io.yodata.ldp.solid.server.undertow;

import io.undertow.server.HttpServerExchange;
import io.yodata.ldp.solid.server.model.AclMode;
import io.yodata.ldp.solid.server.model.Target;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class UndertowTarget extends Target {

    public static UndertowTarget build(HttpServerExchange ex, AclMode accessType) {
        String url = ex.getRequestURL();

        String proto = ex.getRequestHeaders().getFirst("X-Forwarded-Proto");
        if (StringUtils.isNotBlank(proto)) {
            try {
                URIBuilder uriBuilder = new URIBuilder(url);
                uriBuilder.setScheme(proto);
                url = uriBuilder.build().toString();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        UndertowTarget obj = new UndertowTarget();
        obj.setId(URI.create(url));
        obj.accessType = accessType;
        return obj;
    }

}
