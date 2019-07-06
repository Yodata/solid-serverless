package io.yodata.ldp.solid.server.model.store.fs.memory;

import com.google.gson.JsonElement;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.model.store.fs.FsElement;
import io.yodata.ldp.solid.server.model.store.fs.FsElementMeta;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MemoryFsElement extends MemoryFsElementMeta implements FsElement {

    public static FsElement getEmpty() {
        MemoryFsElement element = new MemoryFsElement();
        element.length = 0;
        element.contentType = "application/octet-stream";
        element.data = new byte[0];
        return element;
    }

    public static FsElement forJson(JsonElement el) {
        return forJson(GsonUtil.toJson(el));
    }

    public static FsElement forJson(String json) {
        MemoryFsElement element = new MemoryFsElement();
        element.contentType = "application/json";
        element.data = json.getBytes(StandardCharsets.UTF_8);
        element.length = element.data.length;
        return element;
    }

    public static MemoryFsElement fromAnother(FsElement element) {
        try {
            MemoryFsElement memEl = new MemoryFsElement();
            memEl.init(element.getMeta());
            memEl.data = IOUtils.toByteArray(element.getData());
            element.getData().close();
            return memEl;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] data;

    @Override
    public FsElementMeta getMeta() {
        return this;
    }

    @Override
    public InputStream getData() {
        return new ByteArrayInputStream(data);
    }

}
