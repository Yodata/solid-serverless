package io.yodata.ldp.solid.server.security;

import io.yodata.ldp.solid.server.model.SecurityContext;

public class ApiKeyAction {

    private String type;
    private SecurityContext object;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SecurityContext getObject() {
        return object;
    }

    public void setObject(SecurityContext object) {
        this.object = object;
    }

}
