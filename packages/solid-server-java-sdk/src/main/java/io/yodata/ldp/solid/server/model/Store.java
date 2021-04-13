package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

    Optional<String> getData(String path);

    Optional<String> findEntityData(URI entity, String path);

    void saveEntityData(URI entity, String path, JsonElement el);

    Optional<Acl> getDefaultAcl(String path);

    default Optional<Acl> getEntityAcl(Target t) {
        return getEntityAcl(t, true);
    }

    Optional<Acl> getEntityAcl(Target t, boolean recursive);

    void setEntityAcl(Target t, Acl acl);

    List<Subscription> getEntitySubscriptions(URI entity);

    void setEntitySubscriptions(URI entity, List<Subscription> subs);

    void setEntitySubscriptions(URI entity, JsonObject subs);

    List<Subscription> getAllSubscriptions(URI entity);

    /**
     * Get the global subscriptions
     *
     * @return
     */
    Subscriptions getGlobalSubscriptions();

    Subscriptions getSubscriptions(URI entity);

    JsonObject getRawSubscriptions(URI entity);

    Policies getPolicies(URI entity);

    Page getPage(Target t, String by, String from, boolean isFullFormat, boolean isTemporal);

    Optional<SecurityContext> findForApiKey(String apiKey);

    default Then<String> ensureNotExisting(String path) {
        if (exists(path)) {
            throw new RuntimeException("File at " + path + " already exists");
        }

        return new Then<>(path);
    }

    boolean exists(String path);

    Response head(Target target);

    Response get(Target target);

    JsonObject post(Request in);

    JsonObject save(String path, JsonElement content);

    JsonObject save(Request in);

    JsonObject delete(Request in);

    JsonObject delete(String path);

}
