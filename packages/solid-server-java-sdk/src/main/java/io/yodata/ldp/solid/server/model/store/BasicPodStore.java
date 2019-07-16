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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.yodata.ldp.solid.server.model.data.Request;
import io.yodata.ldp.solid.server.model.store.fs.Filesystem;
import io.yodata.ldp.solid.server.model.store.fs.FsElement;
import io.yodata.ldp.solid.server.model.store.fs.FsElementMeta;
import io.yodata.ldp.solid.server.model.store.fs.FsPage;
import io.yodata.ldp.solid.server.model.transform.Policies;

public class BasicPodStore implements PodStore {

    private String id;
    private Filesystem fs;

    public BasicPodStore(String id, Filesystem fs) {
        this.id = id;
        this.fs = fs;
    }

    @Override
    public boolean exists(String path) {
        return fs.exists("/entities/" + id + path);
    }

    @Override
    public FsElementMeta head(String path) {
        return null;
    }

    @Override
    public FsElement get(String path) {
        return fs.getElement("/entities/" + id + path);
    }

    @Override
    public FsPage list(String path) {
        return null;
    }

    @Override
    public void post(Request in) {

    }

    @Override
    public void save(String path, JsonElement content) {

    }

    @Override
    public boolean save(String path, FsElement element) {
        return false;
    }

    @Override
    public void delete(String path) {

    }

    @Override
    public JsonObject getSubscriptions() {
        return null;
    }

    @Override
    public Policies getPolicies() {
        return null;
    }

}
