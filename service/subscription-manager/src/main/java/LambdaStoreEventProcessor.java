import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.AwsServerBackend;
import io.yodata.ldp.solid.server.aws.AmazonS3Config;
import io.yodata.ldp.solid.server.aws.event.GenericProcessor;
import io.yodata.ldp.solid.server.model.SolidServer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LambdaStoreEventProcessor implements RequestStreamHandler {

    private static final Logger log = LoggerFactory.getLogger(LambdaStoreEventProcessor.class);

    private final GenericProcessor svc;

    public LambdaStoreEventProcessor() {
        AmazonS3Config.register();
        svc = new GenericProcessor(new SolidServer(new AwsServerBackend()));
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        String raw = IOUtils.toString(input, UTF_8);
        try {
            handleRequest(GsonUtil.parseObj(raw));
        } catch (JsonSyntaxException | IllegalStateException | IllegalArgumentException e) {
            log.error("Invalid JSON object received: {}", raw, e);
        }
    }

    private void handleRequest(JsonObject obj) {
        if (!obj.has("Records")) { // This is not from SNS/SQS
            log.debug("This is a regular message");
            svc.handleEvent(obj);
        } else {
            log.debug("Processing as wrapped messages");
            JsonArray records = obj.getAsJsonArray("Records");
            records.forEach(recordEl -> {
                JsonObject record = recordEl.getAsJsonObject();
                if (record.has("Sns")) {
                    String dataRaw = record.get("Sns").getAsJsonObject().get("Message").getAsString();
                    log.debug("SNS data: {}", dataRaw);
                    svc.handleEvent(GsonUtil.parseObj(dataRaw));
                } else if (record.has("body")) {
                    String body = record.getAsJsonPrimitive("body").getAsString();
                    log.debug("SQS data: {}", body);

                    JsonObject message = GsonUtil.parseObj(body);
                    if (message.has("TopicArn")) { // This is SNS to SQS without raw delivery
                        message = GsonUtil.parseObj(GsonUtil.findString(obj, "Message").orElse("{}"));
                    }

                    try {
                        svc.handleEvent(message);
                    } catch (NullPointerException e) {
                        log.warn("Invalid JSON data: {}", body, e);
                    }
                } else {
                    throw new IllegalArgumentException("This is not a SNS or SQS message, cannot process");
                }
            });
        }
    }

}
