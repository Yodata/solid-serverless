package io.yodata.ldp.solid.server.model;

import io.yodata.ldp.solid.server.exception.NotFoundException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryStore extends EntityBasedStore {

    private static class Entity {

        private String contentType;
        private byte[] data;

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

    }

    private Map<String, Entity> entities = new ConcurrentHashMap<>();

    @Override
    protected String getTsPrefix(String from, String namespace) {
        return null;
    }

    @Override
    protected void save(String contentType, byte[] bytes, String path, Map<String, String> meta) {

    }

    @Override
    public void save(String contentType, byte[] bytes, String path) {
        Entity e = new Entity();
        e.setContentType(contentType);
        e.setData(bytes);
        entities.put(path, e);
    }

    @Override
    public void link(String linkTargetPath, String linkPath) {

    }

    @Override
    protected Optional<String> getData(String path) {
        return Optional.ofNullable(entities.get(path)).map(d -> new String(d.getData(), StandardCharsets.UTF_8));
    }

    @Override
    protected Optional<Map<String, String>> findMeta(String path) {
        return Optional.empty();
    }

    @Override
    public Optional<String> findEntityData(URI entity, String path) {
        return getData(buildEntityPath(entity, path));
    }

    @Override
    public Page getPage(Target t, String from, String by, boolean isFullFormat, boolean isTemporal) {
        return new Page();
    }

    @Override
    public boolean exists(String path) {
        return entities.containsKey(path);
    }

    @Override
    public Response head(Target target) {
        Entity entity = entities.get(buildEntityPath(target.getId()));
        if (Objects.isNull(entity)) {
            throw new NotFoundException();
        }

        Response r = new Response();
        r.getHeaders().put("Content-Type", entity.getContentType());
        r.getHeaders().put("Content-Length", Long.toString(entity.getData().length));

        return r;
    }

    @Override
    public Response get(Target target) {
        Entity entity = entities.get(buildEntityPath(target.getId()));
        if (Objects.isNull(entity)) {
            throw new NotFoundException();
        }

        Response r = new Response();
        r.getHeaders().put("Content-Type", entity.getContentType());
        r.getHeaders().put("Content-Length", Long.toString(entity.getData().length));
        r.setBody(entity.getData());

        return r;
    }

    @Override
    public void delete(String path) {
        entities.remove(path);
    }

}
