package io.yodata.ldp.solid.server.model.store.fs;

import java.util.List;

public interface FsPage {

    List<String> getElements();

    String getNext();

}
