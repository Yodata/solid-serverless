package io.yodata.ldp.solid.server.subscription.inbox;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.SecurityProcessor;
import io.yodata.ldp.solid.server.aws.handler.container.ContainerHandler;
import io.yodata.ldp.solid.server.aws.handler.resource.ResourceHandler;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.FileHandler;

public class AppAuthProcessor implements Consumer<InboxService.Wrapper> {

    public static final String AuthType = "AuthorizeAction";
    public static final String SubType = "SubscribeAction";
    public static final String RejectType = "RejectAction";
    public static final List<String> Types = Arrays.asList(AuthType, SubType, RejectType);

    private static final Logger log = LoggerFactory.getLogger(AppAuthProcessor.class);

    public static class Scope {

        private String path;
        private AclMode mode;
        private boolean isSubscribe;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public AclMode getMode() {
            return mode;
        }

        public void setMode(AclMode mode) {
            this.mode = mode;
        }

        public boolean isSubscribe() {
            return isSubscribe;
        }

        public void setSubscribe(boolean subscribe) {
            isSubscribe = subscribe;
        }

        public boolean matchesPath(String path) {
            if (StringUtils.startsWithIgnoreCase(path, this.path)) {
                return true;
            }

            return StringUtils.equalsIgnoreCase(this.path, path);
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

    private S3Store store;
    private ContainerHandler storeMgr;
    private ResourceHandler fileMgr;
    private SecurityProcessor sec;

    public AppAuthProcessor() {
        store = S3Store.getDefault();
        storeMgr = new ContainerHandler(store);
        fileMgr = new ResourceHandler(store);
        sec = SecurityProcessor.getDefault();
    }

    private Scope parse(String raw) throws IllegalArgumentException {
        String[] data = raw.split(":", 2);
        if (data.length != 2) {
            throw new IllegalArgumentException("wrong format, missing :");
        }

        if (StringUtils.isAnyBlank(data)) {
            throw new IllegalArgumentException("Blank data");
        }

        Scope scope = new Scope();
        switch (data[0]) {
            case "profile":
                scope.setPath("/profile/");
                break;
            default:
                throw new IllegalArgumentException(data[0]);
        }

        scope.setMode(AclMode.valueOf(StringUtils.capitalize(data[1])));
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

        for (String raw : toRemove) {
            log.info("Removing scope {}", raw);
            try {
                Scope scope = parse(raw);
                Target aclTarget =  Target.forPath(pod, scope.getPath());
                store.getEntityAcl(aclTarget, false)
                        .ifPresent(acl -> {
                            log.info("We have an ACL to check at {}", aclTarget.getPath());
                            acl.getEntity(newAction.getObject()).ifPresent(entry -> {
                                log.info("We have an ACL entry for {}", newAction.getObject());
                                if (entry.getModes().contains(scope.getMode())) {
                                    log.info("Removing ACL mode {}", scope.getMode());
                                    entry.getModes().remove(scope.getMode());
                                    acl.getEntities().put(newAction.getObject(), entry);
                                    store.setEntityAcl(aclTarget, acl);
                                    log.info("ACL updated at {}", aclTarget.getPath());
                                } else {
                                    log.info("Entity {} does not have {} access, nothing to do", newAction.getObject(), scope.getMode());
                                }
                            });
                        });

                if (scope.isSubscribe()) {
                    log.info("Scope is also subscription, removing from pod");

                    boolean hadSub = false;
                    List<Subscription> subs = store.getEntitySubscriptions(pod.getId());
                    Iterator<Subscription> i = subs.iterator();
                    while (i.hasNext()) {
                        Subscription sub = i.next();
                        if (!StringUtils.equals(scope.getPath(), sub.getObject())) {
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

                        Request in = new Request();
                        in.setMethod("PUT");
                        in.setTimestamp(Instant.now());
                        in.setTarget(Target.forPath(pod, "/settings/subscriptions"));
                        in.setBody(GsonUtil.makeObj("items", GsonUtil.asArrayObj(subs)));
                        fileMgr.put(in);

                        log.info("Existing subscription for {} on {} removed", newAction.getObject(), scope.getPath());
                    }
                }
            } catch (IllegalArgumentException e) {
                log.warn("Unknown/invalid scope {}, ignoring", raw);
            }
        }

        List<String> toAdd = new ArrayList<>(newAction.getContext().getScope());
        toAdd.removeAll(oldAction.getContext().getScope());

        for (String raw : toAdd) {
            log.info("Adding scope {}", raw);
            try {
                Scope scope = parse(raw);
                Target aclTarget =  Target.forPath(pod, scope.getPath());
                store.getEntityAcl(aclTarget, false)
                        .ifPresent(acl -> {
                            log.info("We have an ACL to check at {}", aclTarget.getPath());
                            acl.getEntity(newAction.getObject()).ifPresent(entry -> {
                                log.info("We have an ACL entry for {}", newAction.getObject());
                                if (!entry.getModes().contains(scope.getMode())) {
                                    log.info("Adding ACL mode {}", scope.getMode());
                                    entry.getModes().add(scope.getMode());
                                    acl.getEntities().put(newAction.getObject(), entry);
                                    store.setEntityAcl(aclTarget, acl);
                                    log.info("ACL updated at {}", aclTarget.getPath());
                                } else {
                                    log.info("Entity {} already has {} access", newAction.getObject(), scope.getMode());
                                }
                            });
                        });

                if (scope.isSubscribe()) {
                    log.info("Scope is also subscription, adding to pod");

                    List<Subscription> subs = store.getEntitySubscriptions(pod.getId());
                    boolean foundMatching = subs.stream()
                            .anyMatch(sub -> {
                                if (!StringUtils.equals(scope.getPath(), sub.getObject())) {
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
                        sub.setObject(scope.getPath());
                        sub.setNeedsContext(false);
                        subs.add(sub);

                        Request in = new Request();
                        in.setMethod("PUT");
                        in.setTimestamp(Instant.now());
                        in.setTarget(Target.forPath(pod, "/settings/subscriptions"));
                        in.setBody(GsonUtil.makeObj("items", GsonUtil.asArrayObj(subs)));
                        fileMgr.put(in);

                        log.info("New subscription for {} on {} added", newAction.getObject(), scope.getPath());
                    }
                }
            } catch (IllegalArgumentException e) {
                log.warn("Unknown/invalid scope {}, ignoring", raw);
            }
        }
    }

    public void acceptAuth(Request request, JsonObject message) {
        AuthorizeAction newAction = GsonUtil.get().fromJson(message, AuthorizeAction.class);

        Target pod = new Target(request.getTarget().getId().resolve("/profile/card#me"));
        pod.setAccessType(AclMode.Control);

        try {
            sec.authorize(request.getSecurity(), pod);

            JsonObject data = store.findEntityData(pod.getId(), "/settings/auth/entities")
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

                store.saveEntityData(pod.getId(), "/settings/auth/entities", data);

                // Now we inform the entity there is a pending subscription
                JsonObject subPending = new JsonObject();
                subPending.addProperty("agent", pod.getId().toString());
                subPending.addProperty("type", "SubscribeAction");
                subPending.addProperty("actionStatus", "PotentialActionStatus");
                subPending.addProperty("object", pod.getId().toString());
                subPending.add("context", GsonUtil.makeObj(newAction.getContext()));
                subPending.addProperty("@to", newAction.getObject());

                Request r = new Request();
                r.setMethod("POST");
                r.setTarget(Target.forPath(pod, "/outbox/"));
                r.setBody(subPending);
                Response res = storeMgr.post(r);
                log.info("SC for sending potential subscribe: {}", res.getStatus());
            }
        } catch (ForbiddenException e) {
            log.warn("AuthorizeAction was denied at {}: {}", request.getTarget().getId(), e.getMessage());
        }
    }

    private void acceptSub(Request request, JsonObject message) {
        String actionStatus = GsonUtil.findString(message, "actionStatus").orElse("");
        if (!StringUtils.equals("ActiveActionStatus", actionStatus)) {
            log.warn("Ignoring {} event, unknown status: {}", SubType, actionStatus);
            return;
        }

        String agent = GsonUtil.findString(message, "agent").orElse("");
        if (StringUtils.isBlank(agent)) {
            log.warn("Ignoring invalid event without agent");
            return;
        }

        Target pod = new Target(request.getTarget().getId().resolve("/profile/card#me"));
        pod.setAccessType(AclMode.Control);

        try {
            sec.authorize(request.getSecurity(), Target.forPath(pod, "/"));
            JsonObject items = store.findEntityData(pod.getId(), "/settings/auth/entities")
                    .flatMap(GsonUtil::tryParseObj)
                    .flatMap(obj -> GsonUtil.findObj(obj, "items"))
                    .orElseGet(JsonObject::new);

            Optional<JsonObject> item = GsonUtil.findObj(items, agent);
            if (!item.isPresent()) {
                log.warn("We do not have a pending subscription for {}, ignoring event", agent);
                return;
            }
            log.info("We have a pending subscription for {}", agent);

            AuthorizeAction newAction = GsonUtil.get().fromJson(item.get(), AuthorizeAction.class);

            // We make a dummy action without any scopes so the new ones get added
            AuthorizeAction oldAction = new AuthorizeAction();
            oldAction.setObject(agent);

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
            sec.authorize(request.getSecurity(), pod);

            JsonObject data = store.findEntityData(pod.getId(), "/settings/auth/entities")
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
            store.saveEntityData(pod.getId(), "/settings/auth/entities", data);
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
