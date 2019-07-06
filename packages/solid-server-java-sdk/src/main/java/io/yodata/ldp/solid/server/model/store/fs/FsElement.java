package io.yodata.ldp.solid.server.model.store.fs;

import java.io.InputStream;

public interface FsElement {

    FsElementMeta getMeta();

    InputStream getData();

}
