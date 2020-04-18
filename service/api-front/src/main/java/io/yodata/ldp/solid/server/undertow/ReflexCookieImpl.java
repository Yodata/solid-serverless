package io.yodata.ldp.solid.server.undertow;

import io.undertow.UndertowMessages;
import io.undertow.server.handlers.Cookie;

import java.util.Date;
import java.util.Locale;

public class ReflexCookieImpl implements Cookie {

    private final String name;
    private String value;
    private String path;
    private String domain;
    private Integer maxAge;
    private Date expires;
    private boolean discard;
    private boolean secure;
    private boolean httpOnly;
    private int version = 0;
    private String comment;
    private boolean sameSite;
    private String sameSiteMode;


    public ReflexCookieImpl(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public ReflexCookieImpl(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public ReflexCookieImpl setValue(final String value) {
        this.value = value;
        return this;
    }

    public String getPath() {
        return path;
    }

    public ReflexCookieImpl setPath(final String path) {
        this.path = path;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public ReflexCookieImpl setDomain(final String domain) {
        this.domain = domain;
        return this;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public ReflexCookieImpl setMaxAge(final Integer maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public boolean isDiscard() {
        return discard;
    }

    public ReflexCookieImpl setDiscard(final boolean discard) {
        this.discard = discard;
        return this;
    }

    public boolean isSecure() {
        return secure;
    }

    public ReflexCookieImpl setSecure(final boolean secure) {
        this.secure = secure;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public ReflexCookieImpl setVersion(final int version) {
        this.version = version;
        return this;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public ReflexCookieImpl setHttpOnly(final boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    public Date getExpires() {
        return expires;
    }

    public ReflexCookieImpl setExpires(final Date expires) {
        this.expires = expires;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Cookie setComment(final String comment) {
        this.comment = comment;
        return this;
    }

    @Override
    public boolean isSameSite() {
        return sameSite;
    }

    @Override
    public Cookie setSameSite(final boolean sameSite) {
        this.sameSite = sameSite;
        return this;
    }

    @Override
    public String getSameSiteMode() {
        return sameSiteMode;
    }

    @Override
    public Cookie setSameSiteMode(final String sameSiteMode) {
        if (sameSiteMode != null) {
            switch (sameSiteMode.toLowerCase(Locale.ENGLISH)) {
                case "strict":
                    this.setSameSite(true);
                    this.sameSiteMode = "Strict";
                    break;
                case "lax":
                    this.setSameSite(true);
                    this.sameSiteMode = "Lax";
                    break;
                case "none":
                    this.setSameSite(true);
                    this.sameSiteMode = "None";
                    break;
                default:
                    throw UndertowMessages.MESSAGES.invalidSameSiteMode(sameSiteMode);
            }
        }
        return this;
    }
}
