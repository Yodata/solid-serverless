/*
 * Solid Serverless
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

package io.yodata.ldp.solid.server.model;

import io.yodata.ldp.solid.server.exception.NotFoundException;
import io.yodata.ldp.solid.server.model.store.Store;

import java.util.*;

public abstract class Environment {

    private static ServiceLoader<EnvironmentLoader> svcLoader;

    public static synchronized void bootstrap() {
        if (Objects.isNull(svcLoader)) {
            svcLoader = ServiceLoader.load(EnvironmentLoader.class);
        }
    }

    public static Environment get() {
        bootstrap();

        List<Environment> loaded = new ArrayList<>();
        for (EnvironmentLoader loader : svcLoader) {
            loader.load().ifPresent(loaded::add);
        }

        return loaded.stream().max(Comparator.comparingLong(Environment::getPriority))
                .orElseThrow(NotFoundException::new);
    }

    public abstract long getPriority();

    public abstract Store getStore();

}
