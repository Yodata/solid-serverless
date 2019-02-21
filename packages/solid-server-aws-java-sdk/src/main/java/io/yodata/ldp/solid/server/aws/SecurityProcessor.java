package io.yodata.ldp.solid.server.aws;

import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.exception.UnauthorizedException;
import io.yodata.ldp.solid.server.model.Acl;
import io.yodata.ldp.solid.server.model.SecurityContext;
import io.yodata.ldp.solid.server.model.Store;
import io.yodata.ldp.solid.server.model.Target;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SecurityProcessor {

    private static final Logger log = LoggerFactory.getLogger(SecurityProcessor.class);
    private static SecurityProcessor o;

    public synchronized static SecurityProcessor getDefault() {
        if (Objects.isNull(o)) {
            o = new SecurityProcessor(S3Store.getDefault());
        }

        return o;
    }

    private final Store store;

    public SecurityProcessor(Store store) {
        this.store = store;
    }

    public SecurityContext authenticate(Map<String, List<String>> headers) {
        List<String> instruments = headers.getOrDefault("X-YoData-Instrument".toLowerCase(), Collections.emptyList());
        if (!instruments.isEmpty()) {
            SecurityContext sc = new SecurityContext();
            sc.setInstrument(instruments.get(0));
            sc.setAdmin(false);
            sc.setDefaultAllowed(false);
            return sc;
        }

        List<String> keys = headers.getOrDefault("x-api-key", Collections.emptyList());
        if (keys.isEmpty()) {
            log.info("API Key: <Not provided>");
            return SecurityContext.asAnonymous();
        }

        String key = keys.get(0);
        if (StringUtils.isEmpty(key)) {
            log.info("API Key: <Empty>");
            throw new UnauthorizedException("No API key provided");
        }

        log.info("API Key: {}", key);
        SecurityContext apiKeyContext = store.findForApiKey(key).orElseGet(SecurityContext::new);
        if (apiKeyContext.isAnonymous()) {
            log.info("API key is unknown");
            throw new UnauthorizedException("Invalid API Key");
        }

        log.info("API Key Agent: {}", apiKeyContext.getAgent().orElse("<Empty>"));
        log.info("API Key Instrument: {}", apiKeyContext.getInstrument());
        log.info("API Key is admin? {}", apiKeyContext.isAdmin());
        log.info("API Key is default allowed? {}", apiKeyContext.isDefaultAllowed());

        return apiKeyContext;
    }

    public Acl authorize(SecurityContext sc, Target target) {
        String id = sc.getIdentity();
        log.info("Authorizing access to {} for {}", target.getId(), id);

        Acl acl = store.getEntityAcl(target).orElseGet(() -> {
            if (sc.isDefaultAllowed()) {
                // No ACL set but user is allowed by default
                return Acl.forDefaultAllowed();
            }

            // We fetch default global ACLs, if any
            return store.getDefaultAcl(target.getPath()).orElseGet(Acl::forNone);
        });

        if (sc.isAdmin()) {
            sc.setAllowedModes(Acl.forAdmin().getDef().getModes());
        } else {
            sc.setAllowedModes(acl.getEntity(id).orElseGet(() -> {
                for (Map.Entry<String, Acl.Entry> pattern : acl.getPatterns().entrySet()) {
                    String resolvedPattern = pattern.getKey().replace("%BASE_URL%", "https://" + target.getId().getHost());
                    if (StringUtils.equals(resolvedPattern, id)) {
                        return pattern.getValue();
                    }
                }

                return acl.getDef();
            }).getModes());
        }

        if (!sc.getAllowedModes().contains(target.getAccessType())) {
            log.info("Entity {} was denied {} access to {}", Objects.isNull(id) ? "<Anonymous>" : id, target.getAccessType(), target.getHostAndPath());
            throw new ForbiddenException();
        }

        log.info("Authorized access to {} for {}", target.getId(), id);
        return acl;
    }

}
