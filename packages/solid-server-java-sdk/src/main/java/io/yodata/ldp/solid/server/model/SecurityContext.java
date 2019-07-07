package io.yodata.ldp.solid.server.model;

import io.yodata.ldp.solid.server.model.security.AclMode;

import java.util.*;

public class SecurityContext {

    public static SecurityContext asAnonymous() {
        SecurityContext sc = new SecurityContext();
        sc.isAnonymous = true;
        return sc;
    }

    public static SecurityContext forPod(String id) {
        SecurityContext sc = new SecurityContext();
        sc.setInstrument("https://" + id + "/profile/card#me");
        return sc;
    }

    private transient Boolean isAnonymous;
    private String agent;
    private String instrument;
    private boolean isAdmin;
    private boolean isDefaultAllowed;
    private transient List<AclMode> allowedModes = new ArrayList<>();

    public boolean isAnonymous() {
        return Optional.ofNullable(isAnonymous).orElseGet(() -> Objects.isNull(agent) && Objects.isNull(instrument));
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public Optional<String> getAgent() {
        return Optional.ofNullable(agent);
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

    public String getIdentity() {
        return getAgent().orElseGet(() -> Optional.ofNullable(getInstrument()).orElse(""));
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isDefaultAllowed() {
        return isDefaultAllowed;
    }

    public void setDefaultAllowed(boolean defaultAllowed) {
        isDefaultAllowed = defaultAllowed;
    }

    public List<AclMode> getAllowedModes() {
        if (Objects.isNull(allowedModes)) {
            return Collections.emptyList();
        }

        return allowedModes;
    }

    public boolean can(AclMode mode) {
        return Objects.nonNull(allowedModes) && allowedModes.contains(mode);
    }

    public void setAllowedModes(List<AclMode> allowedModes) {
        this.allowedModes = allowedModes;
    }

}
