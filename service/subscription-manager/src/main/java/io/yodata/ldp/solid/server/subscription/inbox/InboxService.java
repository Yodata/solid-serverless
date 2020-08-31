package io.yodata.ldp.solid.server.subscription.inbox;

import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.model.SolidServer;
import io.yodata.ldp.solid.server.model.event.StorageAction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class InboxService {

    private static final Logger log = LoggerFactory.getLogger(InboxService.class);

    public static class Wrapper {
        public JsonObject message;
        public List<String> scope;
        public StorageAction ev;
    }

    private final Map<String, Consumer<Wrapper>> typeProcessors;

    public InboxService(SolidServer srv) {
        typeProcessors = new HashMap<>();
        typeProcessors.put(AuthorizationProcessor.Type, new AuthorizationProcessor(srv));
        typeProcessors.put(PublishProcessor.Type, new PublishProcessor(srv));

        AppAuthProcessor p = new AppAuthProcessor(srv);
        for (String type : AppAuthProcessor.Types) {
            typeProcessors.put(type, p);
        }
    }

    public void process(JsonObject eventJson) {
        log.debug("Processing event data: {}", GsonUtil.toJson(eventJson));

        StorageAction event = GsonUtil.get().fromJson(eventJson, StorageAction.class);
        Wrapper c = new Wrapper();
        c.ev = event;

        log.info("Processing {}", c.ev.getId());

        if (!event.getObject().isPresent()) {
            log.warn("Event has no data, assuming non-RDF for now and skipping");
            return;
        }
        c.message = event.getObject().get();

        if (!StringUtils.equalsAny(event.getType(), StorageAction.Add, StorageAction.Update, StorageAction.Delete)) {
            log.warn("Event is not about regular storage action, not supported for now, skipping");
            return;
        }

        c.scope = event.getRequest().getScope();

        String type = GsonUtil.findString(c.message, "type").orElse("");
        Consumer<Wrapper> consumer = typeProcessors.get(type);
        if (Objects.isNull(consumer)) {
            log.info("No processor for type {}, ignoring", type);
        } else {
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

}
