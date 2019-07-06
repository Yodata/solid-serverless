package io.yodata.test.ldp.solid.server.model.store.fs.local;

import io.yodata.ldp.solid.server.model.store.fs.Filesystem;
import io.yodata.ldp.solid.server.model.store.fs.local.LocalFilesystem;
import io.yodata.test.ldp.solid.server.model.store.fs.FilesystemTest;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class LocalFilesystemTest extends FilesystemTest {

    @Override
    protected Filesystem create() {
        try {
            return new LocalFilesystem(Files.createTempDirectory("solid-serverless-test-" + UUID.randomUUID().toString()).toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
