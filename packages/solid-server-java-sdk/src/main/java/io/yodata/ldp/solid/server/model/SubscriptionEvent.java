package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonObject;

public class SubscriptionEvent {

    public static class SubscriptionAction {

        public static String Authorize = "AuthorizeAction";
        public static String Update = "UpdateAction";
        public static String Revoke = "RevokeAction";

        private String type;
        private String agent;
        private String instrument;
        private JsonObject object;

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

        public JsonObject getObject() {
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
