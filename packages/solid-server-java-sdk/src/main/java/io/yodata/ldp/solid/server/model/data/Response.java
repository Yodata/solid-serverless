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

package io.yodata.ldp.solid.server.model.data;

import com.google.gson.JsonElement;
import io.yodata.Base64Util;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Response {

    public static Response successful() {
        return new Response();
    }

    private int status = 200;
    private Map<String, String> headers = new HashMap<>();
    private String body;
    private boolean isBase64Encoded = true;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getContentType() {
        return headers.get("Content-type");
    }

    public void setContentType(String encoding) {
        headers.put("Content-type", encoding);
    }

    public Optional<byte[]> getBody() {
        if (Objects.nonNull(body)) {
            return Optional.of(Base64Util.decode(body));
        }

        return Optional.empty();
    }

    public void setBody(JsonElement el) {
        setJsonBody(el);
    }

    public void setBody(byte[] bytes) {
        body = Base64Util.encode(bytes);
    }

    public boolean isBase64Encoded() {
        return isBase64Encoded;
    }

    public void setBase64Encoded(boolean base64Encoded) {
        isBase64Encoded = base64Encoded;
    }

    public void setJsonBody(Object o) {
        setBody(GsonUtil.toJson(o).getBytes(StandardCharsets.UTF_8));
        setContentType(MimeTypes.APPLICATION_JSON);
    }

}
