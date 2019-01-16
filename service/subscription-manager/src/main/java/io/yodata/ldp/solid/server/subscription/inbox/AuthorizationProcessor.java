package io.yodata.ldp.solid.server.subscription.inbox;

import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.SecurityProcessor;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthorizationProcessor implements Consumer<InboxService.Wrapper> {

    private final Logger log = LoggerFactory.getLogger(AuthorizationProcessor.class);

    private S3Store store;
    private SecurityProcessor sec;

    public AuthorizationProcessor() {
        store = new S3Store();
        sec = new SecurityProcessor(store);
    }

    @Override
    public void accept(InboxService.Wrapper w) {
        AuthorizationMessage msg = GsonUtil.get().fromJson(w.message, AuthorizationMessage.class);
        if (StringUtils.isBlank(msg.getAccessTo())) {
            log.info("Authorization requested but no agent provided, invalid data. Skipping");
            return;
        }

        if (StringUtils.isBlank(msg.getAccessTo())) {
            log.info("Authorization requested but no target provided, invalid data. Skipping");
            return;
        }

        if (Objects.isNull(msg.getMode())) {
            log.info("Authorization request but no mode provided, invalid data. Skipping");
            return;
        }
        log.info("Processing request to grant {} to {} on {}", msg.getMode(), msg.getAgent(), msg.getAccessTo());

        // We check that the requester is allowed to control the target
        Target accessTo = Target.forPath(w.ev.getRequest().getTarget(), msg.getAccessTo());
        accessTo.setAccessType(AclMode.Control);

        try {
            // It is, we get the corresponding ACL allowing it
            Acl acl = sec.authorize(w.ev.getRequest().getSecurity(), accessTo);
            log.info("Request is allowed");

            // We check to see if an ACL is already set at the location or else we use the one found
            Acl aclNew = store.getEntityAcl(accessTo, false).orElse(acl);
            log.info("Pre-change ACL: {}", GsonUtil.toJson(aclNew));

            // We get the relevant entry info for the agent we want to grant permissions to
            Acl.Entry entry = aclNew.computeEntity(msg.getAgent());

            // We set the new modes
            entry.setModes(msg.getMode().stream().flatMap(mode -> {
                try {
                    return Stream.of(AclMode.valueOf(mode));
                } catch (IllegalArgumentException e) {
                    log.warn("Ignoring unknown ACL mode {}", mode);
                    return Stream.empty();
                }
            }).collect(Collectors.toList()));

            // We set the scope
            entry.setScope(msg.getScope());
            aclNew.getEntities().put(msg.getAgent(), entry);
            log.info("Post-change ACL: {}", GsonUtil.toJson(aclNew));

            // We save the ACL
            store.setEntityAcl(accessTo, aclNew);

            if (entry.getModes().contains(AclMode.Subscribe)) {
                log.info("Processing subscription");

                Subscription sub = new Subscription();
                sub.setAgent(msg.getAgent());
                sub.setObject(URI.create(msg.getAccessTo()).getPath()); // FIXME no good, we must also check the host
                sub.setScope(msg.getScope());

                List<Subscription> subs = store.getEntitySubscriptions(accessTo.getId());
                subs.add(sub);
                store.setEntitySubscriptions(accessTo.getId(), subs);
            }
        } catch (ForbiddenException e) {
            log.warn("Authorization request denied - The agent {} does not have Control permission on {}", w.ev.getRequest().getSecurity().getIdentity(), accessTo.getId());
        }

        log.info("ACL processing: end");
    }

}
