package io.yodata.ldp.solid.server.model.storage;

import io.yodata.ldp.solid.server.exception.NotFoundException;

import java.util.Optional;

public interface Store {

    boolean exists(String path);

    Optional<StoreElementMeta> findMeta(String path);

    Optional<StoreElement> findElement(String path);

    default StoreElementMeta getMeta(String path) {
        return findMeta(path).orElseThrow(NotFoundException::new);
    }

    default StoreElement getElement(String path) {
        return findElement(path).orElseThrow(NotFoundException::new);
    }

    void setElement(String path, StoreElement element);

    StoreElementPage listElements(String path, String token, long amount);

    void deleteElement(String path);

}
