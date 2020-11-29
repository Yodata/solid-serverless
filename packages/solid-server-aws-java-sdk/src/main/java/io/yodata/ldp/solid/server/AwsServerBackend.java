package io.yodata.ldp.solid.server;

import io.yodata.ldp.solid.server.aws.handler.LambdaInValidationProcessor;
import io.yodata.ldp.solid.server.aws.handler.LambdaOutValidationProcessor;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.model.ServerBackend;
import io.yodata.ldp.solid.server.model.Store;
import io.yodata.ldp.solid.server.model.event.EventBus;
import io.yodata.ldp.solid.server.model.processor.InputValidationProcessor;
import io.yodata.ldp.solid.server.model.processor.OutputValidationProcessor;
import io.yodata.ldp.solid.server.notification.AwsSnsEventBus;

public class AwsServerBackend implements ServerBackend {

    private final AwsSnsEventBus evBus;
    private final LambdaInValidationProcessor inValProc;
    private final LambdaOutValidationProcessor outValProc;

    public AwsServerBackend() {
        evBus = new AwsSnsEventBus();
        inValProc = new LambdaInValidationProcessor(S3Store.getDefault());
        outValProc = new LambdaOutValidationProcessor(S3Store.getDefault());
    }

    @Override
    public Store store() {
        return S3Store.getDefault();
    }

    @Override
    public EventBus eventBus() {
        return evBus;
    }

    @Override
    public InputValidationProcessor inValProc() {
        return inValProc;
    }

    @Override
    public OutputValidationProcessor outValProc() {
        return outValProc;
    }

}
