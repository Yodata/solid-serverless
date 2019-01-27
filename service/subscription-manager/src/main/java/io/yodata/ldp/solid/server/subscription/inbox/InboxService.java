package io.yodata.ldp.solid.server.subscription.inbox;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.model.Event.StorageAction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class InboxService {

    public static class Wrapper {
        public JsonObject message;
        public JsonObject scope;
        public StorageAction ev;
    }

    private final Logger log = LoggerFactory.getLogger(InboxService.class);

    private Map<String, Consumer<Wrapper>> typeProcessors;
    private ReflexProcessor reflexProcessor;

    public InboxService() {
        reflexProcessor = new ReflexProcessor();

        typeProcessors = new HashMap<>();
        typeProcessors.put("Authorization", new AuthorizationProcessor());
    }

    public void process(JsonObject eventJson) {
        log.info("Processing event data: {}", GsonUtil.toJson(eventJson));

        StorageAction event = GsonUtil.get().fromJson(eventJson, StorageAction.class);
        Wrapper c = new Wrapper();
        c.ev = event;

        if (!event.getObject().isPresent()) {
            log.warn("Event has no data, assuming non-RDF for now and skipping");
            return;
        }
        c.message = event.getObject().get();

        if (!StringUtils.equals(StorageAction.Add, event.getType())) {
            log.warn("Event is not about adding data, not supported for now, skipping");
            return;
        }

        c.scope = event.getRequest().getScope();

        String type = GsonUtil.findString(c.message, "type").orElse("");
        Consumer<Wrapper> consumer = typeProcessors.get(type);
        if (Objects.isNull(consumer)) {
            log.info("No processor for type {}, using REflex processor", type);
            consumer = reflexProcessor;
        }

        log.info("Using processor {} for type {}", consumer.getClass().getCanonicalName(), type);
        try {
            consumer.accept(c);
            log.info("Processing of inbox event finished");
        } catch (RuntimeException e) {
            log.warn("Error when processing inbox event: {}", e.getMessage(), e);
            throw e;
        }
    }

}