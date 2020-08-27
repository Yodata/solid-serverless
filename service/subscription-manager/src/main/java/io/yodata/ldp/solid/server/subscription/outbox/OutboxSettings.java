package io.yodata.ldp.solid.server.subscription.outbox;

import java.util.List;
import java.util.Objects;

public class OutboxSettings {

    public static class Domains {

        private List<String> blacklist;
        private List<String> whitelist;

        public boolean hasBlacklist() {
            return !Objects.isNull(blacklist);
        }

        public boolean hasWhitelist() {
            return !Objects.isNull(whitelist);
        }

        public List<String> getBlacklist() {
            return blacklist;
        }

        public List<String> getWhiteList() {
            return whitelist;
        }

    }

    private Domains authorizedDomains = new Domains();

    public Domains getAuthorizedDomains() {
        return authorizedDomains;
    }

}
