package io.yodata.ldp.solid.server.aws.handler;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.aws.Configs;
import io.yodata.ldp.solid.server.model.Exchange;
import io.yodata.ldp.solid.server.model.processor.InputValidationProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class LambdaInValidationProcessor extends LambdaValidationProcessor implements InputValidationProcessor {

    private static final Logger log = LoggerFactory.getLogger(LambdaInValidationProcessor.class);

    private final String lambdaName;
    private final AWSLambda lambda;

    public LambdaInValidationProcessor() {
        lambdaName = Configs.get().get("aws.lambda.middleware.in");
        lambda = AWSLambdaClientBuilder.defaultClient();
    }

    protected void process(Exchange ex) {
        if (!StringUtils.startsWith(ex.getRequest().getContentType().orElse(""), MimeTypes.APPLICATION_JSON)) {
            log.info("Request is not of Content-Type {}, skipping middleware", MimeTypes.APPLICATION_JSON);
            return;
        }

        log.debug("Request {} validation: start", ex.getRequest().getId());
        log.debug("Using lambda {}", lambdaName);

        JsonObject exJson = toJson(ex);
        String payload = GsonUtil.toJson(exJson);
        InvokeRequest invokeReq = new InvokeRequest();
        invokeReq.setFunctionName(lambdaName);
        invokeReq.setPayload(payload);

        log.debug("Calling lambda {}", lambdaName);
        InvokeResult invokeRes = lambda.invoke(invokeReq);
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

        log.info("Request {} validation: end", ex.getRequest().getId());
    }

    @Override
    public void get(Exchange ex) {
        process(ex);
    }

    @Override
    public void post(Exchange ex) {
        process(ex);
    }

    @Override
    public void put(Exchange ex) {
        process(ex);
    }

    @Override
    public void delete(Exchange ex) {
        process(ex);
    }

}
