package io.yodata.ldp.solid.server.aws;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class AmazonS3Config implements Config {

    private static final Logger log = LoggerFactory.getLogger(AmazonS3Config.class);

    private String configBucket;
    private final String configKeyName = "solid-serverless.json";

    private Config parent;
    private AmazonS3 s3;

    private boolean loaded = false;
    private JsonObject cfg = new JsonObject();

    public AmazonS3Config() {
    }

    public AmazonS3Config(Config cfg) {
        parent = cfg;
    }

    private void init() {
        if (loaded) {
            return;
        }

        if (Objects.isNull(s3)) {
            DefaultAWSCredentialsProviderChain credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
            s3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .build();
        }

        if (Objects.isNull(parent)) {
            return;
        }

        Optional<String> b = parent.find("bootstrap.config.aws.s3.bucket");
        if (!b.isPresent()) {
            b = parent.find("aws.s3.bucket.name");
        }
        if (!b.isPresent()) {
            b = parent.find("s3.bucket.name");
        }

        b.ifPresent(s -> {
            if (!s3.doesBucketExistV2(s)) {
                throw new IllegalStateException("S3 bucket does not exist: " + s);
            }

            configBucket = s;

            log.info("Getting config from {}/{}", configBucket, configKeyName);
            if (!s3.doesObjectExist(configBucket, configKeyName)) {
                return;
            }

            S3Object cfgRaw = s3.getObject(configBucket, configKeyName);
            cfg = GsonUtil.parseObj(cfgRaw.getObjectContent());
            try {
                cfgRaw.close();
            } catch (IOException e) {
                // FIXME need to determine if something needs to be done
            }
        });

        loaded = true;
    }

    @Override
    public Optional<String> find(String name) {
        init();

        List<String> keys = new ArrayList<>(Arrays.asList(name.split("\\.")));
        String last = keys.get(keys.size() - 1);
        keys.remove(keys.size() - 1);
        JsonObject c = cfg;
        for (String k : keys) {
            Optional<JsonObject> cNew = GsonUtil.findObj(c, k);
            if (cNew.isPresent()) c = cNew.get();
        }

        Optional<String> value = GsonUtil.findString(c, last);
        if (value.isPresent()) {
            return value;
        }

        return parent.find(name);
    }

}
