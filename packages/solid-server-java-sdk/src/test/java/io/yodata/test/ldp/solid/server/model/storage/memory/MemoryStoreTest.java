package io.yodata.test.ldp.solid.server.model.storage.memory;

import io.yodata.ldp.solid.server.model.storage.Store;
import io.yodata.ldp.solid.server.model.storage.memory.MemoryStore;
import io.yodata.test.ldp.solid.server.model.storage.StoreTest;

public class MemoryStoreTest extends StoreTest {

    @Override
    protected Store create() {
        return new MemoryStore();
    }

}
