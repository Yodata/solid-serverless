package io.yodata.ldp.solid.server.model.storage.memory;

import io.yodata.ldp.solid.server.model.storage.StoreElementMeta;

import java.util.HashMap;
import java.util.Map;

public class MemoryStoreElementMeta implements StoreElementMeta {

    protected String contentType;
    protected long length;
    protected Map<String, String> properties;
    protected boolean isLink;

    public MemoryStoreElementMeta() {
        properties = new HashMap<>();
        isLink = false;
    }

    public MemoryStoreElementMeta(StoreElementMeta meta) {
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
