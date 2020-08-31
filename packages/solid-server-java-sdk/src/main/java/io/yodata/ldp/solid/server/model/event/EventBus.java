package io.yodata.ldp.solid.server.model.event;

import io.yodata.ldp.solid.server.model.Request;

public interface EventBus {

    void sendStoreEvent(Request in);

}
