package io.yodata.ldp.solid.server.aws.handler;

import io.yodata.ldp.solid.server.aws.Configs;

public class ResponseCheckProcessor extends LambdaOutValidationProcessor {

    private final String name;

    public ResponseCheckProcessor() {
        name = Configs.get().get("aws.lambda.middleware.out");
    }

    @Override
    protected String getLambdaName() {
        return name;
    }

}
