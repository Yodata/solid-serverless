/*
 * Copyright 2019 YoData, Inc.
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

package io.yodata.ldp.solid.server.model.client;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.RawLdpResource;
import io.yodata.ldp.solid.server.model.LdpResource;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Optional;

public class SimpleSolidClient implements SolidClient {

    private static final Logger log = LoggerFactory.getLogger(SimpleSolidClient.class);

    private CloseableHttpClient client;
    private String apiKey;

    public SimpleSolidClient() {
        client = HttpClients.createSystem();
    }

    public SimpleSolidClient(String apiKey) {
        this();
        this.apiKey = apiKey;
    }

    private Optional<String> getKey() {
        return Optional.ofNullable(apiKey);
    }

    @Override
    public Optional<LdpResource> get(URI resourceId) {
        HttpGet get = new HttpGet(resourceId);
        getKey().ifPresent(key -> get.addHeader("X-API-Key", apiKey));

        try (CloseableHttpResponse res = client.execute(get)) {
            int sc = res.getStatusLine().getStatusCode();
            log.debug("GET {}: {}", resourceId, sc);
            if (sc != 200) {
                if (sc == 404) {
                    return Optional.empty();
                }

                throw new RuntimeException("Unable to fetch entity. Content was: " + EntityUtils.toString(res.getEntity()));
            }

            return Optional.of(new RawLdpResource(EntityUtils.toByteArray(res.getEntity())));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void put(URI resourceId, JsonObject data) {
        HttpPut req = new HttpPut(resourceId);
        getKey().ifPresent(key -> req.addHeader("X-API-Key", key));
        req.setEntity(new StringEntity(GsonUtil.toJson(data), ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse res = client.execute(req)) {
            int sc = res.getStatusLine().getStatusCode();
            if (sc < 200 || sc > 299) {
                throw new RuntimeException("Unable to save change: " + sc + " - " + EntityUtils.toString(res.getEntity()));
            }

            log.debug("PUT {}: success", resourceId);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save change", e);
        }
    }

    @Override
    public String append(URI resourceId, JsonObject data) {
        HttpPost req = new HttpPost(resourceId);
        getKey().ifPresent(key -> req.addHeader("X-API-Key", key));
        req.setEntity(new StringEntity(GsonUtil.toJson(data), ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse res = client.execute(req)) {
            int sc = res.getStatusLine().getStatusCode();
            if (sc < 200 || sc > 299) {
                throw new RuntimeException("Unable to save change: " + sc + " - " + EntityUtils.toString(res.getEntity()));
            }

            log.debug("POST {}: success", resourceId);

            return EntityUtils.toString(res.getEntity());
        } catch (IOException e) {
            throw new RuntimeException("Unable to save change", e);
        }
    }

}
