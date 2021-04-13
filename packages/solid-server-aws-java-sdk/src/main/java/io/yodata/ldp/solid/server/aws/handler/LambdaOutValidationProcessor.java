package io.yodata.ldp.solid.server.aws.handler;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.config.Configs;
import io.yodata.ldp.solid.server.model.Exchange;
import io.yodata.ldp.solid.server.model.ResponseLogAction;
import io.yodata.ldp.solid.server.model.Store;
import io.yodata.ldp.solid.server.model.processor.OutputValidationProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class LambdaOutValidationProcessor extends LambdaValidationProcessor implements OutputValidationProcessor {

    private static final Logger log = LoggerFactory.getLogger(LambdaOutValidationProcessor.class);

    private final String lambdaName;
    private final AWSLambda lambda;

    public LambdaOutValidationProcessor(Store s) {
        super(s);
        lambdaName = Configs.get().get("aws.lambda.middleware.out");
        DefaultAWSCredentialsProviderChain credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
        lambda = AWSLambdaClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .build();
    }

    protected ResponseLogAction process(Exchange ex) {
        JsonObject processing = new JsonObject();
        processing.addProperty("type", "LambdaProcessing");
        ResponseLogAction result = new ResponseLogAction().addChild(processing);

        if (!StringUtils.startsWith(ex.getResponse().getContentType(), MimeTypes.APPLICATION_JSON)) {
            processing.addProperty("actionStatus", "SkippedActionStatus");
            processing.addProperty("reason", "Not response type of " + MimeTypes.APPLICATION_JSON);
            return result.addChild(processing).withResponse(ex.getResponse());
        }

        processing.addProperty("lambda", lambdaName);

        log.debug("Exchange {}: Response validation: start", ex.getRequest().getId());
        log.debug("Using lambda {}", lambdaName);

        JsonObject exJson = toJson(ex);
        String payload = GsonUtil.toJson(exJson);

        InvokeRequest invokeReq = new InvokeRequest();
        invokeReq.setFunctionName(lambdaName);
        invokeReq.setPayload(payload);

        log.debug("Calling lambda {}", lambdaName);
        InvokeResult invokeRes = lambda.invoke(invokeReq);
        processing.addProperty("status", invokeRes.getStatusCode());
        if (invokeRes.getStatusCode() != 200 || StringUtils.equals("Unhandled", invokeRes.getFunctionError())) {
            throw new RuntimeException("Lambda " + lambdaName + " completed with status " + invokeRes.getStatusCode() + " and/or error " + invokeRes.getFunctionError());
        }
        log.debug("Got reply from lambda {}", lambdaName);

        String p = new String(invokeRes.getPayload().array(), StandardCharsets.UTF_8);
        Exchange exNew = GsonUtil.parse(p, Exchange.class);
        if (Objects.nonNull(exNew.getResponse())) {
            log.debug("Got response object");
            ex.setResponse(exNew.getResponse());
        } else {
            processing.addProperty("hasResponse", false);
            log.debug("DID NOT get a response object, returning pre-call response");
        }

        return result.withResponse(ex.getResponse());
    }

    @Override
    public ResponseLogAction get(Exchange ex) {
        return process(ex);
    }

    @Override
    public ResponseLogAction post(Exchange ex) {
        return process(ex);
    }

    @Override
    public ResponseLogAction put(Exchange ex) {
        return process(ex);
    }

    @Override
    public ResponseLogAction delete(Exchange ex) {
        return process(ex);
    }

}
