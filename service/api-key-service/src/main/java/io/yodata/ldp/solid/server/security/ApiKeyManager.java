package io.yodata.ldp.solid.server.security;

import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.model.SecurityContext;
import io.yodata.ldp.solid.server.model.SolidServer;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Optional;

// FIXME this should really be in the SDK
public class ApiKeyManager {

    private final SolidServer srv;

    public ApiKeyManager(SolidServer srv) {
        this.srv = srv;
    }

    private String getPath(String key) {
        // FIXME No hardcoding. We need to refactor in the SDK using template for the base pod
        return "global/security/api/key/" + key;
    }

    public String generateKey(SecurityContext sc) {
        return addKey(RandomStringUtils.randomAlphanumeric(42), sc);
    }

    public Optional<SecurityContext> find(String key) {
        return srv.store().findForApiKey(key);
    }

    public String addKey(String key, SecurityContext sc) {
        srv.store().ensureNotExisting(getPath(key)).then(v -> saveKey(key, sc));
        return key;
    }

    public void saveKey(String key, SecurityContext sc) {
        if (sc.isAnonymous()) {
            throw new RuntimeException("At least instrument must be set");
        }

        srv.store().save(getPath(key), GsonUtil.get().toJsonTree(sc));
    }

}
