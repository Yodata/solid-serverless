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

package io.yodata.ldp.solid.server.model.store;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.model.store.fs.Filesystem;
import io.yodata.ldp.solid.server.model.transform.Policies;

import java.net.URI;

public class BasicStore implements Store {

    private Filesystem fs;

    public BasicStore(Filesystem fs) {
        this.fs = fs;
    }

    @Override
    public JsonObject getConfig() {
        return fs.findElement("/config").map(v -> GsonUtil.parseObj(v.getData())).orElseGet(JsonObject::new);
    }

    @Override
    public PodStore forPod(String podId) {
        return new BasicPodStore(podId, fs);
    }

    @Override
    public PodStore forDefault() {
        return null;
    }

    @Override
    public PodStore forGlobal() {
        return null;
    }

    @Override
    public Policies getPolicies(URI pod) {
        return null;
    }

}
