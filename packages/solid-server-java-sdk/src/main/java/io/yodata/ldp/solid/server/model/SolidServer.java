package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.MimeTypes;
import io.yodata.ldp.solid.server.config.Configs;
import io.yodata.ldp.solid.server.exception.BadRequestException;
import io.yodata.ldp.solid.server.exception.EncodingNotSupportedException;
import io.yodata.ldp.solid.server.exception.ForbiddenException;
import io.yodata.ldp.solid.server.model.container.ContainerHandler;
import io.yodata.ldp.solid.server.model.resource.ResourceHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
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

    public static String getPublishPath() {
        return "/publish/";
    }

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
        contentTypesAllowed.add(MimeTypes.APPLICATION_ZIP);

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

    public PublishContext getPublishContext(JsonObject message) {
        PublishContext c = new PublishContext();

        c.setTopic(GsonUtil.getStringOrNull(message, "topic"));

        Optional<JsonElement> rOpt = GsonUtil.findElement(message, "recipient");
        if (!rOpt.isPresent()) {
            log.warn("Message did not contain any recipient to send to");
        } else {
            c.setRecipientJson(rOpt.get());

            if (c.getRecipientJson().isJsonArray()) {
                c.getRecipients().addAll(GsonUtil.asList(c.getRecipientJson().getAsJsonArray(), String.class));
            }
            if (c.getRecipientJson().isJsonPrimitive()) {
                c.getRecipients().add(c.getRecipientJson().getAsJsonPrimitive().getAsString());
            }

            GsonUtil.findElement(message, "source").ifPresent(sEl -> {
                if (sEl.isJsonArray()) {
                    c.getRecipients().addAll(GsonUtil.asList(sEl.getAsJsonArray(), String.class));
                }
                if (sEl.isJsonPrimitive()) {
                    c.getRecipients().add(sEl.getAsJsonPrimitive().getAsString());
                }
            });
        }

        return c;
    }

    public boolean canPublish(URI senderId, URI receiverId, String topic) {
        Subscriptions subs = store.getSubscriptions(receiverId);
        return canPublish(senderId, subs, topic).orElseGet(() -> {
            Subscriptions globalSubs = store.getGlobalSubscriptions();
            return canPublish(senderId, globalSubs, topic).orElse(true);
        });
    }

    public Optional<Boolean> canPublish(URI senderId, Subscriptions subs, String topic) {
        Optional<Subscription> subOpt = Optional.ofNullable(subs.toAgentMap().get(senderId.toString()));
        if (!subOpt.isPresent()) {
            return Optional.empty();
        } else {
            Subscription sub = subOpt.get();
            return Optional.of(sub.getPublishes().stream().anyMatch(t -> Topic.matches(t, topic)));
        }
    }

    private void validatePublish(Request in) {
        if (!StringUtils.startsWithIgnoreCase(in.getTarget().getPath(), getPublishPath())) {
            return;
        }

        if (!in.hasBody()) {
            throw new BadRequestException("No request body was sent");
        }

        String contentType = in.getContentType().orElse(MimeTypes.DEFAULT);
        if (!StringUtils.equals(MimeTypes.APPLICATION_JSON, contentType)) {
            throw EncodingNotSupportedException.forEncoding(contentType);
        }

        JsonObject msg;
        try {
            msg = GsonUtil.parseObj(in.getBody());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("request body is not valid JSON object");
        }

        PublishContext c = getPublishContext(msg);
        if (StringUtils.isBlank(c.getTopic())) {
            throw new BadRequestException("No valid topic found");
        }

        if (c.getRecipients().isEmpty()) {
            throw new BadRequestException("No recipient found");
        }

        URI senderId = Target.forProfileCard(in.getTarget().getId()).getId();
        for (String recipient : c.getRecipients()) {
            URI recipientId;
            try {
                recipientId = new URI(recipient);
            } catch (URISyntaxException e) {
                throw new BadRequestException("Invalid ID: " + recipient);
            }

            if (!canPublish(senderId, recipientId, c.getTopic())) {
                throw new ForbiddenException("Not authorized to publish to " + recipient + " on topic " + c.getTopic());
            }
        }
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

        validatePublish(in);

        if (in.getTarget().getPath().endsWith("/")) {
            return folder.post(in);
        } else {
            return file.post(in);
        }
    }

    public Response put(Request in) {
        validate(in);

        validatePublish(in);

        if (in.getTarget().getPath().endsWith("/")) {
            return folder.put(in);
        } else {
            return file.put(in);
        }
    }

}
