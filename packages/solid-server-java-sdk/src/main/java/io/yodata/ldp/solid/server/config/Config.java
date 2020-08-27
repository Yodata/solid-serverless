package io.yodata.ldp.solid.server.config;

import java.util.Optional;

public interface Config {

    default String get(String name) {
        return find(name).orElseThrow(() -> new IllegalArgumentException("No configuration for " + name));
    }

    default String findOrBlank(String name) {
        return findOrDefault(name, "");
    }

    default String findOrDefault(String name, String defaultValue) {
        return find(name).orElse(defaultValue);
    }

    Optional<String> find(String name);

}
