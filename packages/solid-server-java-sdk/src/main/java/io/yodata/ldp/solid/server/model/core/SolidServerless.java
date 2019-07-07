package io.yodata.ldp.solid.server.model.core;

import io.yodata.ldp.solid.server.model.Environment;
import io.yodata.ldp.solid.server.model.SolidPod;
import io.yodata.ldp.solid.server.model.SolidServer;

public class SolidServerless implements SolidServer {

    private final Environment env;

    public SolidServerless() {
        this(Environment.get());
    }

    public SolidServerless(Environment env) {
        this.env = env;
    }

    @Override
    public SolidPod forPod(String id) {
        return null;
    }

}
