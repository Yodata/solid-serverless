package io.yodata.ldp.solid.server.config;

import java.util.HashMap;
import java.util.Optional;

public class MemoryConfig implements Config {

    private final HashMap<String, String> values = new HashMap<>();

    public void set(String key, String value) {
        values.put(key, value);
    }

    @Override
    public Optional<String> find(String name) {
        return Optional.ofNullable(values.get(name));
    }

}
