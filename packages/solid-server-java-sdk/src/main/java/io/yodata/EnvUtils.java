package io.yodata;

import java.util.Optional;

public class EnvUtils {

    public static String get(String name) {
        return find(name).orElseThrow(() -> new IllegalStateException("Environment variable " + name + " is not set"));
    }

    public static Optional<String> find(String name) {
        return Optional.ofNullable(System.getenv(name));
    }

}
