package io.yodata.ldp.solid.server.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.config.Configs;
import io.yodata.ldp.solid.server.model.LazyLoadProvider;
import io.yodata.ldp.solid.server.model.Pusher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class SqsPusher implements Pusher {

    private static final Logger log = LoggerFactory.getLogger(SqsPusher.class);

    private final String sqsUrl = Configs.get().get("aws.sqs.pusher.url");
    private Supplier<AmazonSQS> sqs = new LazyLoadProvider<>(AmazonSQSClientBuilder::defaultClient);

    public void send(JsonObject data, String targetRaw, JsonObject config) {
        JsonObject payload = new JsonObject();
        payload.add("object", data);
        payload.addProperty("target", targetRaw);
        payload.add("config", config);

        SendMessageRequest req = new SendMessageRequest();
        req.setQueueUrl(sqsUrl);
        if (StringUtils.endsWith(req.getQueueUrl(), ".fifo")) {
            req.setMessageGroupId("default");
        }
        req.setMessageBody(GsonUtil.toJson(payload));
        sqs.get().sendMessage(req);
        log.debug("Event dispatched to SQS queue {}", req.getQueueUrl());
    }

}
