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
import io.yodata.ldp.solid.server.exception.NotFoundException;
import io.yodata.ldp.solid.server.model.data.Request;
import io.yodata.ldp.solid.server.model.store.fs.FsElement;
import io.yodata.ldp.solid.server.model.store.fs.FsElementMeta;
import io.yodata.ldp.solid.server.model.store.fs.FsPage;
import io.yodata.ldp.solid.server.model.transform.Policies;

import java.nio.file.Path;
import java.util.Optional;

public interface PodStore {

    boolean exists(String path);

    default Optional<FsElementMeta> tryHead(String path) {
        try {
            return Optional.of(head(path));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    FsElementMeta head(String path);

    default Optional<FsElement> tryGet(String path) {
        try {
            return Optional.of(get(path));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    FsElement get(String path);

    default FsElement get(Path path) {
        return get(path.toString());
    }

    FsPage list(String path);

    void post(Request in);

    void save(String path, JsonElement content);

    boolean save(String path, FsElement element);

    void delete(String path);

    JsonObject getSubscriptions();

    Policies getPolicies();

}
