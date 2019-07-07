package io.yodata.ldp.solid.server.aws.handler.resource.input;

import io.yodata.ldp.solid.server.aws.handler.RequestCheckProcessor;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.model.data.Exchange;
import io.yodata.ldp.solid.server.model.security.AclMode;

public class ResourceRequestCheckProcessor extends RequestCheckProcessor {

    @Override
    public void put(Exchange ex) {
        // We don't allow direct editing of ACL for now
        if (ex.getRequest().getTarget().getPath().endsWith(".acl") && !ex.getRequest().getSecurity().can(AclMode.Control)) {
            throw new ForbiddenException("Direct ACL modification is not allowed");
        }

        super.put(ex);
    }

    @Override
    public void delete(Exchange ex) {
        // We don't allow direct editing of ACL for now
        if (ex.getRequest().getTarget().getPath().endsWith(".acl") && !ex.getRequest().getSecurity().can(AclMode.Control)) {
            throw new ForbiddenException("Direct ACL modification is not allowed");
        }

        super.delete(ex);
    }

}
