package io.yodata.ldp.solid.server.subscription.pusher;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.function.Supplier;

public class Pusher {

    public static void main(String[] args) throws IOException {
        String data;
        try (InputStream is = new FileInputStream(System.getenv("SOLID_SERVERLESS_PUSHER_STANDALONE_INPUT"))) {
            data = IOUtils.toString(is, StandardCharsets.UTF_8);
        }

        JsonObject command = GsonUtil.parseObj(data);
        JsonObject obj = GsonUtil.getObj(command, "object");
        String target = GsonUtil.getStringOrThrow(command, "target");
        JsonObject cfg = GsonUtil.findObj(command, "config").orElseGet(JsonObject::new);

        new Pusher().send(obj, target, cfg);
    }

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

    private Supplier<AmazonSNS> sns = new LazyLoadProvider<>(AmazonSNSClientBuilder::defaultClient);
    private Supplier<AmazonSQS> sqs = new LazyLoadProvider<>(AmazonSQSClientBuilder::defaultClient);
    private Supplier<AWSLambda> lambda = new LazyLoadProvider<>(AWSLambdaClientBuilder::defaultClient);
    private Supplier<CloseableHttpClient> http = new LazyLoadProvider<>(HttpClients::createDefault);

    public void send(JsonObject data, String targetRaw, JsonObject cfg) {
        String id = GsonUtil.findString(data, "@id").orElseGet(() -> GsonUtil.findString(data, "id").orElse("<NOT PROVIDED>"));
        log.info("{} - Sending data to {}: {}", id, targetRaw, data);
        try {
            String dataRaw = GsonUtil.toJson(data);
            URI target = URI.create(targetRaw);
            if (StringUtils.equals("aws-sns", target.getScheme())) {
                PublishRequest req = new PublishRequest();
                String arn = target.getAuthority();
                log.info("ARN: {}", arn);
                req.setTopicArn(arn);
                req.setMessage(dataRaw);
                sns.get().publish(req);
                log.info("Event dispatched to SNS topic {}", req.getTopicArn());
            } else if (StringUtils.equals("aws-sqs", target.getScheme())) {
                String queueUrl = new URIBuilder(target).setScheme("https").build().toURL().toString();
                try {
                    SendMessageRequest req = new SendMessageRequest();
                    req.setQueueUrl(queueUrl);
                    if (StringUtils.endsWith(req.getQueueUrl(), ".fifo")) {
                        req.setMessageGroupId("default");
                    }
                    req.setMessageBody(dataRaw);
                    sqs.get().sendMessage(req);
                    log.info("Event dispatched to SQS queue {}", req.getQueueUrl());
                } catch (AmazonSQSException e) {
                    log.error("Failure to to SQS queue {}", queueUrl);
                    log.error("Message: {}", data);
                    log.error("Error: {}", e.getMessage(), e);
                    throw e;
                }
            } else if (StringUtils.equals(target.getScheme(), "aws-lambda")) {
                String lName = target.getAuthority();
                InvokeRequest i = new InvokeRequest();
                i.setFunctionName(lName);
                i.setPayload(dataRaw);
                InvokeResult r = lambda.get().invoke(i);
                int statusCode = r.getStatusCode();
                String functionError = r.getFunctionError();
                if (statusCode != 200 || StringUtils.isNotEmpty(functionError)) {
                    throw new RuntimeException("Error when calling lambda " + lName + " | Status code: " + statusCode + " | Error: " + functionError);
                }
                log.info("Lambda {} was successfully called", lName);
            } else if (StringUtils.equalsAny(target.getScheme(), "http", "https")) {
                HttpConfig httpCfg = GsonUtil.get().fromJson(cfg, HttpConfig.class);
                HttpPost req = new HttpPost(target);
                httpCfg.getHeaders().forEach((name, values) -> values.forEach(value -> req.addHeader(name, value)));
                req.setEntity(new StringEntity(GsonUtil.toJson(data), ContentType.APPLICATION_JSON));

                if (StringUtils.equals(httpCfg.getSign().getType(), "sha1-salt")) {
                    if (StringUtils.isEmpty(httpCfg.getSign().getSalt())) {
                        log.warn("No secret given but sha1 signature requested - ignoring signature");
                    } else {
                        try {
                            SecretKeySpec spec = new SecretKeySpec(httpCfg.getSign().getSalt().getBytes(StandardCharsets.UTF_8), "HmacSHA1");
                            Mac mac = Mac.getInstance("HmacSHA1");
                            mac.init(spec);
                            byte[] sign = mac.doFinal(dataRaw.getBytes(StandardCharsets.UTF_8));
                            String hex = "sha1=" + Hex.encodeHexString(sign);
                            req.addHeader("X-Signature", hex);
                        } catch (NoSuchAlgorithmException e) {
                            log.warn("Unable to sign message: Unknown algorithm", e);
                        } catch (InvalidKeyException e) {
                            log.warn("Unable to sign message: Invalid key spec", e);
                        }
                    }
                }

                try (CloseableHttpResponse res = http.get().execute(req)) {
                    int sc = res.getStatusLine().getStatusCode();
                    String body = EntityUtils.toString(res.getEntity());
                    if (sc < 200 || sc >= 300) {
                        log.error("{} - Unable to send notification - HTTP Status code: {}", id, sc);
                        log.error("Error: {}", body);
                    } else {
                        log.info("{} - Message was successfully sent to {}", id, target);
                        log.debug("Response body:\n{}", body);
                    }
                } catch (UnknownHostException e) {
                    log.warn("{} - Unable to send Message, will NOT retry: Unknown host: {}", id, e.getMessage());
                } catch (IOException e) {
                    log.error("{} - Unable to send Message due to I/O error, will retry", id, e);
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
