package io.yodata.ldp.solid.server.model.storage;

import java.io.InputStream;

public interface StoreElement extends StoreElementMeta {

    InputStream getData();

}
