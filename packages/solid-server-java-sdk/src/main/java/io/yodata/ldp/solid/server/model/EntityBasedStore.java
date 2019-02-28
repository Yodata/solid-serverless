package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class EntityBasedStore implements Store {

    private static final Logger log = LoggerFactory.getLogger(EntityBasedStore.class);
    private static final Type subListType = new TypeToken<List<Subscription>>() {}.getType();
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("/yyyy/MM/dd/HH/mm/ss/SSS/");

    protected String buildEntityPath(String entity, String path) {
        return "entities/" + entity + "/data/by-id" + path;
    }

    protected String buildEntityPath(URI entity, String path) {
        return buildEntityPath(entity.getHost(), path);
    }

    protected String buildEntityPath(URI entity) {
        return buildEntityPath(entity, entity.getPath());
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
        String path = "entities/" + in.getTarget().getHost() + "/data/by-id" + in.getTarget().getPath();
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
        String host = entity.getHost();
        log.info("Getting entity subscriptions for {}", host);
        List<Subscription> subs = new ArrayList<>();
        String entPath = "/settings/subscriptions";
        findEntityData(entity, entPath).ifPresent(obj -> subs.addAll(extractSubs(entPath, obj, false)));
        return subs;
    }

    @Override
    public void setEntitySubscriptions(URI entity, List<Subscription> subs) {
        if (subs.stream().anyMatch(sub -> StringUtils.isBlank(sub.getAgent()))) {
            throw new IllegalArgumentException("Some subscription(s) do not have an agent");
        }

        save(MimeTypes.APPLICATION_JSON, GsonUtil.toJsonBytes(subs), "entities/" + entity.getHost() + "/settings/subscriptions");
    }

    private List<Subscription> extractSubs(String path, String obj, boolean needContext) {
        List<Subscription> subs = new ArrayList<>();
        JsonElement el = GsonUtil.parse(obj);
        if (el.isJsonObject()) {
            el = el.getAsJsonObject().get("items");
        }
        List<Subscription> list = GsonUtil.get().fromJson(el, subListType);
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

    public List<Subscription> getSubscriptions(URI entity) {
        log.info("Getting all subscriptions for {}", entity);
        List<Subscription> subs = getInternalSubscriptions();
        subs.addAll(getGlobalSubscriptions());
        subs.addAll(getEntitySubscriptions(entity));
        return subs;
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

        paths.forEach(p -> save(in.getContentType().orElse("application/octet-stream"), in.getBody(), p));
    }

    @Override
    public void delete(Request in) {
        delete("entities/" + in.getTarget().getHost() + "/data/by-id" + in.getTarget().getPath());
    }

    @Override
    public void save(String path, JsonElement content) {
        save(MimeTypes.APPLICATION_JSON, GsonUtil.toJsonBytes(content), path);
    }

    protected abstract String getTsPrefix(String from, String namespace);

    protected abstract void save(String contentType, byte[] bytes, String path);

    protected abstract Optional<String> getData(String path);

}
