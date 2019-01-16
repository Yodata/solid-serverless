package io.yodata.ldp.solid.server.aws.handler;

import io.yodata.EnvUtils;

public class RequestCheckProcessor extends LambdaInValidationProcessor {

    private final String name;

    public RequestCheckProcessor() {
        name = EnvUtils.get("IN_MIDDLEWARE_LAMBDA");
    }

    @Override
    protected String getLambdaName() {
        return name;
    }

}
