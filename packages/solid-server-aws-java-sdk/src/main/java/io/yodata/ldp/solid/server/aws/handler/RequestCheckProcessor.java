package io.yodata.ldp.solid.server.aws.handler;

import io.yodata.ldp.solid.server.aws.Configs;

public class RequestCheckProcessor extends LambdaInValidationProcessor {

    private final String name;

    public RequestCheckProcessor() {
        name = Configs.get().get("aws.lambda.middleware.in");
    }

    @Override
    protected String getLambdaName() {
        return name;
    }

}
