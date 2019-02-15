package io.yodata.ldp.solid.server.subscription.pusher;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;

public class Pusher {

    private static final Logger log = LoggerFactory.getLogger(Pusher.class);

    private AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
    private AWSLambda lambda = AWSLambdaClientBuilder.defaultClient();

    public void send(JsonObject data, String targetRaw) {
        log.info("Sending data to {}: {}", targetRaw, data);
        try {
            URI target = URI.create(targetRaw);
            if (StringUtils.equals("aws-sqs", target.getScheme())) {
                SendMessageRequest req = new SendMessageRequest();
                req.setQueueUrl(new URIBuilder(target).setScheme("https").build().toURL().toString());
                if (StringUtils.endsWith(req.getQueueUrl(), ".fifo")) {
                    req.setMessageGroupId("default");
                }
                req.setMessageBody(GsonUtil.toJson(data));
                sqs.sendMessage(req);
                log.info("Event dispatched to SQS queue {}", req.getQueueUrl());
            } else if (StringUtils.equals(target.getScheme(), "aws-lambda")) {
                String lName = target.getAuthority();
                InvokeRequest i = new InvokeRequest();
                i.setFunctionName(lName);
                i.setPayload(GsonUtil.toJson(data));
                InvokeResult r = lambda.invoke(i);
                int statusCode = r.getStatusCode();
                String functionError = r.getFunctionError();
                if (statusCode != 200 || StringUtils.isNotEmpty(functionError)) {
                    throw new RuntimeException("Error when calling lambda " + lName + " | Status code: " + statusCode + " | Error: " + functionError);
                }
                log.info("Lambda {} was successfully called", lName);
            } else if (StringUtils.equals(target.getScheme(), "aws-s3")) {
                throw new RuntimeException("AWS S3 is not implemented as subscription target");
            } else if (StringUtils.equalsAny(target.getScheme(), "http", "https")) {
                throw new RuntimeException("HTTP is not implemented as subscription target");
            } else {
                throw new RuntimeException("Scheme " + target.getScheme() + " is not supported");
            }
        } catch (IllegalArgumentException e) {
            log.error("Target destination is not understood: {}", targetRaw);
        } catch (URISyntaxException e) {
            log.error("Cannot build URI", e);
        } catch (MalformedURLException e) {
            log.error("Cannot build URL", e);
        }
    }

}
