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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.function.Supplier;

public class Pusher {

    private static class LazyLoadProvider<T> implements Supplier<T> {

        private Supplier<T> builder;
        private T obj;

        private LazyLoadProvider(Supplier<T> builder) {
            this.builder = builder;
        }

        @Override
        public T get() {
            synchronized (this) {
                if (Objects.isNull(obj)) {
                    obj = builder.get();
                }
            }

            return obj;
        }

    }

    private static final Logger log = LoggerFactory.getLogger(Pusher.class);

    private Supplier<AmazonSQS> sqs = new LazyLoadProvider<>(AmazonSQSClientBuilder::defaultClient);
    private Supplier<AWSLambda> lambda = new LazyLoadProvider<>(AWSLambdaClientBuilder::defaultClient);
    private Supplier<CloseableHttpClient> http = new LazyLoadProvider<>(HttpClients::createDefault);

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
                sqs.get().sendMessage(req);
                log.info("Event dispatched to SQS queue {}", req.getQueueUrl());
            } else if (StringUtils.equals(target.getScheme(), "aws-lambda")) {
                String lName = target.getAuthority();
                InvokeRequest i = new InvokeRequest();
                i.setFunctionName(lName);
                i.setPayload(GsonUtil.toJson(data));
                InvokeResult r = lambda.get().invoke(i);
                int statusCode = r.getStatusCode();
                String functionError = r.getFunctionError();
                if (statusCode != 200 || StringUtils.isNotEmpty(functionError)) {
                    throw new RuntimeException("Error when calling lambda " + lName + " | Status code: " + statusCode + " | Error: " + functionError);
                }
                log.info("Lambda {} was successfully called", lName);
            } else if (StringUtils.equals(target.getScheme(), "aws-s3")) {
                throw new RuntimeException("AWS S3 is not implemented as subscription target");
            } else if (StringUtils.equalsAny(target.getScheme(), "http", "https")) {
                HttpPost req = new HttpPost(target);
                req.setEntity(new StringEntity(GsonUtil.toJson(data), ContentType.APPLICATION_JSON));
                try (CloseableHttpResponse res = http.get().execute(req)) {
                    int sc = res.getStatusLine().getStatusCode();
                    if (sc < 200 || sc >= 300) {
                        log.error("Unable to send notification | sc: {}", sc);
                        log.error("Error: {}", res.getEntity().getContent());
                        throw new RuntimeException("Status code when sending to " + target.toString() + ": " + sc);
                    }

                    log.info("Message was successfully sent to {}", target);
                } catch (UnknownHostException e) {
                    log.warn("Unable to send Message, will NOT retry: Unknown host: {}", e.getMessage());
                } catch (IOException e) {
                    log.error("Unable to send Message due to I/O error, will retry", e);
                    throw new RuntimeException("Unable to send notification to " + target.toString(), e);
                }
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
