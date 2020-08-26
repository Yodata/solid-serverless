package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.model.transform.Policies;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class EntityBasedStore implements Store {

    private static final Logger log = LoggerFactory.getLogger(EntityBasedStore.class);
    private static final Type subListType = new TypeToken<List<Subscription>>() {
    }.getType();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("/yyyy/MM/dd/HH/mm/ss/SSS/");

    public static final String SUBS_PATH = "/settings/subscriptions";

    protected String buildEntityPath(String entity, String path) {
        return "entities/" + entity.toLowerCase() + "/data/by-id" + path;
    }

    protected String buildEntityPath(URI entity, String path) {
        return buildEntityPath(entity.getHost(), path);
    }

    protected String buildEntityPath(URI entity) {
        return buildEntityPath(entity, entity.getPath());
    }

    private Optional<Acl> fetchAcl(String entity, String path, boolean recursive) {
        entity = entity.toLowerCase(); // Host is case-insensitive

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
            log.debug("Checking in {}", aclPath);
            Optional<String> aclRaw = getData(aclPath);
            if (aclRaw.isPresent()) {
                log.info("ACL found at {}", aclPath);
                return Optional.of(GsonUtil.get().fromJson(aclRaw.get(), Acl.class));
            } else {
                log.debug("ACL not found at {}", aclPath);
            }
        }

        log.info("No ACL found in {} for {}", entity, path);
        return Optional.empty();
    }

    public Optional<JsonObject> getDefaultPolicy() {
        return getData("default/data/by-id/public/yodata/data-policy.json").map(GsonUtil::parseObj);
    }

    public Optional<JsonObject> getGlobalPolicy() {
        return getData("global/data/by-id/public/yodata/data-policy.json").map(GsonUtil::parseObj);
    }

    public Optional<JsonObject> getEntityPolicy(URI entity) {
        return findEntityData(entity, "/public/yodata/data-policy.json").map(GsonUtil::parseObj);
    }

    @Override
    public boolean save(Request in) {
        String path = buildEntityPath(in.getTarget().getHost(), in.getTarget().getPath());
        boolean exists = exists(path);
        save(in.getContentType().orElse("application/octet-stream"), in.getBody(), path);
        return exists;
    }

    @Override
    public boolean saveEntityData(URI entity, String path, JsonElement el) {
        Target t = new Target(entity.resolve(path));
        Request in = new Request();
        in.setTarget(t);
        in.setBody(el);
        in.setTimestamp(Instant.now());
        return save(in);
    }

    @Override
    public Optional<Acl> getEntityAcl(Target t, boolean recursive) {
        return fetchAcl("entities/" + t.getHost(), t.getPath(), recursive);
    }

    @Override
    public Optional<Acl> getDefaultAcl(String path) {
        return fetchAcl("default", path, true);
    }

    public Optional<Acl> getAcl(Target t) {
        return Optional.ofNullable(getEntityAcl(t).orElseGet(() -> getDefaultAcl(t.getPath()).orElse(null)));
    }

    @Override
    public void setEntityAcl(Target t, Acl acl) {
        save(MimeTypes.APPLICATION_JSON, GsonUtil.toJson(acl).getBytes(StandardCharsets.UTF_8), "entities/" + t.getHost() + "/data/by-id" + t.getPath() + ".acl");
    }

    @Override
    public List<Subscription> getEntitySubscriptions(URI entity) {
        String host = new Target(entity).getHost();
        log.info("Getting entity subscriptions for {}", host);
        List<Subscription> subs = new ArrayList<>();
        findEntityData(entity, SUBS_PATH).ifPresent(obj -> subs.addAll(extractSubs(SUBS_PATH, obj, false)));
        return subs;
    }

    @Override
    public void setEntitySubscriptions(URI entity, List<Subscription> subs) {
        if (subs.stream().anyMatch(sub -> StringUtils.isBlank(sub.getAgent()))) {
            throw new IllegalArgumentException("Some subscription(s) do not have an agent");
        }

        save(MimeTypes.APPLICATION_JSON, GsonUtil.toJsonBytes(subs), buildEntityPath(entity.getHost(), SUBS_PATH));
    }

    @Override
    public void setEntitySubscriptions(URI entity, JsonObject subs) {
        save(MimeTypes.APPLICATION_JSON, GsonUtil.toJsonBytes(subs), buildEntityPath(entity.getHost(), SUBS_PATH));
    }

    public List<Subscription> extractSubs(String path, String obj, boolean needContext) {
        JsonElement el = GsonUtil.parse(obj);
        if (el.isJsonArray()) {
            el = GsonUtil.makeObj("items", el);
        }

        if (!el.isJsonObject()) {
            log.warn("{} is not a JSON array or object, skipping", path);
            return new ArrayList<>();
        }

        String version = GsonUtil.findString(el.getAsJsonObject(), "version").orElse("0");

        List<Subscription> list;
        if (StringUtils.equals(version, "0")) {
            list = GsonUtil.get().fromJson(GsonUtil.findArray(el.getAsJsonObject(), "items").orElseGet(JsonArray::new), subListType);
        } else {
            if (!StringUtils.equals(version, "1")) {
                log.warn("Subscription file at {} is of unsupported version {}, will try to parse regardless", path, version);
            }

            try {
                list = GsonUtil.get().fromJson(el, Subscriptions.class).toMatchList();
            } catch (JsonSyntaxException e) {
                log.warn("Invalid subscription file at {}, ignoring", path, e);
                list = new ArrayList<>();
            }
        }

        List<Subscription> subs = new ArrayList<>();
        int i = 1;
        for (Subscription sub : list) {
            if (StringUtils.isBlank(sub.getId())) {
                sub.setId(path + "#entity-" + i);
                if (Objects.isNull(sub.needsContext())) {
                    sub.setNeedsContext(needContext);
                }
            }
            subs.add(sub);
            i++;
        }
        return subs;
    }

    private List<Subscription> getInternalSubscriptions() {
        log.info("Getting internal subscriptions");
        List<Subscription> subs = new ArrayList<>();
        String intPath = "internal/subscriptions";
        getData(intPath).ifPresent(obj -> subs.addAll(extractSubs(intPath, obj, true)));
        return subs;
    }

    private List<Subscription> getGlobalSubscriptions() {
        log.info("Getting global subscriptions");
        List<Subscription> subs = new ArrayList<>();
        String entPath = "global/subscriptions";
        getData(entPath).ifPresent(obj -> subs.addAll(extractSubs(entPath, obj, true)));
        return subs;
    }

    @Override
    public List<Subscription> getAllSubscriptions(URI entity) {
        log.info("Getting all subscriptions for {}", entity);
        List<Subscription> subs = getInternalSubscriptions();
        subs.addAll(getGlobalSubscriptions());
        subs.addAll(getEntitySubscriptions(entity));
        return subs;
    }

    @Override
    public Subscriptions getSubscriptions(URI entity) {
        return GsonUtil.get().fromJson(getRawSubscriptions(entity), Subscriptions.class);
    }

    @Override
    public JsonObject getRawSubscriptions(URI entity) {
        Optional<String> rawOpt = findEntityData(entity, SUBS_PATH);

        String raw;
        if (rawOpt.isPresent()) {
            raw = rawOpt.get();
        } else {
            log.info("No subscriptions file for {}, using empty", entity);
            raw = "{}";
        }

        JsonElement rawEl = GsonUtil.parse(raw);
        if (rawEl.isJsonArray()) {
            log.info("Old format, we turn info an object");
            rawEl = GsonUtil.makeObj("items", rawEl);
        }

        if (!rawEl.isJsonObject()) { //
            log.info("Invalid format, we ignore");
            rawEl = GsonUtil.makeObj("version", "1");
        }

        return rawEl.getAsJsonObject();
    }

    @Override
    public Policies getPolicies(URI entity) {
        Policies p = new Policies();
        getDefaultPolicy().ifPresent(p::setDef);
        getGlobalPolicy().ifPresent(p::setGlobal);
        getEntityPolicy(entity).ifPresent(p::setLocal);
        return p;
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

    @Override
    public void post(Request in) {
        Instant timestamp = in.getTimestamp();

        URI id = in.getDestination().getId();
        log.debug("Id: {}", id);
        Path idPath = Paths.get(id.getPath());
        log.debug("Path: {}", id.getPath());
        String byIdPath = buildEntityPath(in.getDestination().getHost(), in.getDestination().getPath());
        ensureNotExisting(byIdPath);

        Map<String, String> meta = new HashMap<>();
        if (Objects.nonNull(timestamp)) {
            meta.put("X-Solid-Serverless-Timestamp", Long.toString(timestamp.toEpochMilli()));
        }
        save(in.getContentType().orElse("application/octet-stream"), in.getBody(), byIdPath, meta);

        if (Objects.nonNull(timestamp)) {
            log.debug("Timestamp: {}", timestamp.toEpochMilli());
            LocalDateTime ldt = LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC);
            String tsPath = "entities/" + id.getHost() + "/data/by-ts" + idPath.getParent().toString() + ldt.format(dtf) + idPath.getFileName().toString();
            link(byIdPath, tsPath);
        }
    }

    @Override
    public void delete(Request in) {
        URI id = in.getDestination().getId();
        Path idPath = Paths.get(id.getPath());
        String path = buildEntityPath(in.getTarget().getHost(), in.getTarget().getPath());

        findMeta(path).ifPresent(meta -> {
            String tsRaw = meta.get("X-Solid-Serverless-Timestamp".toLowerCase());
            if (StringUtils.isNotBlank(tsRaw)) {
                try {
                    Instant ts = Instant.ofEpochMilli(Long.parseLong(tsRaw));
                    LocalDateTime ldt = LocalDateTime.ofInstant(ts, ZoneOffset.UTC);
                    String tsPath = "entities/" + id.getHost() + "/data/by-ts" + idPath.getParent().toString() + ldt.format(dtf) + idPath.getFileName().toString();
                    delete(tsPath);
                    log.debug("Deleted by TS index: {}", tsPath);
                } catch (NumberFormatException e) {
                    log.warn("Invalid TS header value: {}", tsRaw);
                }
            }
        });

        delete(path);
        log.info("Deleted ID: {}", path);
    }

    @Override
    public void save(String path, JsonElement content) {
        save(MimeTypes.APPLICATION_JSON, GsonUtil.toJsonBytes(content), path, new HashMap<>());
    }

    public void save(String contentType, byte[] bytes, String path) {
        save(contentType, bytes, path, new HashMap<>());
    }

    public abstract void link(String linkTargetPath, String linkPath);

    protected abstract String getTsPrefix(String from, String namespace);

    protected abstract void save(String contentType, byte[] bytes, String path, Map<String, String> meta);

    public abstract Optional<Map<String, String>> findMeta(String path);

}
