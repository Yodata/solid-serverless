package io.yodata.ldp.solid.server.notification;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.config.Configs;
import io.yodata.ldp.solid.server.model.event.SkeletonEventBus;
import io.yodata.ldp.solid.server.model.event.StorageAction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsSnsEventBus extends SkeletonEventBus {

    private static final Logger log = LoggerFactory.getLogger(AwsSnsEventBus.class);

    private final String storeTopic;
    private final AmazonSNS sns;

    public AwsSnsEventBus() {
        storeTopic = Configs.get().get("aws.sns.event.store.topic");
        if (StringUtils.isBlank(storeTopic)) {
            throw new IllegalStateException("Event store SNS topic ARN is not valid");
        }

        DefaultAWSCredentialsProviderChain credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
        sns = AmazonSNSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .build();
    }

    @Override
    protected void doSend(StorageAction msg) {
        log.debug("Publishing store event to SNS topic {}", storeTopic);
        sns.publish(storeTopic, GsonUtil.toJson(msg));
        log.debug("Published store event");
    }

}
