package io.yodata.ldp.solid.server.model.store.fs;

import java.io.InputStream;

public class BasicElement implements FsElement {

    private FsElementMeta meta;
    private InputStream data;

    public BasicElement(FsElementMeta meta, InputStream data) {
        this.meta = meta;
        this.data = data;
    }

    @Override
    public FsElementMeta getMeta() {
        return meta;
    }

    @Override
    public InputStream getData() {
        return data;
    }

}
