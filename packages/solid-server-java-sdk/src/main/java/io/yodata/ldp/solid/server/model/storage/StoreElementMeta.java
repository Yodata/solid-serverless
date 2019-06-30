package io.yodata.ldp.solid.server.model.storage;

import java.util.Map;

public interface StoreElementMeta {

    String getContentType();

    long getLength();

    Map<String, String> getProperties();

    boolean isLink();

}
