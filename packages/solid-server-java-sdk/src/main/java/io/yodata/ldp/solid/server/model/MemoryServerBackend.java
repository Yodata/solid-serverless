package io.yodata.ldp.solid.server.model;

import io.yodata.ldp.solid.server.model.event.EventBus;
import io.yodata.ldp.solid.server.model.processor.InputValidationProcessor;
import io.yodata.ldp.solid.server.model.processor.OutputValidationProcessor;
import io.yodata.ldp.solid.server.model.processor.StubInValidationProcessor;
import io.yodata.ldp.solid.server.model.processor.StubOutValidationProcessor;

public class MemoryServerBackend implements ServerBackend {

    private Store store;
    private EventBus eventBus;
    private InputValidationProcessor inValProc;
    private OutputValidationProcessor outValProc;

    public MemoryServerBackend() {
        store = new MemoryStore();
        eventBus = in -> {
            // no-op, blackhole;
        };
        inValProc = new StubInValidationProcessor();
        outValProc = new StubOutValidationProcessor();
    }

    @Override
    public Store store() {
        return store;
    }

    @Override
    public EventBus eventBus() {
        return eventBus;
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
