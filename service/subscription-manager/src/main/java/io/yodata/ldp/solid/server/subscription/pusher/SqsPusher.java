package io.yodata.ldp.solid.server.subscription.pusher;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.JsonObject;
import io.yodata.EnvUtils;
import io.yodata.GsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class SqsPusher {

    private static final Logger log = LoggerFactory.getLogger(SqsPusher.class);

    private final String sqsUrl = EnvUtils.get("PUSHER_SQS_URL");
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
        log.info("Event dispatched to SQS queue {}", req.getQueueUrl());
    }

}
