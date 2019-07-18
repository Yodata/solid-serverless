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

package io.yodata.ldp.solid.server.model.store.fs.local;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.exception.NotFoundException;
import io.yodata.ldp.solid.server.model.store.fs.*;
import io.yodata.ldp.solid.server.model.store.fs.memory.MemoryFsElementMeta;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;

public class LocalFilesystem implements Filesystem {

    private static final Logger log = LoggerFactory.getLogger(LocalFilesystem.class);

    private Path baseData;
    private Path baseMeta;

    public LocalFilesystem(String location) {
        baseMeta = initPath(Paths.get(location, "meta").toAbsolutePath());
        baseData = initPath(Paths.get(location, "data").toAbsolutePath());
    }

    private Path initPath(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!Files.isDirectory(path)) {
            throw new RuntimeException(path + " is not a directory");
        }

        if (!Files.isWritable(path)) {
            throw new RuntimeException(path + " is read-only");
        }

        return path;
    }

    private String sanitise(String path) {
        if (StringUtils.startsWith(path, "/")) {
            path = path.substring(1);
        }

        return path;
    }

    private Path resolveMeta(String path) {
        return baseMeta.resolve(sanitise(path));
    }

    private Path resolveData(String path) {
        return baseData.resolve(sanitise(path));
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(resolveData(path));
    }

    @Override
    public Optional<FsElementMeta> findMeta(String path) {
        Path metaPath = resolveMeta(path);

        JsonObject data;
        try (InputStream is = Files.newInputStream(metaPath, StandardOpenOption.READ)) {
            data = GsonUtil.parseObj(is);

            BasicMeta meta = new BasicMeta();
            meta.setContentType(GsonUtil.getStringOrNull(data, "contentType"));
            meta.setLength(GsonUtil.getLong(data, "length"));
            meta.setProperties(new HashMap<>());
            meta.setLink(GsonUtil.findLong(data, "isLink").orElse(0L) > 0L);

            return Optional.of(meta);
        } catch (NoSuchFileException e) {
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<FsElement> findElement(String path) {
        FsElementMeta meta = findMeta(path).orElseGet(MemoryFsElementMeta::new);

        try {
            return Optional.of(new BasicElement(meta, Files.newInputStream(resolveData(path), StandardOpenOption.READ)));
        } catch (NoSuchFileException e) {
            log.warn("Inconsistency for {}: Meta file was found, but data file is not", path);
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void setElement(String path, FsElement element) {
        Path metaPath = resolveMeta(path);
        Path dataPath = resolveData(path);

        try {
            Files.createDirectories(metaPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Files.createDirectories(dataPath.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        JsonObject meta = new JsonObject();
        meta.addProperty("contentType", element.getMeta().getContentType());
        meta.addProperty("length", element.getMeta().getLength());
        meta.add("properties", GsonUtil.makeObj(element.getMeta().getProperties()));

        try (OutputStream os = Files.newOutputStream(dataPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            IOUtils.copyLarge(element.getData(), os, 0, element.getMeta().getLength());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (OutputStream os = Files.newOutputStream(metaPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            IOUtils.write(GsonUtil.toJsonBytes(meta), os);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public FsPage listElements(String path, String token, long amount) {
        BasicFsPage page = new BasicFsPage();
        page.setNext(token);

        try {
            Path metaPath = resolveMeta(path);
            Files.list(metaPath)
                    .sorted(Comparator.naturalOrder())
                    .map(p -> p.getFileName().toString())
                    .filter(p -> p.compareTo(token) > 0)
                    .forEachOrdered(p -> {
                        if (page.getElements().size() < amount) {
                            page.addElement(p);
                            page.setNext(p);
                        }
                    });

            return page;
        } catch (NoSuchFileException e) {
            return page;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void deleteElement(String path) {
        try {
            Files.delete(resolveMeta(path));
            if (!Files.deleteIfExists(resolveData(path))) {
                log.warn("Inconsistency for {}: Meta file was deleted, but data file does not exist", path);
            }
        } catch (NoSuchFileException e) {
            throw new NotFoundException();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
