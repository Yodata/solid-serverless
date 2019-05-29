package io.yodata.ldp.solid.server.aws.store;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.yodata.Base64Util;
import io.yodata.EnvUtils;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.exception.NotFoundException;
import io.yodata.ldp.solid.server.model.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class S3Store extends EntityBasedStore {

    private static final Logger log = LoggerFactory.getLogger(S3Store.class);

    private static S3Store instance;

    public synchronized static S3Store getDefault() {
        if (Objects.isNull(instance)) {
            instance = new S3Store();
        }

        return instance;
    }

    private AmazonS3 s3;
    private List<String> buckets;
    private int pageMaxKeys;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm/ss/SSS");

    private S3Store() {
        pageMaxKeys = EnvUtils.find("S3_LIST_MAX_KEYS").map(Integer::parseInt).orElse(50);

        DefaultAWSCredentialsProviderChain credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
        s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .build();

        buckets = new ArrayList<>();
        String raw = EnvUtils.find("S3_BUCKET_NAMES").orElseGet(() -> EnvUtils.get("S3_BUCKET_NAME"));
        JsonElement el = new JsonParser().parse(raw);
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

    private String getBucket() {
        return buckets.get(0);
    }

    private Optional<S3Object> getEntityFile(String entity, String path) {
        return getFile("entities/" + entity + "/data/by-id" + path);
    }

    private Optional<S3Object> getFile(String path) {
        log.debug("Getting S3 object at {}", path);
        try {
            return Optional.of(s3.getObject(getBucket(), path));
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() != 404) {
                throw new RuntimeException(e);
            }

            return Optional.empty();
        }
    }

    private Optional<ObjectMetadata> getFileMeta(String path) {
        log.debug("Getting S3 meta at {}", path);
        try {
            return Optional.of(s3.getObjectMetadata(getBucket(), path));
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() != 404) {
                throw new RuntimeException(e);
            }

            return Optional.empty();
        }
    }

    private Optional<S3Object> getEntityFile(URI entity, String path) {
        return getEntityFile(entity.getHost(), path);
    }

    @Override
    protected Optional<String> getData(String path) {
        log.debug("Getting S3 object {}", path);

        return getFile(path).map(this::getData);
    }

    @Override
    protected Optional<Map<String, String>> findMeta(String path) {
        return getFileMeta(path).map(meta -> new HashMap<>(meta.getUserMetadata()));
    }

    private String getData(S3Object o) {
        try (InputStream is = o.getObjectContent()) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<String> findEntityData(URI entity, String path) {
        return getEntityFile(entity, path).map(this::getData);
    }

    @Override
    public boolean exists(String path) {
        try {
            s3.getObjectMetadata(getBucket(), path);
            return true;
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() != 404) {
                log.warn("Error while reading S3 object {}: {}", path, e.getMessage());
                throw new RuntimeException(e);
            }

            return false;
        }
    }

    @Override
    protected void save(String contentType, byte[] bytes, String path, Map<String, String> meta) {
        log.debug("File {} will be stored in {} buckets", path, buckets.size());
        buckets.forEach(bucket -> {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(bytes.length);
            metadata.setUserMetadata(meta);

            log.info("Storing {} bytes in bucket {} in path {}", bytes.length, bucket, path);
            PutObjectResult res = s3.putObject(bucket, path, new ByteArrayInputStream(bytes), metadata);
            log.info("Stored under ETag {}", res.getETag());
        });
    }

    @Override
    public void delete(String path) {
        log.debug("Deleting {}", path);
        buckets.forEach(bucket -> {
            log.debug("Deleting from bucket {}", bucket);
            s3.deleteObject(bucket, path);
        });
        log.info("Deleted {}", path);
    }

    @Override
    public void link(String linkTargetPath, String linkPath) {
        buckets.forEach(bucket -> {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata("X-Solid-Serverless-Link", linkTargetPath);
            s3.putObject(bucket, linkPath, new ByteArrayInputStream(new byte[0]), metadata);
            log.info("Stored link in bucket {} from {} to {}", bucket, linkTargetPath, linkPath);
        });
    }

    @Override
    protected String getTsPrefix(String from, String namespace) {
        final String delimiter = "/";

        if (namespace.endsWith(delimiter)) {
            namespace = namespace.substring(0, namespace.length() - 1);
        }

        log.debug("Finding prefix for TS {}", from);

        // We find by year
        Instant ts = Instant.ofEpochMilli(Long.parseLong(from));
        String formatted = dtf.format(LocalDateTime.ofInstant(ts, ZoneOffset.UTC));
        log.debug("Formatted: {}", formatted);
        String[] paths = formatted.split(delimiter);
        String prefix = namespace + delimiter;

        for (String path : paths) {
            log.debug("-- Loop --");
            log.debug("Prefix: {}", prefix);

            int max = Integer.parseInt(path);
            log.debug("Max: {}", max);

            ListObjectsV2Request req = new ListObjectsV2Request();
            req.setBucketName(getBucket());
            req.setDelimiter(delimiter);
            req.setPrefix(prefix);

            ListObjectsV2Result res = s3.listObjectsV2(req);
            List<String> prefixes = new ArrayList<>();
            for (String cp : res.getCommonPrefixes()) {
                cp = cp.substring(prefix.length());
                if (cp.endsWith(delimiter)) cp = cp.substring(0, cp.length() - 1);
                log.debug("Common prefix: {}", cp);
                prefixes.add(cp);
            }
            Optional<String> before = prefixes.stream().filter(p -> Integer.parseInt(p) >= max).min(Comparator.naturalOrder());

            if (before.isPresent()) {
                log.debug("Found previous path: {}", before.get());
                prefix = prefix + before.get() + delimiter;
            } else {
                log.debug("No previous path, we do not continue further");
                prefix = prefix + max + delimiter;
                return prefix;
            }
        }

        log.debug("Finished looking for matches, returning current prefix");
        return prefix;
    }

    @Override
    public Page getPage(Target t, String by, String from, boolean isFullFormat, boolean isTemporal) {
        Page p = new Page();

        boolean isTs = false;

        if (StringUtils.equals("timestamp", by)) {
            isTemporal = true;
            isTs = true;
        }
        String prefix = isTemporal ? "entities/" + t.getHost() + "/data/by-ts" : "entities/" + t.getHost() + "/data/by-id";
        String namespace = prefix + t.getPath();

        ListObjectsV2Request req = new ListObjectsV2Request();
        req.setBucketName(getBucket());
        req.setPrefix(namespace);
        if (!"".equals(from)) {
            if (!isTs) {
                String sinceDecoded = namespace + new String(Base64Util.decode(from), StandardCharsets.UTF_8);
                log.info("Starting after {}", sinceDecoded);
                req.setStartAfter(sinceDecoded);
            } else {
                String tsPrefix = getTsPrefix(from, namespace);
                log.debug("TS Prefix: " + tsPrefix);
                req.setStartAfter(tsPrefix);
            }
        } else {
            log.info("Starting from the beginning");
        }

        if (!isTemporal) {
            req.setDelimiter("/");
        }

        do {
            log.debug("Looping");
            log.debug("Start after: " + req.getStartAfter());

            req.setMaxKeys(pageMaxKeys - p.getContains().size());
            ListObjectsV2Result res = s3.listObjectsV2(req);
            p.setNext(res.getNextContinuationToken());

            res.getCommonPrefixes().forEach(cp -> p.getContains().add(new JsonPrimitive(cp.substring(namespace.length()))));
            for (S3ObjectSummary obj : res.getObjectSummaries()) {
                if (obj.getKey().endsWith(".acl")) {
                    log.info("ACL found, skipping from listing");
                    continue;
                }

                log.debug("Adding {}", obj.getKey());
                if (isFullFormat) {
                    Optional<S3Object> dataOpt = getFile(obj.getKey());

                    boolean done = false;
                    do {
                        if (!dataOpt.isPresent()) {
                            log.warn("S3 object vanished at {}, ignoring", obj.getKey());
                            break;
                        }

                        S3Object data = dataOpt.get();
                        String redirect = data.getObjectMetadata().getUserMetaDataOf("X-Solid-Serverless-Link");
                        if (StringUtils.isNotBlank(redirect)) {
                            log.debug("Following link from {} to {}", data.getKey(), redirect);

                            try {
                                data.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            dataOpt = getFile(redirect);
                            if (!dataOpt.isPresent()) {
                                log.warn("Cleaning up dead link at {} towards {}", data.getKey(), redirect);
                                delete(data.getKey());
                                break;
                            }

                            continue;
                        }

                        JsonElement el = GsonUtil.parse(data.getObjectContent(), JsonElement.class);
                        p.getContains().add(el);
                        done = true;
                    } while (!done);
                } else {
                    p.getContains().add(new JsonPrimitive(Paths.get(obj.getKey().substring(namespace.length())).getFileName().toString()));
                }

                p.setNext(Base64Util.encode(obj.getKey().substring(namespace.length()).getBytes(StandardCharsets.UTF_8)));
                req.setPrefix(null);
                req.setStartAfter(obj.getKey());
            }

            if (!res.isTruncated()) {
                log.debug("No more elements in scope");
                break;
            }
        } while (p.getContains().size() < pageMaxKeys);

        log.debug("Next token: {}", p.getNext());

        return p;
    }

    @Override
    public Response get(Target target) {
        String s3Path = "entities/" + target.getHost() + "/data/by-id" + target.getPath();
        log.info("Getting Resource {}", s3Path);

        try {
            S3Object obj = s3.getObject(getBucket(), s3Path);
            byte[] data = IOUtils.toByteArray(obj.getObjectContent(), obj.getObjectMetadata().getContentLength());
            obj.getObjectContent().close();

            Response r = new Response();
            r.getHeaders().put(Headers.CONTENT_TYPE, obj.getObjectMetadata().getContentType());
            r.getHeaders().put(Headers.CONTENT_LENGTH, Long.toString(obj.getObjectMetadata().getContentLength()));
            r.setBody(data);

            return r;
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() != 404) {
                log.warn("Error while reading S3 object {}: {}", s3Path, e.getMessage());
                throw new RuntimeException(e);
            }

            log.info("S3 object {} not found", s3Path);
            throw new NotFoundException();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
