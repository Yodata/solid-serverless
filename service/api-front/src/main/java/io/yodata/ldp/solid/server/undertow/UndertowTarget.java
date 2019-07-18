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

import io.undertow.server.HttpServerExchange;
import io.yodata.ldp.solid.server.model.data.Target;
import io.yodata.ldp.solid.server.model.security.AclMode;
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
