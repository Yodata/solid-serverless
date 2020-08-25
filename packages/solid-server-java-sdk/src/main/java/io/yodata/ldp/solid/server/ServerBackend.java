package io.yodata.ldp.solid.server;

import io.yodata.ldp.solid.server.model.Store;
import io.yodata.ldp.solid.server.model.event.EventBus;
import io.yodata.ldp.solid.server.model.processor.InputValidationProcessor;
import io.yodata.ldp.solid.server.model.processor.OutputValidationProcessor;

public interface ServerBackend {

    Store store();

    EventBus eventBus();

    InputValidationProcessor inValProc();

    OutputValidationProcessor outValProc();

}
