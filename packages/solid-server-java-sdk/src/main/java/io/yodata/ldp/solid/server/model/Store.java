package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonElement;
import io.yodata.ldp.solid.server.model.transform.Policies;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface Store {

    class Then<T> {

        private T value;

        public Then(T value) {
            this.value = value;
        }

        public Then then(Consumer<T> c) {
            c.accept(value);
            return this;
        }

    }

    Optional<String> findEntityData(URI entity, String path);

    boolean saveEntityData(URI entity, String path, JsonElement el);

    Optional<Acl> getDefaultAcl(String path);

    default Optional<Acl> getEntityAcl(Target t) {
        return getEntityAcl(t, true);
    }

    Optional<Acl> getEntityAcl(Target t, boolean recursive);

    void setEntityAcl(Target t, Acl acl);

    List<Subscription> getEntitySubscriptions(URI entity);

    void setEntitySubscriptions(URI entity, List<Subscription> subs);

    List<Subscription> getSubscriptions(URI entity);

    Policies getPolicies(URI entity);

    Page getPage(Target t, String from, String by, boolean isFullFormat);

    Optional<SecurityContext> findForApiKey(String apiKey);

    default Then<String> ensureNotExisting(String path) {
        if (exists(path)) {
            throw new RuntimeException("File at " + path + " already exists");
        }

        return new Then<>(path);
    }

    boolean exists(String path);

    Response get(Target target);

    void post(Request in);

    void save(String path, JsonElement content);

    boolean save(Request in);

    void delete(Request in);

    void delete(String path);

}
