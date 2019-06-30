package io.yodata.ldp.solid.server.model.storage.memory;

import io.yodata.ldp.solid.server.exception.NotFoundException;
import io.yodata.ldp.solid.server.model.storage.*;

import java.util.*;

public class MemoryStore implements Store {

    private SortedMap<String, StoreElement> entities = new TreeMap<>();

    @Override
    public boolean exists(String path) {
        return entities.containsKey(path);
    }

    @Override
    public Optional<StoreElementMeta> findMeta(String path) {
        return Optional.ofNullable(entities.get(path));
    }

    @Override
    public Optional<StoreElement> findElement(String path) {
        return Optional.ofNullable(entities.get(path));
    }

    @Override
    public void setElement(String path, StoreElement element) {
        entities.put(path, element);
    }

    @Override
    public StoreElementPage listElements(String path, String token, long amount) {
        BasicStoreElementPage page = new BasicStoreElementPage();
        page.setNext(token);

        for (String key : entities.keySet()) {
            if (!key.startsWith(path)) {
                continue;
            }

            if (key.compareTo(token) > 0) {
                page.addElement(key.substring(path.length()));
                page.setNext(key);
            }
            if (page.getElements().size() == amount) {
                break;
            }
        }

        return page;
    }

    @Override
    public void deleteElement(String path) {
        StoreElement el = entities.remove(path);
        if (Objects.isNull(el)) {
            throw new NotFoundException();
        }
    }

}
