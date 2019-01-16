import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.subscription.inbox.InboxService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LambdaInboxProcessor extends InboxService implements RequestStreamHandler {

    private final Logger log = LoggerFactory.getLogger(LambdaInboxProcessor.class);

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        String raw = IOUtils.toString(input, UTF_8);
        try {
            handleRequest(GsonUtil.parseObj(raw));
        } catch (JsonSyntaxException | IllegalStateException | IllegalArgumentException e) {
            log.error("Invalid JSON object received: {}", raw);
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
}
