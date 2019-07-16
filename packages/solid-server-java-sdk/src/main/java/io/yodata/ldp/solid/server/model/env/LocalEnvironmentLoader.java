package io.yodata.ldp.solid.server.model.env;

import java.util.Optional;

public class LocalEnvironmentLoader implements EnvironmentLoader {

    @Override
    public Optional<Environment> load() {
        return Optional.of(new LocalEnvironment());
    }

}
