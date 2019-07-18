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

package io.yodata.ldp.solid.server.model;

import io.yodata.ldp.solid.server.model.data.Page;
import io.yodata.ldp.solid.server.model.data.Request;
import io.yodata.ldp.solid.server.model.data.Response;
import io.yodata.ldp.solid.server.model.data.Target;

public interface SolidPod {

    SecurityContext getIdentity();

    SecurityContext identifyWithApiKey(String apiKey);

    SolidSession getSession(SecurityContext sc);

    Response head(Request in);

    Response get(Request in);

    Page list(Target t, String by, String from, boolean isFullFormat, boolean isTemporal);

    Response post(Request in);

    // returns true if data was overwritten
    Response put(Request in);

    Response delete(Request in);

}
