package io.yodata.ldp.solid.server.model.storage.s3;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.yodata.Base64Util;
import io.yodata.EnvUtils;
import io.yodata.ldp.solid.server.model.store.fs.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class S3Filesystem implements Filesystem {

    private static final Logger log = LoggerFactory.getLogger(S3Filesystem.class);

    private AmazonS3 s3;
    private List<String> buckets;

    public static AmazonS3 getClient() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }

    public static String getBuckets() {
        return EnvUtils.find("S3_BUCKET_NAMES").orElseGet(() -> EnvUtils.get("S3_BUCKET_NAME"));
    }

    public S3Filesystem() {
        this(getClient(), getBuckets());
    }

    public S3Filesystem(String bucketsRaw) {
        this(getClient(), bucketsRaw);
    }

    public S3Filesystem(AmazonS3 client, String bucketsRaw) {
        s3 = client;

        buckets = new ArrayList<>();
        JsonElement el = new JsonParser().parse(bucketsRaw);
        if (el.isJsonPrimitive()) {
            buckets.add(el.getAsString());
        } else if (el.isJsonArray()) {
            el.getAsJsonArray().forEach(arrEl -> {
                if (arrEl.isJsonPrimitive()) buckets.add(arrEl.getAsString());
                else throw new IllegalArgumentException(arrEl + " is not a valid bucket name");
            });
        } else {
            throw new IllegalArgumentException("S3_BUCKET_NAMES is not JSON format");
        }
    }

    private String getMainBucket() {
        return buckets.get(0);
    }

    @Override
    public boolean exists(String path) {
        log.debug("Checking if {} exists", path);

        return findMeta(path).isPresent();
    }

    @Override
    public Optional<FsElementMeta> findMeta(String path) {
        log.debug("Getting S3 meta at {}", path);

        try {
            return Optional.of(new S3FsElementMeta(s3.getObjectMetadata(getMainBucket(), path)));
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() != 404) {
                throw new RuntimeException(e);
            }

            return Optional.empty();
        }
    }

    @Override
    public Optional<FsElement> findElement(String path) {
        log.debug("Getting S3 object at {}", path);

        try {
            S3Object obj = s3.getObject(getMainBucket(), path);
            return Optional.of(new BasicElement(new S3FsElementMeta(obj.getObjectMetadata()), obj.getObjectContent()));
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() != 404) {
                throw new RuntimeException(e);
            }

            return Optional.empty();
        }
    }

    @Override
    public void setElement(String path, FsElement element) {
        log.debug("File {} will be stored in {} buckets", path, buckets.size());

        buckets.forEach(bucket -> {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(element.getMeta().getContentType());
            metadata.setContentLength(element.getMeta().getLength());
            metadata.setUserMetadata(element.getMeta().getProperties());

            log.debug("Storing {} bytes in bucket {} in path {}", element.getMeta().getLength(), bucket, path);
            PutObjectResult res = s3.putObject(bucket, path, element.getData(), metadata);
            log.info("Stored {}:{} ({})", bucket, path, res.getETag());
        });
    }

    @Override
    public FsPage listElements(String path, String token, long amount) {
        if (amount < 1 || amount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Amount of items in the list is out of range. Must be between 1 and " + Integer.MAX_VALUE);
        }

        BasicFsPage page = new BasicFsPage();
        page.setNext(token);

        ListObjectsV2Request req = new ListObjectsV2Request();
        req.setDelimiter("/");
        req.setBucketName(getMainBucket());
        req.setPrefix(path);
        req.setMaxKeys((int) amount);
        if (StringUtils.isNotEmpty(token)) {
            req.setStartAfter(path + Base64Util.decodeUtf8(token));
        }

        do {
            log.debug("Looping");
            log.debug("Start after: " + req.getStartAfter());

            req.setMaxKeys((int) amount - page.getElements().size());
            ListObjectsV2Result res = s3.listObjectsV2(req);
            if (res.isTruncated()) {
                page.setNext(res.getNextContinuationToken());
            }

            res.getCommonPrefixes().forEach(cp -> page.addElement(cp.substring(path.length())));
            for (S3ObjectSummary obj : res.getObjectSummaries()) {
                if (obj.getKey().endsWith(".acl")) {
                    log.info("ACL found, skipping from listing");
                    continue;
                }

                String localKey = obj.getKey().substring(path.length());

                log.debug("Adding {}", obj.getKey());
                page.addElement(Paths.get(localKey).getFileName().toString());
                page.setNext(Base64Util.encodeUtf8(localKey));
                req.setPrefix(null);
                req.setStartAfter(obj.getKey());
            }

            if (!res.isTruncated()) {
                log.debug("No more elements in scope");
                break;
            }
        } while (page.getElements().size() < amount);

        log.debug("Next token: {}", page.getNext());
        return page;
    }

    @Override
    public void deleteElement(String path) {
        log.debug("Deleting {}", path);
        buckets.forEach(bucket -> {
            log.debug("Deleting from bucket {}", bucket);
            s3.deleteObject(bucket, path);
        });
        log.info("Deleted {}", path);
    }

}
