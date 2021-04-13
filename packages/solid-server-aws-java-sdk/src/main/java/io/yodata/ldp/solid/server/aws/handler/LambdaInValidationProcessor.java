package io.yodata.ldp.solid.server.aws.handler;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.config.Configs;
import io.yodata.ldp.solid.server.model.Exchange;
import io.yodata.ldp.solid.server.model.Store;
import io.yodata.ldp.solid.server.model.processor.InputValidationProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class LambdaInValidationProcessor extends LambdaValidationProcessor implements InputValidationProcessor {

    private static final Logger log = LoggerFactory.getLogger(LambdaInValidationProcessor.class);

    private final String lambdaName;
    private final AWSLambda lambda;

    public LambdaInValidationProcessor(Store s) {
        super(s);
        lambdaName = Configs.get().get("aws.lambda.middleware.in");
        lambda = AWSLambdaClientBuilder.defaultClient();
    }

    protected JsonObject process(Exchange ex) {
        JsonObject result = new JsonObject();

        if (!StringUtils.startsWith(ex.getRequest().getContentType().orElse(""), MimeTypes.APPLICATION_JSON)) {
            result.addProperty("actionStatus", "SkippedActionStatus");
            result.addProperty("reason", "Request is not of Content-Type " + MimeTypes.APPLICATION_JSON + ", skipping");
            return result;
        }

        result.addProperty("lambda", lambdaName);

        log.debug("Request {} validation: start", ex.getRequest().getId());
        log.debug("Using lambda {}", lambdaName);

        JsonObject exJson = toJson(ex);
        String payload = GsonUtil.toJson(exJson);
        InvokeRequest invokeReq = new InvokeRequest();
        invokeReq.setFunctionName(lambdaName);
        invokeReq.setPayload(payload);

        log.debug("Calling lambda {}", lambdaName);
        InvokeResult invokeRes = lambda.invoke(invokeReq);
        result.addProperty("status", invokeRes.getStatusCode());
        if (invokeRes.getStatusCode() != 200 || StringUtils.equals("Unhandled", invokeRes.getFunctionError())) {
            throw new RuntimeException("Lambda " + lambdaName + " completed with status " + invokeRes.getStatusCode() + " and/or error " + invokeRes.getFunctionError());
        }
        log.debug("Got reply from lambda {}", lambdaName);

        byte[] invokeResBody = invokeRes.getPayload().array();
        JsonObject exProcessed = GsonUtil.parseObj(invokeResBody);
        GsonUtil.findObj(exProcessed, "response").ifPresent(resJson -> log.debug("Got response object"));
        Exchange exNew = GsonUtil.parse(invokeResBody, Exchange.class);

        if (Objects.nonNull(exNew.getRequest())) {
            ex.setRequest(exNew.getRequest());
        }
        ex.setResponse(exNew.getResponse());

        result.addProperty("actionStatus", "CompletedActionStatus");
        return result;
    }

    @Override
    public JsonObject get(Exchange ex) {
        return process(ex);
    }

    @Override
    public JsonObject post(Exchange ex) {
        return process(ex);
    }

    @Override
    public JsonObject put(Exchange ex) {
        return process(ex);
    }

    @Override
    public JsonObject delete(Exchange ex) {
        return process(ex);
    }

}
