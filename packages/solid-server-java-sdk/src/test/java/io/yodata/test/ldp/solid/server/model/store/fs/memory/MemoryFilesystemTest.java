package io.yodata.test.ldp.solid.server.model.store.fs.memory;

import io.yodata.ldp.solid.server.model.store.fs.Filesystem;
import io.yodata.ldp.solid.server.model.store.fs.memory.MemoryFilesystem;
import io.yodata.test.ldp.solid.server.model.store.fs.FilesystemTest;

public class MemoryFilesystemTest extends FilesystemTest {

    @Override
    protected Filesystem create() {
        return new MemoryFilesystem();
    }

}
