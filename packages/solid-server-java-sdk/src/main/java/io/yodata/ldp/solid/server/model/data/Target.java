package io.yodata.ldp.solid.server.model.data;

import io.yodata.ldp.solid.server.model.security.AclMode;

import java.net.URI;
import java.util.regex.Pattern;

public class Target {

    public static Target forPath(Target t, String path) {
        Target tNew = forPath(t.getId(), path);
        tNew.setAccessType(t.getAccessType());
        return tNew;
    }

    public static Target forPath(URI base, String path) {
        Target t = new Target(base.resolve(path));
        t.setAccessType(AclMode.Read);
        return t;
    }

    protected URI id;
    protected String host;
    protected String path;
    protected AclMode accessType;

    public Target() {
        // stub
    }

    public Target(URI target) {
        setId(target);
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
        setHost(id.getHost());
        setPath(id.getPath());
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public AclMode getAccessType() {
        return accessType;
    }

    public void setAccessType(AclMode accessType) {
        this.accessType = accessType;
    }

    public String getHostAndPath() {
        return getHost() + getPath();
    }

    public boolean pathMatches(String globPattern) {
        String regexBuild = globPattern.replace("/*", "[^/]+(/?)").replace("*", "[^/]*(/?)");
        String regex = "^" + regexBuild + "$";
        System.out.println("Regex: " + regex);
        return Pattern.compile(regex).matcher(getPath()).matches();
    }

}
