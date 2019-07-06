package io.yodata.ldp.solid.server.model.store.fs.memory;

import io.yodata.ldp.solid.server.exception.NotFoundException;
import io.yodata.ldp.solid.server.model.store.fs.*;

import java.util.*;

public class MemoryFilesystem implements Filesystem {

    private SortedMap<String, MemoryFsElement> entities = new TreeMap<>();

    @Override
    public boolean exists(String path) {
        return entities.containsKey(path);
    }

    @Override
    public Optional<FsElementMeta> findMeta(String path) {
        return Optional.ofNullable(entities.get(path));
    }

    @Override
    public Optional<FsElement> findElement(String path) {
        return Optional.ofNullable(entities.get(path));
    }

    @Override
    public void setElement(String path, FsElement element) {
        entities.put(path, MemoryFsElement.fromAnother(element));
    }

    @Override
    public FsPage listElements(String path, String token, long amount) {
        BasicFsPage page = new BasicFsPage();
        page.setNext(token);

        for (String key : entities.keySet()) {
            if (!key.startsWith(path)) {
                continue;
            }

            key = key.substring(path.length());
            String[] split = key.split("/");
            if (split.length > 1) {
                key = split[0] + "/";
            }

            if (key.compareTo(token) > 0 && !page.getElements().contains(key)) {
                page.addElement(key);
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
        FsElement el = entities.remove(path);
        if (Objects.isNull(el)) {
            throw new NotFoundException();
        }
    }

}
