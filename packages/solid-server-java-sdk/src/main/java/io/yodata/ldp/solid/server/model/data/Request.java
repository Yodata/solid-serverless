package io.yodata.ldp.solid.server.model.data;

import com.google.gson.JsonElement;
import io.yodata.Base64Util;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.model.SecurityContext;
import io.yodata.ldp.solid.server.model.security.Acl;
import io.yodata.ldp.solid.server.model.transform.Policies;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class Request {

    protected String id;
    protected Instant timestamp = Instant.now();
    protected SecurityContext security;
    protected String url;
    protected Target target;
    protected Target destination;
    protected Acl acl;
    protected String method;
    protected Map<String, List<String>> rawHeaders = new HashMap<>();
    protected Map<String, List<String>> parameters = new HashMap<>();
    protected String body;
    protected Policies policy = new Policies();
    protected boolean isBase64Encoded = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, List<String>> getHeaders() {
        return rawHeaders;
    }

    public Optional<String> getContentType() {
        return rawHeaders.getOrDefault("content-type", Collections.emptyList()).stream().findFirst();
    }

    public void setContentType(String contentType) {
        rawHeaders.put("content-type", Collections.singletonList(contentType));
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.rawHeaders = headers;
    }

    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    public List<String> getParamter(String name) {
        return parameters.getOrDefault(name, Collections.emptyList());
    }

    public Optional<String> getSingleParameter(String name) {
        return getParamter(name).stream().findFirst();
    }

    public void setParameters(Map<String, List<String>> parameters) {
        this.parameters = parameters;
    }

    public SecurityContext getSecurity() {
        return Optional.ofNullable(security).orElseGet(SecurityContext::asAnonymous);
    }

    public void setSecurity(SecurityContext security) {
        this.security = security;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
        url = target.getId().toString();
    }

    public Target getDestination() {
        return Optional.ofNullable(destination).orElse(target);
    }

    public void setDestination(Target destination) {
        this.destination = destination;
    }

    public Acl getAcl() {
        return Optional.ofNullable(acl).orElseGet(Acl::forNone);
    }

    public List<String> getScope() {
        return new ArrayList<>(getAcl().computeEntity(getSecurity().getIdentity()).getScope());
    }

    public void setAcl(Acl acl) {
        this.acl = acl;
    }

    public byte[] getBody() {
        return Base64Util.decode(body);
    }

    public String getBodyBase64() {
        return body;
    }

    public void setBody(byte[] bytes) {
        this.body = Base64Util.encode(bytes);
    }

    public void setBody(JsonElement el) {
        setBody(GsonUtil.toJson(el).getBytes(StandardCharsets.UTF_8));
        setContentType(MimeTypes.APPLICATION_JSON);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Policies getPolicy() {
        return policy;
    }

    public void setPolicy(Policies policy) {
        this.policy = policy;
    }

    public boolean isBase64Encoded() {
        return isBase64Encoded;
    }

    public void setBase64Encoded(boolean base64Encoded) {
        isBase64Encoded = base64Encoded;
    }

}
