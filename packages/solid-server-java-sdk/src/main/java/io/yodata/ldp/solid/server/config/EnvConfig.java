package io.yodata.ldp.solid.server.config;

import java.util.Optional;

public class EnvConfig implements Config {

    private static final EnvConfig instance = new EnvConfig();

    public static EnvConfig get() {
        return instance;
    }

    @Override
    @Deprecated
    public Optional<String> find(String name) {
        String v = System.getenv(name.replace(".", "_").toUpperCase());
        return Optional.ofNullable(v);
    }

}
