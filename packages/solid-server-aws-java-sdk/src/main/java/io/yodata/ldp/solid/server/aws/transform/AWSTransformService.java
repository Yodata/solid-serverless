package io.yodata.ldp.solid.server.aws.transform;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.JsonObject;
import io.yodata.EnvUtils;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.model.transform.TransformMessage;
import io.yodata.ldp.solid.server.model.transform.TransformService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AWSTransformService implements TransformService {

    private AWSLambda lambda;
    private String lName;

    public AWSTransformService() {
        lName = EnvUtils.find("TRANSFORM_AWS_LAMBDA_NAME").orElse("create-view");
        lambda = AWSLambdaClientBuilder.defaultClient();
    }

    @Override
    public JsonObject transform(TransformMessage message) {
        String payload = GsonUtil.toJson(message);

        InvokeRequest req = new InvokeRequest();
        req.setFunctionName(lName);
        req.setPayload(payload);
        InvokeResult res = lambda.invoke(req);
        int sc = res.getStatusCode();
        JsonObject body = GsonUtil.parseObj(res.getPayload().array());
        if (sc != 200 || StringUtils.isNotEmpty(res.getFunctionError())) {
            throw new RuntimeException("Normalization service returned an error: " + GsonUtil.toJson(body));
        }

        return body;
    }

}
