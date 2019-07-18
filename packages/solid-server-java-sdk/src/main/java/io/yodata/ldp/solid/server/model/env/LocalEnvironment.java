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

package io.yodata.ldp.solid.server.model.env;

import io.yodata.EnvUtils;
import io.yodata.ldp.solid.server.model.processor.RequestFilter;
import io.yodata.ldp.solid.server.model.processor.ResponseFilter;
import io.yodata.ldp.solid.server.model.store.BasicStore;
import io.yodata.ldp.solid.server.model.store.Store;
import io.yodata.ldp.solid.server.model.store.fs.Filesystem;
import io.yodata.ldp.solid.server.model.store.fs.local.LocalFilesystem;

import java.util.Collections;
import java.util.List;

public class LocalEnvironment extends Environment {

    private Filesystem fs;
    private Store store;

    public LocalEnvironment() {
        fs = new LocalFilesystem(EnvUtils.get("YOLID_FS_LOCAL_PATH"));
        store = new BasicStore(fs);
    }

    @Override
    public long getPriority() {
        return 0;
    }

    @Override
    public Store getStore() {
        return store;
    }

    @Override
    public List<RequestFilter> getInputFilters() {
        return Collections.emptyList();
    }

    @Override
    public List<ResponseFilter> getOutputFilters() {
        return Collections.emptyList();
    }

}
