package io.yodata.ldp.solid.server.model.store.fs;

import java.util.Map;

public interface FsElementMeta {

    String getContentType();

    long getLength();

    Map<String, String> getProperties();

    boolean isLink();

}
