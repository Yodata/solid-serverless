package io.yodata.ldp.solid.server.config;

import java.util.Optional;

public interface Config {

    default String get(String name) {
        return find(name).orElseThrow(() -> new IllegalArgumentException("No configuration for " + name));
    }

    default String findOrBlank(String name) {
        return find(name).orElse("");
    }

    Optional<String> find(String name);

}
