package io.yodata.ldp.solid.server.subscription.pusher;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.config.Configs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LambdaPusher {

    private static final Logger log = LoggerFactory.getLogger(LambdaPusher.class);

    private final String lName = Configs.get().get("aws.lambda.pusher.name");
    private final AWSLambda lambda = AWSLambdaClientBuilder.defaultClient();

    public void send(JsonObject data, String targetRaw, JsonObject config) {
        JsonObject payload = new JsonObject();
        payload.add("object", data);
        payload.addProperty("target", targetRaw);
        payload.add("config", config);

        InvokeRequest i = new InvokeRequest();
        i.setFunctionName(lName);
        i.setPayload(GsonUtil.toJson(payload));
        InvokeResult r = lambda.invoke(i);
        int statusCode = r.getStatusCode();
        String functionError = r.getFunctionError();
        if (statusCode != 200 || StringUtils.isNotEmpty(functionError)) {
            throw new RuntimeException("Error when calling lambda " + lName + " | Status code: " + statusCode + " | Error: " + functionError);
        }
        log.info("Lambda {} was successfully called", lName);
    }

}
