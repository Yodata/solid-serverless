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
import io.yodata.ldp.solid.server.model.transform.Policies;

import java.net.URI;

public interface Store {

    JsonObject getConfig();

    default PodStore forPod(URI pod) {
        return forPod(pod.getHost());
    }

    PodStore forPod(String podId);

    PodStore forDefault();

    PodStore forGlobal();

    Policies getPolicies(URI pod);

}