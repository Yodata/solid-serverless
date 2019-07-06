package io.yodata.ldp.solid.server.model.store.fs;

import io.yodata.ldp.solid.server.exception.NotFoundException;

import java.util.Optional;

public interface Filesystem {

    boolean exists(String path);

    Optional<FsElementMeta> findMeta(String path);

    Optional<FsElement> findElement(String path);

    default FsElementMeta getMeta(String path) {
        return findMeta(path).orElseThrow(NotFoundException::new);
    }

    default FsElement getElement(String path) {
        return findElement(path).orElseThrow(NotFoundException::new);
    }

    void setElement(String path, FsElement element);

    FsPage listElements(String path, String token, long amount);

    void deleteElement(String path);

}
