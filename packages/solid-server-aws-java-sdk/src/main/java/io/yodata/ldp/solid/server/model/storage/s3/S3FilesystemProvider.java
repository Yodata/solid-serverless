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

package io.yodata.ldp.solid.server.model.storage.s3;

import io.yodata.EnvUtils;
import io.yodata.ldp.solid.server.model.store.fs.Filesystem;
import io.yodata.ldp.solid.server.model.store.fs.FilesystemProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class S3FilesystemProvider implements FilesystemProvider {

    @Override
    public Optional<Filesystem> build() {
        String s3Bucket = EnvUtils.find("S3_BUCKET_NAMES")
                .orElseGet(() -> EnvUtils.find("S3_BUCKET_NAME").orElse(""));

        if (StringUtils.isBlank(s3Bucket)) {
            return Optional.empty();
        }

        return Optional.of(new S3Filesystem(s3Bucket));
    }

}
