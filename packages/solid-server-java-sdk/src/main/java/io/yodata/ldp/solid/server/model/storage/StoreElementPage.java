package io.yodata.ldp.solid.server.model.storage;

import java.util.List;

public interface StoreElementPage {

    List<String> getElements();

    String getNext();

}
