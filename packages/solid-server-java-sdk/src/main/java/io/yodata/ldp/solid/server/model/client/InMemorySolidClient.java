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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemorySolidClient implements SolidClient {

    private Map<URI, LdpResource> resources = new HashMap<>();

    public Map<URI, LdpResource> resources() {
        return resources;
    }

    public void clear() {
        resources.clear();
    }

    @Override
    public Optional<LdpResource> get(URI resourceId) {
        return Optional.ofNullable(resources.get(resourceId));
    }

    @Override
    public void put(URI resourceId, JsonObject data) {
        resources.put(resourceId, new RawLdpResource(GsonUtil.toJsonBytes(data)));
    }

    @Override
    public String append(URI resourceId, JsonObject data) {
        // FIXME not right
        put(URI.create(resourceId.toString() + UUID.randomUUID().toString()), data);
        return "";
    }

}
