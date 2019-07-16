/*
 * Copyright 2019 YoData, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.yodata.ldp.solid.server.model.core;

import io.yodata.GsonUtil;
import io.yodata.Optionals;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.exception.UnauthenticatedException;
import io.yodata.ldp.solid.server.model.SecurityContext;
import io.yodata.ldp.solid.server.model.data.Target;
import io.yodata.ldp.solid.server.model.security.Acl;
import io.yodata.ldp.solid.server.model.store.Store;
import io.yodata.ldp.solid.server.model.store.fs.FsElement;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BasicAuth {

    private static final Logger log = LoggerFactory.getLogger(BasicAuth.class);

    private String podId;
    private Store store;

    public BasicAuth(String podId, Store store) {
        this.podId = podId;
        this.store = store;
    }

    public Optional<SecurityContext> findForApiKey(String apiKey) {
        log.info("Fetching data for API key {}", apiKey);

        String keyPath = "/security/api/key/" + apiKey;
        Optional<FsElement> element = Optionals.get(
                () -> store.forPod(podId).tryGet(keyPath),
                () -> store.forGlobal().tryGet(keyPath)
        );
        if (!element.isPresent()) {
            log.info("API key not found");
            return Optional.empty();
        }

        log.info("API key found");
        return Optional.of(GsonUtil.parse(element.get().getData(), SecurityContext.class));
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
            throw new UnauthenticatedException("No API key provided");
        }

        log.info("API Key: {}", key);
        SecurityContext apiKeyContext = findForApiKey(key).orElseGet(SecurityContext::new);
        if (apiKeyContext.isAnonymous()) {
            log.info("API key is unknown");
            throw new UnauthenticatedException("Invalid API Key");
        }

        log.info("API Key Agent: {}", apiKeyContext.getAgent().orElse("<Empty>"));
        log.info("API Key Instrument: {}", apiKeyContext.getInstrument());
        log.info("API Key is admin? {}", apiKeyContext.isAdmin());
        log.info("API Key is default allowed? {}", apiKeyContext.isDefaultAllowed());

        return apiKeyContext;
    }

    public SecurityContext authenticate(String token) {
        SecurityContext apiKeyContext = findForApiKey(token).orElseGet(SecurityContext::new);
        if (apiKeyContext.isAnonymous()) {
            log.info("API key is unknown");
            throw new UnauthenticatedException("Invalid API Key");
        }

        log.info("API Key Agent: {}", apiKeyContext.getAgent().orElse("<Empty>"));
        log.info("API Key Instrument: {}", apiKeyContext.getInstrument());
        log.info("API Key is admin? {}", apiKeyContext.isAdmin());
        log.info("API Key is default allowed? {}", apiKeyContext.isDefaultAllowed());

        return apiKeyContext;
    }

    public Acl authorize(SecurityContext sc, Target target) {
        Acl finalAcl = null;
        Iterator<Path> p = Paths.get(target.getId()).iterator();
        while (p.hasNext()) {
            Path q = p.next();

            String keyPath = q + ".acl";
            Optional<FsElement> element = Optionals.get(
                    () -> store.forPod(podId).tryGet(keyPath),
                    () -> store.forDefault().tryGet(keyPath)
            );

            if (element.isPresent()) {
                // We found an ACL
                Acl acl = finalAcl = GsonUtil.parse(element.get().getData(), Acl.class);

                Optional<Acl.Entry> forEntity = acl.getEntity(sc.getIdentity());
                if (forEntity.isPresent()) {
                    // There is a direct entry for requester in the ACL, using it
                    sc.setAllowedModes(forEntity.get().getModes());
                    break;
                }

                for (Map.Entry<String, Acl.Entry> pattern : acl.getPatterns().entrySet()) {
                    String resolvedPattern = pattern.getKey().replace("%BASE_URL%", "https://" + target.getId().getHost());
                    if (StringUtils.equals(resolvedPattern, sc.getIdentity())) {
                        // There is a pattern entry for requester in the ACL, using it
                        sc.setAllowedModes(pattern.getValue().getModes());
                        break;
                    }
                }

                // We found nothing, we check if the ACL is marked as final
                if (acl.isFinal()) {
                    // it is, so we end here
                    break;
                }
            }
        }

        if (Objects.isNull(finalAcl)) {
            finalAcl = Acl.forNone();
        }

        if (sc.isDefaultAllowed()) {
            sc.getAllowedModes().addAll(Acl.forDefaultAllowed().getDef().getModes());
        }

        if (!sc.getAllowedModes().contains(target.getAccessType()) && !sc.isAdmin()) {
            log.info("Entity {} was denied {} access to {}", Objects.isNull(sc.getIdentity()) ? "<Anonymous>" : sc.getIdentity(), target.getAccessType(), target.getHostAndPath());
            throw new ForbiddenException();
        }

        log.info("Authorized access to {} for {}", target.getId(), sc.getIdentity());
        return finalAcl;
    }

}
