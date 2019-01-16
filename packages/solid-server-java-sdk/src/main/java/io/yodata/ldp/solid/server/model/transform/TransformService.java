package io.yodata.ldp.solid.server.model.transform;

import com.google.gson.JsonObject;

public interface TransformService {

    JsonObject transform(TransformMessage message);

}
