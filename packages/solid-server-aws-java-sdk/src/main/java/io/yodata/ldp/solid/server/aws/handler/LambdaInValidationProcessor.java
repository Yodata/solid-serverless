package io.yodata.ldp.solid.server.aws.handler;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.model.Exchange;
import io.yodata.ldp.solid.server.model.processor.InputValidationProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class LambdaInValidationProcessor extends LambdaValidationProcessor implements InputValidationProcessor {

    private final Logger log = LoggerFactory.getLogger(LambdaInValidationProcessor.class);

    private final AWSLambda lambda;

    protected abstract String getLambdaName();

    public LambdaInValidationProcessor() {
        this.lambda = AWSLambdaClientBuilder.defaultClient();
    }

    protected void process(Exchange ex) {
        if (!StringUtils.startsWith(ex.getRequest().getContentType().orElse(""), MimeTypes.APPLICATION_JSON)) {
            log.info("Request is not of Content-Type {}, skipping middleware", MimeTypes.APPLICATION_JSON);
            return;
        }

        log.info("Request {} validation: start", ex.getRequest().getId());
        log.info("Using lambda {}", getLambdaName());

        JsonObject exJson = toJson(ex);
        String payload = GsonUtil.toJson(exJson);
        InvokeRequest invokeReq = new InvokeRequest();
        invokeReq.setFunctionName(getLambdaName());
        invokeReq.setPayload(payload);

        log.info("Calling lambda {}", getLambdaName());
        InvokeResult invokeRes = lambda.invoke(invokeReq);
        if (invokeRes.getStatusCode() != 200 || StringUtils.equals("Unhandled", invokeRes.getFunctionError())) {
            throw new RuntimeException("Lambda " + getLambdaName() + " completed with status " + invokeRes.getStatusCode() + " and/or error " + invokeRes.getFunctionError());
        }
        log.info("Got reply from lambda {}", getLambdaName());

        JsonObject exProcessed = GsonUtil.parseObj(invokeRes.getPayload().array());
        GsonUtil.findObj(exProcessed, "response").ifPresent(resJson -> log.info("Got response object"));
        Exchange exNew = GsonUtil.parse(invokeRes.getPayload().array(), Exchange.class);

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
