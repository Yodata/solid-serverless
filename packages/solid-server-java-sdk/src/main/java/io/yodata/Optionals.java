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

package io.yodata;

import java.util.Optional;
import java.util.function.Supplier;

public class Optionals {

    @SafeVarargs
    public static <T> Optional<T> get(Supplier<Optional<T>>... ops) {
        for (Supplier<Optional<T>> op : ops) {
            Optional<T> v = op.get();
            if (v.isPresent()) {
                return v;
            }
        }

        return Optional.empty();
    }

}
