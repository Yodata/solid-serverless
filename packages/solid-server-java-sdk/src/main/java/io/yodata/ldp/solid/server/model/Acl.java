package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.*;

public class Acl {

    public static Acl forNone() {
        return new Acl();
    }

    public static Acl forDefaultAllowed() {
        Acl acl = new Acl();
        acl.getDef().setModes(Collections.singletonList(AclMode.Append));
        return acl;
    }

    public static Acl forAdmin() {
        Acl acl = new Acl();
        acl.getDef().setModes(Arrays.asList(AclMode.values()));
        return acl;
    }

    public static class Entry {

        private List<AclMode> modes = new ArrayList<>();
        private JsonObject scope = new JsonObject();

        public List<AclMode> getModes() {
            return modes;
        }

        public void setModes(List<AclMode> modes) {
            this.modes = modes;
        }

        public void addMode(AclMode mode) {
            modes.add(mode);
        }

        public JsonObject getScope() {
            return scope;
        }

        public void setScope(JsonObject scope) {
            this.scope = scope;
        }

    }

    @SerializedName("default")
    private Entry def = new Entry();
    private Map<String, Entry> entities = new HashMap<>();
    private Map<String, Entry> patterns = new HashMap<>();

    public Entry getDef() {
        return def;
    }

    public void setDef(Entry def) {
        this.def = def;
    }

    public Map<String, Entry> getEntities() {
        return entities;
    }

    public void setEntities(Map<String, Entry> entities) {
        this.entities = entities;
    }

    public Optional<Entry> getEntity(String id) {
        return Optional.ofNullable(entities.get(id));
    }

    public Entry computeEntity(String id) {
        return getEntity(id).orElse(def);
    }

    public Map<String, Entry> getPatterns() {
        return patterns;
    }

    public void setPatterns(Map<String, Entry> patterns) {
        this.patterns = patterns;
    }

}
