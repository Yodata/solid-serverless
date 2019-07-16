/*
 * Copyright 2018 YoData, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.yodata.ldp.solid.server.model.security;

import com.google.gson.annotations.SerializedName;

import java.util.*;

public class Acl {

    public static Acl forNone() {
        return new Acl();
    }

    public static Acl forInit() {
        Entry entry = new Entry();
        entry.setModes(Arrays.asList(AclMode.Read, AclMode.Write, AclMode.Append, AclMode.Control));
        Acl acl = new Acl();
        acl.getPatterns().put("%BASE_URL%/profile/card#me", entry);
        return acl;
    }

    public static Acl forDefaultAllowed() {
        Acl acl = new Acl();
        acl.getDef().setModes(Collections.singletonList(AclMode.Append));
        return acl;
    }

    public static Acl forAdmin() {
        Acl acl = new Acl();
        acl.getDef().setModes(Arrays.asList(AclMode.Read, AclMode.Write, AclMode.Append, AclMode.Control));
        return acl;
    }

    public static class Entry {

        public static Entry copy(Entry source) {
            Entry dest = new Entry();
            dest.modes = new ArrayList<>(source.modes);
            dest.scope = new HashSet<>(source.scope);
            return dest;
        }

        private List<AclMode> modes = new ArrayList<>();
        private Set<String> scope = new HashSet<>();

        public List<AclMode> getModes() {
            return modes;
        }

        public void setModes(List<AclMode> modes) {
            this.modes = modes;
        }

        public void addMode(AclMode mode) {
            modes.add(mode);
        }

        public Set<String> getScope() {
            return scope;
        }

        public void setScope(Collection<String> scope) {
            this.scope = new HashSet<>(scope);
        }

    }

    @SerializedName("default")
    private Entry def = new Entry();
    private Map<String, Entry> entities = new HashMap<>();
    private Map<String, Entry> patterns = new HashMap<>();
    private boolean isFinal;

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
        return getEntity(id).orElse(Entry.copy(def));
    }

    public Map<String, Entry> getPatterns() {
        return patterns;
    }

    public void setPatterns(Map<String, Entry> patterns) {
        this.patterns = patterns;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

}
