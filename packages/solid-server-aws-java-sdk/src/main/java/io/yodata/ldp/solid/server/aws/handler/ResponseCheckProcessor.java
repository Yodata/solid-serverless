package io.yodata.ldp.solid.server.aws.handler;

import io.yodata.EnvUtils;

public class ResponseCheckProcessor extends LambdaOutValidationProcessor {

    private final String name;

    public ResponseCheckProcessor() {
        name = EnvUtils.get("OUT_MIDDLEWARE_LAMBDA");
    }

    @Override
    protected String getLambdaName() {
        return name;
    }

}
