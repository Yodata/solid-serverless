package io.yodata.ldp.solid.server.aws.handler;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.model.data.Exchange;
import io.yodata.ldp.solid.server.model.data.Response;
import io.yodata.ldp.solid.server.model.processor.ResponseFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class LambdaOutFilter extends LambdaValidationProcessor implements ResponseFilter {

    private final Logger log = LoggerFactory.getLogger(LambdaOutFilter.class);

    private final AWSLambda lambda;

    protected abstract String getLambdaName();

    public LambdaOutFilter() {
        DefaultAWSCredentialsProviderChain credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
        lambda = AWSLambdaClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .build();
    }

    protected Response process(Exchange ex) {
        if (!StringUtils.startsWith(ex.getResponse().getContentType(), MimeTypes.APPLICATION_JSON)) {
            log.info("Response is not of Content-Type {}, skipping middleware", MimeTypes.APPLICATION_JSON);
            return ex.getResponse();
        }

        log.info("Exchange {}: Response validation: start", ex.getRequest().getId());
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

        String p = new String(invokeRes.getPayload().array(), StandardCharsets.UTF_8);
        Exchange exNew = GsonUtil.parse(p, Exchange.class);
        if (Objects.nonNull(exNew.getResponse())) {
            log.info("Got response object");
            ex.setResponse(exNew.getResponse());
        } else {
            log.warn("DID NOT get a response object, returning pre-call response");
        }

        log.info("Exchange {}: Response validation: end", ex.getRequest().getId());
        return ex.getResponse();
    }

    @Override
    public Response head(Exchange ex) {
        return process(ex);
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
