package io.yodata.ldp.solid.server.model;

import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.config.Configs;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.model.container.ContainerHandler;
import io.yodata.ldp.solid.server.model.resource.ResourceHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

import static io.yodata.ldp.solid.server.config.Configs.DOM_BASE;

public class SolidServer {

    private static final Logger log = LoggerFactory.getLogger(SolidServer.class);

    public static final BiFunction<String, String, Boolean> DomainMatching = (full, base) -> {
        if (StringUtils.isAnyBlank(full, base)) {
            return false;
        }

        if (StringUtils.equalsIgnoreCase(full, base)) {
            return true;
        }

        return StringUtils.endsWithIgnoreCase(full, "." + base);
    };

    public static final BiFunction<String, String, Boolean> DomainPatternMatching = (full, pattern) -> {
        if (StringUtils.isAnyBlank(full, pattern)) {
            return false;
        }

        if (StringUtils.equalsIgnoreCase(full, pattern)) {
            return true;
        }

        if (!StringUtils.startsWithIgnoreCase(pattern, "*.")) {
            return false;
        }

        pattern = StringUtils.substringAfter(pattern, "*.");
        return StringUtils.endsWithIgnoreCase(full, "." + pattern);
    };

    private final Store store;
    private final ContainerHandler folder;
    private final ResourceHandler file;
    private final SecurityProcessor sec;

    private final Set<String> contentTypesAllowed;

    public SolidServer(ServerBackend backend) {
        this.store = backend.store();
        this.folder = new ContainerHandler(backend);
        this.file = new ResourceHandler(backend);
        this.sec = SecurityProcessor.getDefault(store);

        if (StringUtils.isBlank(getBaseDomain())) {
            throw new IllegalStateException("Base domain cannot be empty/bank");
        }

        contentTypesAllowed = new HashSet<>();
        contentTypesAllowed.add(MimeTypes.APPLICATION_JSON);
        contentTypesAllowed.add(MimeTypes.APPLICATION_JSON_LD);
        contentTypesAllowed.add(MimeTypes.APPLICATION_YAML);

        try {
            contentTypesAllowed.addAll(Arrays.asList(StringUtils.split(IOUtils.resourceToString("/contentType/image.txt", StandardCharsets.UTF_8))));
        } catch (IOException e) {
            log.error("Unable to load images content types", e);
        }
    }

    public SecurityProcessor security() {
        return sec;
    }

    public Store store() {
        return store;
    }

    public String getBaseDomain() {
        return Configs.get().findOrBlank(DOM_BASE);
    }

    public boolean isServingDomain(String domain) {
        return DomainMatching.apply(domain, getBaseDomain());
    }

    private void validateContentType(Request in) {
        // Only check if we are possibly receiving something
        if (!StringUtils.equalsAny(in.getMethod(), "POST", "PUT", "PATCH")) {
            return;
        }

        // Only check if there is an actual body
        if (!in.hasBody()) {
            return;
        }

        String contentType = in.getContentType().orElse(MimeTypes.DEFAULT);
        if (contentTypesAllowed.stream().noneMatch(ct -> StringUtils.equalsIgnoreCase(ct, contentType))) {
            throw new ForbiddenException("Content-Type of " + contentType + " is not allowed");
        }
    }

    private void validate(Request in) {
        validateContentType(in);
    }

    public Response head(Request in) {
        validate(in);

        if (in.getTarget().getPath().endsWith("/")) {
            return folder.head(in);
        } else {
            return file.head(in);
        }
    }

    public Response get(Request in) {
        validate(in);

        if (in.getTarget().getPath().endsWith("/")) {
            return folder.get(in);
        } else {
            return file.get(in);
        }
    }

    public Response delete(Request in) {
        validate(in);

        if (in.getTarget().getPath().endsWith("/")) {
            return folder.delete(in);
        } else {
            return file.delete(in);
        }
    }

    public Response post(Request in) {
        validate(in);

        if (in.getTarget().getPath().endsWith("/")) {
            return folder.post(in);
        } else {
            return file.post(in);
        }
    }

    public Response put(Request in) {
        validate(in);

        if (in.getTarget().getPath().endsWith("/")) {
            return folder.put(in);
        } else {
            return file.put(in);
        }
    }

}
