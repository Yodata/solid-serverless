import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.model.event.StorageAction;
import io.yodata.ldp.solid.server.subscription.subscriber.SubscriptionService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LambdaSubscriptionProcessor extends SubscriptionService implements RequestStreamHandler {

    private static final Logger log = LoggerFactory.getLogger(LambdaSubscriptionProcessor.class);

    public LambdaSubscriptionProcessor() {
        super(S3Store.getDefault());
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        String raw = IOUtils.toString(input, UTF_8);
        try {
            handleRequest(GsonUtil.parseObj(raw));
        } catch (JsonSyntaxException | IllegalStateException | IllegalArgumentException e) {
            log.error("Error when processing input data: {}", raw, e);
        }
    }

    private void handleRequest(JsonObject obj) {
        if (!obj.has("Records")) { // This is not from SNS/SQS
            log.debug("This is a regular message");
            process(obj);
        } else {
            log.debug("Processing as wrapped messages");
            JsonArray records = obj.getAsJsonArray("Records");
            records.forEach(recordEl -> {
                JsonObject record = recordEl.getAsJsonObject();
                if (record.has("Sns")) {
                    String dataRaw = record.get("Sns").getAsJsonObject().get("Message").getAsString();
                    log.debug("SNS data: {}", dataRaw);
                    handleAwsEvent(GsonUtil.parseObj(dataRaw));
                } else if (record.has("body")) {
                    String body = record.getAsJsonPrimitive("body").getAsString();
                    log.debug("SQS data: {}", body);
                    handleAwsEvent(GsonUtil.parseObj(body));
                } else {
                    throw new IllegalArgumentException("This is not a SNS or SQS message, cannot process");
                }
            });
        }
    }

    private void handleAwsEvent(JsonObject obj) {
        StorageAction action = GsonUtil.get().fromJson(obj, StorageAction.class);
        if (!StringUtils.equalsAny(action.getType(), StorageAction.Add, StorageAction.Update)) {
            log.debug("Storage action {} is not supported, skipping", action.getType());
            return;
        }

        if (!action.getObject().isPresent()) {
            log.warn("No object provided");
            return;
        }

        URI id = URI.create(action.getId());
        log.info("Processing {}", id);
        process(action.getObject().get());
    }

}
