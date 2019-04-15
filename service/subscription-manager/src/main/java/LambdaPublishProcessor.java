import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.store.S3Store;
import io.yodata.ldp.solid.server.subscription.publisher.Publisher;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LambdaPublishProcessor extends Publisher implements RequestStreamHandler {

    private static final Logger log = LoggerFactory.getLogger(LambdaPublishProcessor.class);

    public LambdaPublishProcessor() {
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
            handle(obj);
        } else {
            log.debug("Processing as wrapped messages");
            JsonArray records = obj.getAsJsonArray("Records");
            records.forEach(recordEl -> {
                JsonObject record = recordEl.getAsJsonObject();
                if (record.has("Sns")) {
                    String dataRaw = record.get("Sns").getAsJsonObject().get("Message").getAsString();
                    log.debug("SNS data: {}", dataRaw);
                    handle(GsonUtil.parseObj(dataRaw));
                } else if (record.has("body")) {
                    String body = record.getAsJsonPrimitive("body").getAsString();
                    log.debug("SQS data: {}", body);
                    handle(GsonUtil.parseObj(body));
                } else {
                    throw new IllegalArgumentException("This is not a SNS or SQS message, cannot process");
                }
            });
        }
    }

}