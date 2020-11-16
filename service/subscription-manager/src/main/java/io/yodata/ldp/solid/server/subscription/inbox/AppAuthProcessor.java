package io.yodata.ldp.solid.server.subscription.inbox;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public class AppAuthProcessor implements Consumer<InboxService.Wrapper> {

    public static final String AuthType = "AuthorizeAction";
    public static final String SubType = "SubscribeAction";
    public static final String RejectType = "RejectAction";
    public static final List<String> Types = Arrays.asList(AuthType, SubType, RejectType);

    private static final Logger log = LoggerFactory.getLogger(AppAuthProcessor.class);

    public static class Scope {

        private String id;
        private AclMode mode;
        private String topic;
        private String path;
        private boolean isSubscribe;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public AclMode getMode() {
            return mode;
        }

        public void setMode(AclMode mode) {
            this.mode = mode;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isSubscribe() {
            return isSubscribe;
        }

        public void setSubscribe(boolean subscribe) {
            isSubscribe = subscribe;
        }

    }

    public static class Context {

        private List<String> scope = new ArrayList<>();

        public List<String> getScope() {
            return scope;
        }

        public void setScope(List<String> scope) {
            this.scope = scope;
        }

    }

    public static class AuthorizeAction {

        private String type;
        private String agent;
        private String instrument;
        private String object = "";
        private Context context = new Context();

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAgent() {
            return agent;
        }

        public void setAgent(String agent) {
            this.agent = agent;
        }

        public String getInstrument() {
            return instrument;
        }

        public void setInstrument(String instrument) {
            this.instrument = instrument;
        }

        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }

    }

    private final SolidServer srv;

    public AppAuthProcessor(SolidServer srv) {
        this.srv = srv;
    }

    private Scope parse(String raw) {
        String[] data = raw.split(":", 2);
        if (data.length != 2) {
            throw new IllegalArgumentException("wrong format, missing :");
        }

        if (StringUtils.isAnyBlank(data)) {
            throw new IllegalArgumentException("Blank data");
        }

        Scope scope = new Scope();
        scope.setId(raw);
        scope.setTopic(data[1]);
        String mode = data[0];
        String topicPath = scope.getTopic();
        if (scope.getTopic().contains("#")) {
            String[] v = scope.getTopic().split("#", 2);
            topicPath = v[0];
            if (!topicPath.endsWith("/")) {
                topicPath = topicPath + "/";
            }
        }

        scope.setPath("/event/topic/" + topicPath);

        scope.setMode(AclMode.valueOf(StringUtils.capitalize(mode)));
        if (AclMode.Read.equals(scope.getMode())) {
            scope.setSubscribe(true);
        }

        return scope;
    }

    private void updateAcl(Target pod, AuthorizeAction oldAction, AuthorizeAction newAction) {
        log.info("Old scopes: {}", GsonUtil.toJson(oldAction.getContext().getScope()));
        log.info("New scopes: {}", GsonUtil.toJson(newAction.getContext().getScope()));

        List<String> toRemove = new ArrayList<>(oldAction.getContext().getScope());
        toRemove.removeAll(newAction.getContext().getScope());

        Map<String, List<Scope>> toRemovePath = new HashMap<>();
        for (String s : toRemove) {
            try {
                Scope scope = parse(s);
                List<Scope> scopes = toRemovePath.computeIfAbsent(scope.getPath(), p -> new ArrayList<>());
                scopes.add(scope);
            } catch (IllegalArgumentException e) {
                log.info("Ignoring invalid scope {}: {}", s, e.getMessage());
            }
        }

        List<String> toAdd = new ArrayList<>(newAction.getContext().getScope());
        toAdd.removeAll(oldAction.getContext().getScope());

        Map<String, List<Scope>> toAddPath = new HashMap<>();
        for (String s : toAdd) {
            try {
                Scope scope = parse(s);
                List<Scope> scopes = toAddPath.computeIfAbsent(scope.getPath(), p -> new ArrayList<>());
                scopes.add(scope);
            } catch (IllegalArgumentException e) {
                log.info("Ignoring invalid scope {}: {}", s, e.getMessage());
            }
        }

        for (Map.Entry<String, List<Scope>> entries : toRemovePath.entrySet()) {
            log.info("Removing scopes under {}", entries.getKey());

            Target aclTarget = Target.forPath(pod, entries.getKey());
            srv.store().getEntityAcl(aclTarget, false)
                    .ifPresent(acl -> {
                        log.info("We have an ACL to check at {}", aclTarget.getPath());
                        acl.getEntity(newAction.getObject()).ifPresent(entry -> {
                            log.info("We have an ACL entry for {}", newAction.getObject());
                            for (Scope scope : entries.getValue()) {
                                if (entry.getModes().contains(scope.getMode())) {
                                    log.info("Removing ACL mode {}", scope.getMode());
                                    entry.getModes().remove(scope.getMode());
                                    entry.setScope(newAction.getContext().getScope());
                                    acl.getEntities().put(newAction.getObject(), entry);
                                } else {
                                    log.info("Entity {} does not have {} access, nothing to do", newAction.getObject(), scope.getMode());
                                }
                            }

                            srv.store().setEntityAcl(aclTarget, acl);
                            log.info("ACL updated at {}", aclTarget.getPath());
                        });
                    });


            if (entries.getValue().stream().anyMatch(Scope::isSubscribe)) {
                log.info("Found scope acting as subscription, removing from pod");

                boolean hadSub = false;
                List<Subscription> subs = srv.store().getEntitySubscriptions(pod.getId());
                Iterator<Subscription> i = subs.iterator();
                while (i.hasNext()) {
                    Subscription sub = i.next();
                    if (!StringUtils.equals(entries.getKey(), sub.getObject())) {
                        continue;
                    }

                    if (!StringUtils.equals(newAction.getObject(), sub.getAgent())) {
                        continue;
                    }

                    i.remove();
                    hadSub = true;
                }

                if (!hadSub) {
                    log.info("No matching subscription already in place, not updating subscriptions");
                } else {
                    log.info("Matching subscription already in place, removing");

                    Request in = new Request().internal();
                    in.setMethod("PUT");
                    in.setTimestamp(Instant.now());
                    in.setTarget(Target.forPath(pod, "/settings/subscriptions"));
                    in.setBody(GsonUtil.makeObj("items", GsonUtil.asArrayObj(subs)));
                    srv.put(in);

                    log.info("Existing subscription for {} on {} removed", newAction.getObject(), entries.getKey());
                }
            }
        }

        for (Map.Entry<String, List<Scope>> entries : toAddPath.entrySet()) {
            Target aclTarget = Target.forPath(pod, entries.getKey());
            Acl acl = srv.store().getEntityAcl(aclTarget).orElseGet(Acl::forInit);
            log.info("ACL read: {}", GsonUtil.toJson(acl));
            Acl.Entry entry = acl.computeEntity(newAction.getObject());
            log.info("ACL compute: {}", GsonUtil.toJson(acl));

            for (Scope scope : entries.getValue()) {
                log.info("Adding scope {}", scope.getId());
                entry.getScope().add(scope.getTopic());
                if (!entry.getModes().contains(scope.getMode())) {
                    log.info("Adding ACL mode {}", scope.getMode());
                    entry.getModes().add(scope.getMode());
                } else {
                    log.info("Entity {} already has {} access", newAction.getObject(), scope.getMode());
                }
            }

            log.info("ACL before: {}", GsonUtil.toJson(acl));
            acl.getEntities().put(newAction.getObject(), entry);
            log.info("ACL after: {}", GsonUtil.toJson(acl));
            srv.store().setEntityAcl(aclTarget, acl);
            log.info("ACL updated at {}", aclTarget.getPath());

            if (entries.getValue().stream().anyMatch(Scope::isSubscribe)) {
                log.info("Found scope acting as subscription, adding to pod");

                List<Subscription> subs = srv.store().getEntitySubscriptions(pod.getId());
                boolean foundMatching = subs.stream()
                        .anyMatch(sub -> {
                            if (!StringUtils.equals(entries.getKey(), sub.getObject())) {
                                return false;
                            }

                            if (!StringUtils.equals(newAction.getObject(), sub.getAgent())) {
                                return false;
                            }

                            return true;
                        });
                if (!foundMatching) {
                    log.info("No matching subscription already in place, adding a new one");

                    Subscription sub = new Subscription();
                    sub.setAgent(newAction.getObject());
                    sub.setObject(entries.getKey());
                    sub.setNeedsContext(false);
                    sub.setExclusive(false);
                    subs.add(sub);

                    Request in = new Request();
                    in.setMethod("PUT");
                    in.setTimestamp(Instant.now());
                    in.setTarget(Target.forPath(pod, "/settings/subscriptions"));
                    in.setBody(GsonUtil.makeObj("items", GsonUtil.asArrayObj(subs)));
                    srv.put(in);

                    log.info("New subscription for {} on {} added", newAction.getObject(), entries.getKey());
                }
            }
        }
    }

    private void acceptAuth(Request request, JsonObject message) {
        AuthorizeAction newAction = GsonUtil.get().fromJson(message, AuthorizeAction.class);

        Target pod = new Target(request.getTarget().getId().resolve("/profile/card#me"));
        pod.setAccessType(AclMode.Control);

        try {
            srv.security().authorize(request.getSecurity(), pod);

            JsonObject data = srv.store().findEntityData(pod.getId(), "/settings/auth/entities")
                    .flatMap(GsonUtil::tryParseObj)
                    .orElseGet(JsonObject::new);

            JsonObject items = GsonUtil.findObj(data, "items").orElseGet(JsonObject::new);

            if (items.has(newAction.getObject())) {
                log.info("Entity {} has already an authorization - only updating ACLs", newAction.getObject());
                AuthorizeAction oldAction = GsonUtil.get().fromJson(items.get(newAction.getObject()), AuthorizeAction.class);
                updateAcl(pod, oldAction, newAction);
            } else {
                log.info("Entity {} does not have an authorization - sending sub notif", newAction.getObject());
                items.add(newAction.getObject(), message);
                data.add("items", items);

                // Now we inform the entity there is a pending subscription
                JsonObject subPending = new JsonObject();
                subPending.addProperty("agent", pod.getId().toString());
                subPending.addProperty("type", "SubscribeAction");
                subPending.addProperty("actionStatus", "PotentialActionStatus");
                subPending.addProperty("object", newAction.getObject());
                subPending.add("context", GsonUtil.makeObj(newAction.getContext()));
                subPending.addProperty("@to", newAction.getObject());

                Request r = new Request();
                r.setMethod("POST");
                r.setTarget(Target.forPath(pod, "/outbox/"));
                r.setBody(subPending);
                Response res = srv.post(r);
                log.info("SC for sending potential subscribe: {}", res.getStatus());
            }

            // We update the master state
            items.add(newAction.getObject(), message);
            data.add("items", items);
            srv.store().saveEntityData(pod.getId(), "/settings/auth/entities", data);
        } catch (ForbiddenException e) {
            log.warn("AuthorizeAction was denied at {}: {}", request.getTarget().getId(), e.getMessage());
        }
    }

    private void acceptSub(Request request, JsonObject message) {
        String actionStatus = GsonUtil.findString(message, "actionStatus").orElse("");
        if (!StringUtils.equalsAny(actionStatus, "ActiveActionStatus", "CompletedActionStatus")) {
            log.warn("Ignoring {} event, unknown status: {}", SubType, actionStatus);
            return;
        }

        String object = GsonUtil.findString(message, "object").orElse("");
        if (StringUtils.isBlank(object)) {
            log.warn("Ignoring invalid event without agent");
            return;
        }

        Target pod = new Target(request.getTarget().getId().resolve("/profile/card#me"));
        pod.setAccessType(AclMode.Control);

        try {
            JsonObject items = srv.store().findEntityData(pod.getId(), "/settings/auth/entities")
                    .flatMap(GsonUtil::tryParseObj)
                    .flatMap(obj -> GsonUtil.findObj(obj, "items"))
                    .orElseGet(JsonObject::new);

            Optional<JsonObject> item = GsonUtil.findObj(items, object);
            if (!item.isPresent()) {
                log.warn("We do not have a pending subscription for {}, ignoring event", object);
                return;
            }
            log.info("We have a pending subscription for {}", object);

            AuthorizeAction newAction = GsonUtil.get().fromJson(item.get(), AuthorizeAction.class);

            // We make a dummy action without any scopes so the new ones get added
            AuthorizeAction oldAction = new AuthorizeAction();
            oldAction.setObject(object);

            updateAcl(pod, oldAction, newAction);
        } catch (ForbiddenException e) {
            log.warn("AuthorizeAction was denied at {}: {}", request.getTarget().getId(), e.getMessage());
        }
    }

    private void acceptReject(Request request, JsonObject message) {
        String object = GsonUtil.findString(message, "object").orElse("");
        if (StringUtils.isBlank(object)) {
            log.warn("Ignoring invalid event without object");
            return;
        }

        Target pod = new Target(request.getTarget().getId().resolve("/profile/card#me"));
        pod.setAccessType(AclMode.Control);
        try {
            srv.security().authorize(request.getSecurity(), pod);

            JsonObject data = srv.store().findEntityData(pod.getId(), "/settings/auth/entities")
                    .flatMap(GsonUtil::tryParseObj)
                    .orElseGet(JsonObject::new);

            JsonObject items = GsonUtil.findObj(data, "items").orElseGet(JsonObject::new);
            Optional<JsonObject> item = GsonUtil.findObj(items, object);
            if (!item.isPresent()) {
                log.warn("We do not have an authorization entry for {}, ignoring event", object);
                return;
            }

            log.info("We remove authorizations for {}", object);
            AuthorizeAction oldAction = GsonUtil.get().fromJson(item.get(), AuthorizeAction.class);

            // We make a dummy action without any scopes so the old ones gets removed
            AuthorizeAction newAction = new AuthorizeAction();
            newAction.setObject(object);

            updateAcl(pod, oldAction, newAction);

            log.info("Removing entity authorization entry for {}", object);
            items.remove(object);
            srv.store().saveEntityData(pod.getId(), "/settings/auth/entities", data);
        } catch (ForbiddenException e) {
            log.warn("AuthorizeAction was denied at {}: {}", request.getTarget().getId(), e.getMessage());
        }

    }

    @Override
    public void accept(InboxService.Wrapper wrapper) {
        String type = GsonUtil.findString(wrapper.message, "type").orElse("");

        if (StringUtils.equals(AuthType, type)) {
            acceptAuth(wrapper.ev.getRequest(), wrapper.message);
        } else if (StringUtils.equals(SubType, type)) {
            acceptSub(wrapper.ev.getRequest(), wrapper.message);
        } else if (StringUtils.equals(RejectType, type)) {
            acceptReject(wrapper.ev.getRequest(), wrapper.message);
        } else {
            log.warn("Received unknown type {}, ignoring", type);
        }
    }

}
