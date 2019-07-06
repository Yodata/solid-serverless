package io.yodata.ldp.solid.server.model.store.fs.memory;

import io.yodata.ldp.solid.server.model.store.fs.FsElement;
import io.yodata.ldp.solid.server.model.store.fs.FsElementMeta;

import java.util.HashMap;
import java.util.Map;

public class MemoryFsElementMeta implements FsElementMeta {

    protected String contentType;
    protected long length;
    protected Map<String, String> properties;
    protected boolean isLink;

    public MemoryFsElementMeta() {
        properties = new HashMap<>();
        isLink = false;
    }

    public MemoryFsElementMeta(FsElementMeta meta) {
        init(meta);
    }

    protected void init(FsElementMeta meta) {
        this.contentType = meta.getContentType();
        this.length = meta.getLength();
        this.properties = new HashMap<>(meta.getProperties());
        this.isLink = meta.isLink();
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public boolean isLink() {
        return isLink;
    }

}
