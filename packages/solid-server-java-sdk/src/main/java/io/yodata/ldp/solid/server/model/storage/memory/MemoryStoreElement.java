package io.yodata.ldp.solid.server.model.storage.memory;

import com.google.gson.JsonElement;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.model.storage.StoreElement;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MemoryStoreElement extends MemoryStoreElementMeta implements StoreElement {

    public static StoreElement getEmpty() {
        MemoryStoreElement element = new MemoryStoreElement();
        element.length = 0;
        element.contentType = "application/octet-stream";
        element.data = new byte[0];
        return element;
    }

    public static StoreElement forJson(JsonElement el) {
        return forJson(GsonUtil.toJson(el));
    }

    public static StoreElement forJson(String json) {
        MemoryStoreElement element = new MemoryStoreElement();
        element.contentType = "application/json";
        element.data = json.getBytes(StandardCharsets.UTF_8);
        element.length = element.data.length;
        return element;
    }

    private byte[] data;

    @Override
    public InputStream getData() {
        return new ByteArrayInputStream(data);
    }

}
