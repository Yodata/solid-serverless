/*
 * Copyright 2018 YoData, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.yodata.ldp.solid.server.aws.handler;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.model.data.Exchange;
import io.yodata.ldp.solid.server.model.processor.RequestFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class LambdaInFilter extends LambdaValidationProcessor implements RequestFilter {

    private final Logger log = LoggerFactory.getLogger(LambdaInFilter.class);

    private final AWSLambda lambda;

    protected abstract String getLambdaName();

    public LambdaInFilter() {
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

        byte[] invokeResBody = invokeRes.getPayload().array();
        JsonObject exProcessed = GsonUtil.parseObj(invokeResBody);
        GsonUtil.findObj(exProcessed, "response").ifPresent(resJson -> log.info("Got response object"));
        Exchange exNew = GsonUtil.parse(invokeResBody, Exchange.class);

        if (Objects.nonNull(exNew.getRequest())) {
            ex.setRequest(exNew.getRequest());
        }
        ex.setResponse(exNew.getResponse());

        log.info("Request {} validation: end", ex.getRequest().getId());
    }

    @Override
    public void head(Exchange ex) {
        process(ex);
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
