package io.yodata.ldp.solid.server.model.transform;

import com.google.gson.JsonObject;

public class EchoTransformService implements TransformService {

    @Override
    public JsonObject transform(TransformMessage message) {
        return message.getObject();
    }

}
