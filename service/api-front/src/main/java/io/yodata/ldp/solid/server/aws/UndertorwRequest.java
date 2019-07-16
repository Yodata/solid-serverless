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

package io.yodata.ldp.solid.server.aws;

import io.undertow.server.HttpServerExchange;
import io.yodata.ldp.solid.server.model.SecurityContext;
import io.yodata.ldp.solid.server.model.data.Request;
import io.yodata.ldp.solid.server.model.security.Acl;
import io.yodata.ldp.solid.server.undertow.UndertowTarget;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.StreamSupport;

public class UndertorwRequest extends Request {

    private static byte[] getBody(HttpServerExchange ex) {
        if (ex.getRequestContentLength() < 1) {
            return new byte[0];
        }

        try {
            return IOUtils.toByteArray(ex.getInputStream(), ex.getRequestContentLength());
        } catch (IOException e) {
            throw new RuntimeException("Error while fetching request body", e);
        }
    }

    private static Map<String, List<String>> getHeaders(HttpServerExchange exchange) {
        Map<String, List<String>> headers = new HashMap<>();

        StreamSupport.stream(exchange.getRequestHeaders().spliterator(), false).forEach(k -> {
            k.forEach(v -> headers.computeIfAbsent(k.getHeaderName().toString().toLowerCase(), k1 -> new ArrayList<>()).add(v));
        });

        return headers;
    }

    public static UndertorwRequest build(
            HttpServerExchange ex,
            SecurityContext sc,
            UndertowTarget target,
            Acl acl,
            Map<String, List<String>> headers
    ) {
        UndertorwRequest r = new UndertorwRequest();

        r.setId(UUID.randomUUID().toString());
        r.setTimestamp(Instant.now());
        r.setSecurity(sc);
        r.setTarget(target);
        r.setAcl(acl);
        r.setMethod(ex.getRequestMethod().toString());
        r.setHeaders(new HashMap<>(headers));
        r.setParameters(new HashMap<>());

        ex.getQueryParameters().forEach((key, queue) -> queue.forEach(value -> {
            r.parameters.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }));

        r.setBody(getBody(ex));

        return r;
    }

    public static UndertorwRequest build(HttpServerExchange ex, UndertowTarget target) {
        UndertorwRequest r = new UndertorwRequest();

        r.setId(UUID.randomUUID().toString());
        r.setTimestamp(Instant.now());
        r.setMethod(ex.getRequestMethod().toString());
        r.setTarget(target);
        r.setHeaders(getHeaders(ex));
        r.setParameters(new HashMap<>());

        return r;
    }

}
