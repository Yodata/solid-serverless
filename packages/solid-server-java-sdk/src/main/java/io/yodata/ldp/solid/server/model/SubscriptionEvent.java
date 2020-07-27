package io.yodata.ldp.solid.server.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionEvent {

    public static class Subscription {

        private String type;
        private String version;
        private String agent;
        private String instrument;
        private String host;
        private List<String> subscribes;
        private List<String> publishes;
        private Map<String, String> scopes = new HashMap<>();

        public String getType() {
            return type;
        }

        public String getVersion() {
            return version;
        }

        public String getAgent() {
            return agent;
        }

        public String getInstrument() {
            return instrument;
        }

        public String getHost() {
            return host;
        }

        public List<String> getSubscribes() {
            return subscribes;
        }

        public List<String> getPublishes() {
            return publishes;
        }

        public Map<String, String> getScopes() {
            return scopes;
        }

    }

    public static class SubscriptionAction {

        public static String Authorize = "AuthorizeAction";
        public static String Revoke = "RevokeAction";

        private String type;
        private String agent;
        private String instrument;
        private Subscription object;

        public static String getAuthorize() {
            return Authorize;
        }

        public static String getRevoke() {
            return Revoke;
        }

        public String getType() {
            return type;
        }

        public String getAgent() {
            return agent;
        }

        public String getInstrument() {
            return instrument;
        }

        public Subscription getObject() {
            return object;
        }

    }

    private String topic;
    private SubscriptionAction data;

    public String getTopic() {
        return topic;
    }

    public SubscriptionAction getData() {
        return data;
    }

}
