package io.yodata.ldp.solid.server.notification;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import io.yodata.EnvUtils;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.model.event.StorageAction;
import io.yodata.ldp.solid.server.model.Request;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);

    private AmazonSNS sns;
    private String storeTopic;

    public EventBus() {
        this.storeTopic = EnvUtils.get("EVENT_STORE_SNS_TOPIC_ARN");
        if (StringUtils.isBlank(storeTopic)) {
            throw new IllegalStateException("Event store SNS topic ARN is not valid");
        }

        DefaultAWSCredentialsProviderChain credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
        sns = AmazonSNSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .build();
    }

    public void sendStoreEvent(Request in) {
        StorageAction event = new StorageAction();
        event.setTarget(in.getTarget().getId().toString());
        event.setId(in.getDestination().getId().toString());
        switch (in.getMethod()) {
            case "POST":
                event.setType(StorageAction.Add);
                break;
            case "PUT":
                event.setType(StorageAction.Update);
                break;
            case "DELETE":
                event.setType(StorageAction.Delete);
                break;
            default:
                log.info("HTTP method {} is unknown, cannot send storage action event");
                return;
        }

        in.getContentType().filter(ct -> StringUtils.equals(MimeTypes.APPLICATION_JSON, ct)).ifPresent(ct -> {
            try {
                event.setObject(GsonUtil.parseObj(in.getBody()));
            } catch (IllegalArgumentException e) {
                log.info("JSON was given but could not be parsed as JSON object, not adding to the body");
            }
        });

        in.setBody("".getBytes(StandardCharsets.UTF_8));
        event.setRequest(in);

        log.info("Publishing store event to SNS topic {}", storeTopic);
        sns.publish(storeTopic, GsonUtil.get().toJson(event));
        log.info("Published store event");
    }

}
