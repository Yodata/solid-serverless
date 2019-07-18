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

package io.yodata.ldp.solid.server.model.processor;

import io.yodata.ldp.solid.server.model.data.Exchange;

public class StubRequestFilter implements RequestFilter {

    @Override
    public void head(Exchange ex) {
        // no-op
    }

    @Override
    public void get(Exchange ex) {
        // no-op
    }

    @Override
    public void post(Exchange ex) {
        // no-op
    }

    @Override
    public void put(Exchange ex) {
        // no-op
    }

    @Override
    public void delete(Exchange ex) {
        // no-op
    }

}
