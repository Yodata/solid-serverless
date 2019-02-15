package io.yodata.ldp.solid.server.aws.store;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.CollectionUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import io.yodata.Base64Util;
import io.yodata.EnvUtils;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.exception.NotFoundException;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.model.transform.Policies;
import io.yodata.ldp.solid.server.notification.EventBus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class S3Store {

    private static S3Store instance;

    public synchronized static S3Store getDefault() {
        if (Objects.isNull(instance)) {
            instance = new S3Store();
        }

        return instance;
    }

    public static class Then<T> {

        private T value;

        public Then(T value) {
            this.value = value;
        }

        public Then then(Consumer<T> c) {
            c.accept(value);
            return this;
        }

    }

    private final Logger log = LoggerFactory.getLogger(S3Store.class);

    private static final Type subListType = new TypeToken<List<Subscription>>() {}.getType();

    AmazonS3 s3;
    List<String> buckets;
    EventBus evBus;
    int pageMaxKeys;

    public S3Store() {
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

        evBus = new EventBus();
    }

    String getBucket() {
        return buckets.get(0);
    }

    public Optional<S3Object> getEntityFile(String entity, String path) {
        return getFile("entities/" + entity + "/data/by-id" + path);
    }

    public Optional<S3Object> getFile(String path) {
        log.info("Getting S3 object at {}", path);
        try {
            return Optional.of(s3.getObject(getBucket(), path));
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() != 404) {
                throw new RuntimeException(e);
            }

            return Optional.empty();
        }
    }

    public Optional<S3Object> getEntityFile(URI entity) {
        return getEntityFile(entity.getHost(), entity.getPath());
    }

    public Optional<S3Object> getEntityFile(URI entity, String path) {
        return getEntityFile(entity.getHost(), path);
    }

    public Optional<String> getData(String path) {
        log.info("Getting S3 object {}", path);

        return getFile(path).map(this::getData);
    }

    public String getData(S3Object o) {
        try {
            String data = IOUtils.toString(o.getObjectContent(), StandardCharsets.UTF_8);
            o.getObjectContent().close();
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Then<String> ensureNotExisting(String path) {
        if (exists(path)) {
            throw new RuntimeException("S3 object at " + path + " already exists");
        }

        return new Then<>(path);
    }

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

    public Optional<SecurityContext> findForApiKey(String apiKey) {
        log.info("Fetching data for API key {}", apiKey);
        Optional<String> data = getData("global/security/api/key/" + apiKey);
        if (data.isPresent()) {
            log.info("API key found");
            return Optional.of(GsonUtil.get().fromJson(data.get(), SecurityContext.class));
        } else {
            log.info("API key not found");
            return Optional.empty();
        }
    }

    private Optional<Acl> fetchAcl(String entity, String path, boolean recursive) {
        log.info("Fetching ACL in {} for {}", entity, path);

        List<String> paths = new ArrayList<>();
        paths.add(path);
        Path p = Paths.get(path);
        if (recursive) {
            log.info("Check ACL recursively");
            while (true) {
                p = p.getParent();
                if (Objects.isNull(p)) {
                    break;
                }

                String pRaw = p.toString();
                if (!"/".contentEquals(pRaw)) {
                    pRaw = pRaw + "/";
                }
                paths.add(pRaw);
            }
        }

        for (String loc : paths) {
            String aclPath = entity + "/data/by-id" + loc + ".acl";
            log.info("Checking in {}", aclPath);
            Optional<String> aclRaw = getData(aclPath);
            if (aclRaw.isPresent()) {
                log.info("ACL found at {}", aclPath);
                return Optional.of(GsonUtil.get().fromJson(aclRaw.get(), Acl.class));
            } else {
                log.info("ACL not found at {}", aclPath);
            }
        }

        log.info("No ACL found in {} for {}", entity, path);
        return Optional.empty();
    }

    public Optional<Acl> getEntityAcl(Target t) {
        return getEntityAcl(t, true);
    }

    public Optional<Acl> getEntityAcl(Target t, boolean recursive) {
        return fetchAcl("entities/" + t.getHost(), t.getPath(), recursive);
    }

    public Optional<Acl> getDefaultAcl(String path) {
        return fetchAcl("default", path, true);
    }

    public Optional<Acl> getAcl(Target t) {
        return Optional.ofNullable(getEntityAcl(t).orElseGet(() -> getDefaultAcl(t.getPath()).orElse(null)));
    }

    public void setEntityAcl(Target t, Acl acl) {
        save(MimeTypes.APPLICATION_JSON, GsonUtil.toJson(acl).getBytes(StandardCharsets.UTF_8), "entities/" + t.getHost() + "/data/by-id" + t.getPath() + ".acl");
    }

    private void save(String bucketName, String contentType, byte[] bytes, String path) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(bytes.length);

        log.info("Storing {} bytes in bucket {} in path {}", bytes.length, bucketName, path);
        PutObjectResult res = s3.putObject(bucketName, path, new ByteArrayInputStream(bytes), metadata);
        log.info("Stored under ETag {}", res.getETag());
    }

    void save(String contentType, byte[] bytes, String path) {
        buckets.forEach(bucket -> save(bucket, contentType, bytes, path));
    }

    public void save(String path, JsonElement content) {
        save(MimeTypes.APPLICATION_JSON, GsonUtil.toJsonBytes(content), path);
    }

    public void delete(String path) {
        log.info("Deleting {}", path);
        buckets.forEach(bucket -> {
            log.info("Deleting from bucket {}", bucket);
            s3.deleteObject(bucket, path);
        });
        log.info("Deleted {}", path);
    }

    public List<Subscription> getInternalSubscriptions() {
        log.info("Getting internal subscriptions");
        List<Subscription> subs = new ArrayList<>();
        String intPath = "internal/subscriptions";
        getFile(intPath).ifPresent(obj -> subs.addAll(extractSubs(intPath, obj)));
        return subs;
    }

    public List<Subscription> getGlobalSubscriptions() {
        log.info("Getting global subscriptions");
        List<Subscription> subs = new ArrayList<>();
        String entPath = "global/subscriptions";
        getFile(entPath).ifPresent(obj -> subs.addAll(extractSubs(entPath, obj)));
        return subs;
    }

    public List<Subscription> getEntitySubscriptions(URI entity) {
        String host = entity.getHost();
        log.info("Getting entity subscriptions for {}", host);
        List<Subscription> subs = new ArrayList<>();
        String entPath = "/settings/subscriptions";
        getEntityFile(entity, entPath).ifPresent(obj -> subs.addAll(extractSubs(entPath, obj)));
        return subs;
    }

    public List<Subscription> extractSubs(String path, S3Object obj) {
        List<Subscription> subs = new ArrayList<>();
        try (S3ObjectInputStream is = obj.getObjectContent()) {
            JsonElement el = GsonUtil.parse(IOUtils.toString(is, StandardCharsets.UTF_8));
            if (el.isJsonObject()) {
                el = el.getAsJsonObject().get("items");
            }
            List<Subscription> list = GsonUtil.get().fromJson(el, subListType);
            int i = 1;
            for (Subscription sub : list) {
                if (StringUtils.isBlank(sub.getId())) {
                    sub.setId(path + "#entity-" + i);
                }
                subs.add(sub);
                i++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return subs;
    }

    public List<Subscription> getSubscriptions(URI entity) {
        log.info("Getting all subscriptions for {}", entity);
        return CollectionUtils.mergeLists(
                CollectionUtils.mergeLists(getInternalSubscriptions(), getGlobalSubscriptions()),
                getEntitySubscriptions(entity));
    }

    public void setEntitySubscriptions(URI entity, List<Subscription> subs) {
        if (subs.stream().anyMatch(sub -> StringUtils.isBlank(sub.getAgent()))) {
            throw new IllegalArgumentException("Some subscription(s) do not have an agent");
        }

        save(MimeTypes.APPLICATION_JSON, GsonUtil.toJsonBytes(subs), "entities/" + entity.getHost() + "/subscriptions");
    }

    public Optional<JsonObject> getDefaultPolicy() {
        return getData("default/data/by-id/public/yodata/data-policy.json").map(GsonUtil::parseObj);
    }

    public Optional<JsonObject> getGlobalPolicy() {
        return getData("global/data/by-id/public/yodata/data-policy.json").map(GsonUtil::parseObj);
    }

    public Optional<JsonObject> getEntityPolicy(URI entity) {
        return getEntityFile(entity.getHost(), "/public/yodata/data-policy.json")
                .map(this::getData)
                .map(GsonUtil::parseObj);
    }

    public Policies getPolicies(URI entity) {
        Policies p = new Policies();
        getDefaultPolicy().ifPresent(p::setDef);
        getGlobalPolicy().ifPresent(p::setGlobal);
        getEntityPolicy(entity).ifPresent(p::setLocal);
        return p;
    }

    private String getTsPrefix(String from, String namespace) {
        if (from.length() < 13) {
            from = "0000000000000";
        }

        StringBuilder tsBuild = new StringBuilder(namespace);

        ListObjectsV2Request req = new ListObjectsV2Request();
        req.setBucketName(getBucket());
        int posStart = 0;
        int posEnd = from.length() - 13 + 2;
        String tsPrefix;
        List<S3ObjectSummary> objs;
        String lastListed = namespace;

        try {
            namespace = tsBuild.toString();
            tsPrefix = from.substring(posStart, posEnd) + "/";
            req.setPrefix(namespace + tsPrefix);
            req.setMaxKeys(1);
            objs = s3.listObjectsV2(req).getObjectSummaries();
            if (objs.size() < 1) {
                return lastListed;
            }
            tsBuild.append(tsPrefix);
            lastListed = objs.get(0).getKey();

            posStart = posEnd;
            posEnd += 2;
            namespace = tsBuild.toString();
            tsPrefix = from.substring(posStart, posEnd) + "/";
            req.setPrefix(namespace + tsPrefix);
            req.setMaxKeys(1);
            objs = s3.listObjectsV2(req).getObjectSummaries();
            if (objs.size() < 1) {
                return lastListed;
            }
            tsBuild.append(tsPrefix);
            lastListed = objs.get(0).getKey();

            posStart = posEnd;
            posEnd += 2;
            namespace = tsBuild.toString();
            tsPrefix = from.substring(posStart, posEnd) + "/";
            req.setPrefix(namespace + tsPrefix);
            req.setMaxKeys(1);
            objs = s3.listObjectsV2(req).getObjectSummaries();
            if (objs.size() < 1) {
                return lastListed;
            }
            tsBuild.append(tsPrefix);
            lastListed = objs.get(0).getKey();

            posStart = posEnd;
            posEnd += 2;
            namespace = tsBuild.toString();
            tsPrefix = from.substring(posStart, posEnd) + "/";
            req.setPrefix(namespace + tsPrefix);
            req.setMaxKeys(1);
            objs = s3.listObjectsV2(req).getObjectSummaries();
            if (objs.size() < 1) {
                return lastListed;
            }
            tsBuild.append(tsPrefix);
            lastListed = objs.get(0).getKey();

            posStart = posEnd;
            posEnd += 2;
            namespace = tsBuild.toString();
            tsPrefix = from.substring(posStart, posEnd) + "/";
            req.setPrefix(namespace + tsPrefix);
            req.setMaxKeys(1);
            objs = s3.listObjectsV2(req).getObjectSummaries();
            if (objs.size() < 1) {
                return lastListed;
            }
            tsBuild.append(tsPrefix);
            lastListed = objs.get(0).getKey();

            posStart = posEnd;
            namespace = tsBuild.toString();
            tsPrefix = from.substring(posStart);
            req.setPrefix(namespace + tsPrefix);
            req.setMaxKeys(1);
            objs = s3.listObjectsV2(req).getObjectSummaries();
            if (objs.size() < 1) {
                return lastListed;
            }
            tsBuild.append(tsPrefix);
            lastListed = objs.get(0).getKey();
        } catch (ArrayIndexOutOfBoundsException e) {
            // we don't care;
        }

        return lastListed;
    }

    private String getIdPrefix(String namespace, String host, String path, String id) {
        if (!path.endsWith("/")) {
            path += "/";
        }

        JsonObject data = getData("entities/" + host + path + id)
                .map(GsonUtil::parseObj)
                .orElseGet(JsonObject::new);

        return getTsPrefix(GsonUtil.findString(data, "timestamp").orElse(""), namespace);
    }

    public Page getPage(Target t, String from, String by, boolean isFullFormat) {
        Page p = new Page();

        String prefix = "entities/" + t.getHost() + "/data/by-id";
        String namespace = prefix + t.getPath();

        ListObjectsV2Request req = new ListObjectsV2Request();
        req.setBucketName(getBucket());
        req.setDelimiter("/");

        switch (by) {
            case "timestamp":
                req.setPrefix(getTsPrefix(from, namespace));
                break;
            case "path":
                req.setPrefix(getIdPrefix(namespace, t.getHost(), t.getPath(), from));
                break;
            case "token":
                req.setPrefix(namespace);
                if (!"".equals(from)) {
                    String sinceDecoded = namespace + new String(Base64Util.decode(from), StandardCharsets.UTF_8);
                    log.info("Starting after {}", sinceDecoded);
                    req.setStartAfter(sinceDecoded);
                } else {
                    log.info("Starting from the beginning");
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown from type: " + by);
        }

        do {
            req.setMaxKeys(pageMaxKeys - p.getContains().size());
            ListObjectsV2Result res = s3.listObjectsV2(req);
            p.setNext(res.getNextContinuationToken());

            res.getCommonPrefixes().forEach(cp -> p.getContains().add(new JsonPrimitive(cp.substring(namespace.length()))));
            for (S3ObjectSummary obj : res.getObjectSummaries()) {
                if (obj.getKey().endsWith(".acl")) {
                    log.info("ACL found, skipping from listing");
                    continue;
                }

                log.info("Adding {}", obj.getKey());
                if (isFullFormat) {
                    S3Object s3obj = s3.getObject(obj.getBucketName(), obj.getKey());
                    log.info("Redirection location: {}", s3obj.getRedirectLocation());
                    JsonElement el = GsonUtil.parse(s3obj.getObjectContent(), JsonElement.class);
                    p.getContains().add(el);
                } else {
                    p.getContains().add(new JsonPrimitive(Paths.get(obj.getKey().substring(namespace.length())).getFileName().toString()));
                }
                p.setNext(Base64Util.encode(obj.getKey().substring(namespace.length()).getBytes(StandardCharsets.UTF_8)));
                req.setPrefix(null);
                req.setStartAfter(obj.getKey());
            }

            if (!res.isTruncated()) {
                log.info("No more elements in scope");
                break;
            }

            log.info("Looping");
        } while (p.getContains().size() < pageMaxKeys);

        log.info("Next token: {}", p.getNext());

        return p;
    }

    public void post(Request in) {
        List<String> paths = new ArrayList<>();

        URI id = in.getDestination().getId();
        log.info("Id: {}", id);
        Path idPath = Paths.get(id.getPath());
        log.info("Path: {}", id.getPath());
        String byIdPath = "entities/" + in.getDestination().getHost() + "/data/by-id" + in.getDestination().getPath();
        ensureNotExisting(byIdPath);
        paths.add(byIdPath);

        Instant timestamp = in.getTimestamp();
        if (Objects.nonNull(timestamp)) {
            log.info("Timestamp: {}", timestamp.toEpochMilli());
            LocalDateTime ldt = LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("/yyyy/MM/dd/HH/mm/ss/SSS/");
            paths.add("entities/" + id.getHost() + "/data/by-ts" + idPath.getParent().toString() + ldt.format(dtf) + idPath.getFileName().toString());
        }

        log.info("Will be stored under {} paths in {} buckets", paths.size(), buckets.size());
        paths.forEach(p -> save(in.getContentType().orElse("application/octet-stream"), in.getBody(), p));
    }

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

    public boolean save(Request in) {
        String s3Path = "entities/" + in.getTarget().getHost() + "/data/by-id" + in.getTarget().getPath();
        boolean exists = exists(s3Path);
        save(in.getContentType().orElse("application/octet-stream"), in.getBody(), s3Path);
        return exists;
    }

    public void delete(Request in) {
        String s3Path = "entities/" + in.getTarget().getHost() + "/data/by-id" + in.getTarget().getPath();
        delete(s3Path);
    }

}
