import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.handler.container.ContainerHandler;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.model.*;
import io.yodata.ldp.solid.server.model.event.StorageAction;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class App implements RequestStreamHandler {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    private ContainerHandler storeHandler;
    private URI mainPod;
    private boolean toSpecificPod;

    public App() {
        storeHandler = new ContainerHandler(S3Store.getDefault());

        String mainPodUriRaw = System.getenv("BASE_POD_URI");
        if (StringUtils.isNotEmpty(mainPodUriRaw)) {
            mainPod = URI.create(mainPodUriRaw);
        }

        String toSpecificPodRaw = System.getenv("TO_SPECIFIC_POD");
        if (StringUtils.isEmpty(toSpecificPodRaw)) {
            toSpecificPodRaw = "true";
        }
        toSpecificPod = StringUtils.equals(toSpecificPodRaw, "true");
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        String raw = IOUtils.toString(input, UTF_8);
        try {
            handleRequest(GsonUtil.parseObj(raw));
        } catch (JsonSyntaxException | IllegalStateException | IllegalArgumentException e) {
            log.error("Invalid JSON object received - Data: {}", raw, e);
        }
    }

    private void handleRequest(JsonObject obj) {
        if (!obj.has("Records")) { // This is not from SNS/SQS
            log.info("This is a regular message");
            process(obj);
        } else {
            log.info("Processing as wrapped messages");
            JsonArray records = obj.getAsJsonArray("Records");
            records.forEach(recordEl -> {
                JsonObject record = recordEl.getAsJsonObject();
                if (record.has("Sns")) {
                    String dataRaw = record.get("Sns").getAsJsonObject().get("Message").getAsString();
                    log.info("SNS data: {}", dataRaw);
                    process(GsonUtil.parseObj(dataRaw));
                } else if (record.has("body")) {
                    String body = record.getAsJsonPrimitive("body").getAsString();
                    log.info("SQS data: {}", body);
                    process(GsonUtil.parseObj(body));
                } else {
                    throw new IllegalArgumentException("This is not a SNS or SQS message, cannot process");
                }
            });
        }
    }

    private void process(JsonObject obj) {
        StorageAction event = GsonUtil.get().fromJson(obj, StorageAction.class);
        log.info("Processing event {}", event.getId());
        URI objId = event.getRequest().getTarget().getId();
        String podId = objId.resolve("/profile/card#me").toString();

        JsonObject actionNew = new JsonObject();
        actionNew.addProperty(ActionPropertyKey.Type.getId(), event.getType());
        actionNew.addProperty(ActionPropertyKey.Timestamp.getId(), event.getRequest().getTimestamp().toEpochMilli());
        actionNew.addProperty(ActionPropertyKey.Instrument.getId(), event.getRequest().getSecurity().getInstrument());
        event.getRequest().getSecurity().getAgent().ifPresent(a -> actionNew.addProperty(ActionPropertyKey.Agent.getId(), a));
        if (event.getObject().isPresent()) {
            actionNew.add(ActionPropertyKey.Object.getId(), event.getObject().get());
        } else {
            actionNew.addProperty(ActionPropertyKey.Object.getId(), event.getId());
        }

        JsonObject notification = new JsonObject();
        notification.addProperty("topic", "realestate/profile#" + event.getType().toLowerCase().replace("action",""));
        notification.addProperty(ActionPropertyKey.Type.getId(), "Notification");
        notification.addProperty(ActionPropertyKey.Timestamp.getId(), Instant.now().toEpochMilli());
        notification.addProperty(ActionPropertyKey.Instrument.getId(), podId);
        notification.add("data", actionNew);

        SecurityContext sc = new SecurityContext();
        sc.setAgent(podId);
        sc.setInstrument(podId);
        sc.setAdmin(true);

        if (toSpecificPod) {
            send(objId, sc, notification);
        }

        if (Objects.nonNull(mainPod)) {
            send(mainPod, sc,notification);
        }
    }

    private void send(URI base, SecurityContext sc, JsonObject notification) {
        Target target = Target.forPath(new Target(base), "/event/topic/realestate/profile/");
        Request r = new Request();
        r.setMethod("POST");
        r.setTarget(target);
        r.setSecurity(sc);
        r.setBody(notification);

        Response res = storeHandler.post(r);
        String eventId = GsonUtil.parseObj(res.getBody().get()).get("id").getAsString();
        log.info("Topic event was saved at {}", eventId);
    }

}
