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
import io.yodata.ldp.solid.server.model.Response;
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

    protected Response process(Exchange ex) {
        if (!StringUtils.startsWith(ex.getResponse().getContentType(), MimeTypes.APPLICATION_JSON)) {
            log.info("Response of Content-Type {} and not {}, skipping middleware", ex.getResponse().getContentType(), MimeTypes.APPLICATION_JSON);
            return ex.getResponse();
        }

        log.debug("Exchange {}: Response validation: start", ex.getRequest().getId());
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

        String p = new String(invokeRes.getPayload().array(), StandardCharsets.UTF_8);
        Exchange exNew = GsonUtil.parse(p, Exchange.class);
        if (Objects.nonNull(exNew.getResponse())) {
            log.debug("Got response object");
            ex.setResponse(exNew.getResponse());
        } else {
            log.warn("DID NOT get a response object, returning pre-call response");
        }

        log.info("Exchange {}: Response validation: end", ex.getRequest().getId());
        return ex.getResponse();
    }

    @Override
    public Response get(Exchange ex) {
        return process(ex);
    }

    @Override
    public Response post(Exchange ex) {
        return process(ex);
    }

    @Override
    public Response put(Exchange ex) {
        return process(ex);
    }

    @Override
    public Response delete(Exchange ex) {
        return process(ex);
    }

}
