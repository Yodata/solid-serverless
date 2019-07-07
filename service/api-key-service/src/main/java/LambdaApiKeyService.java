import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import io.yodata.ldp.solid.server.aws.store.S3Core;
import io.yodata.ldp.solid.server.security.ApiKeyAction;
import io.yodata.ldp.solid.server.security.ApiKeyManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LambdaApiKeyService implements RequestStreamHandler {

    private ApiKeyManager mgr;

    public LambdaApiKeyService() {
        this.mgr = new ApiKeyManager(S3Core.getDefault());
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        JsonObject outputJson = handleRequest(GsonUtil.parse(input, ApiKeyAction.class));
        IOUtils.write(GsonUtil.toJsonBytes(outputJson), output);
    }

    private JsonObject handleRequest(ApiKeyAction action) {
        if (StringUtils.equals("CreateAction", action.getType())) {
            String key = mgr.generateKey(action.getObject());
            return GsonUtil.makeObj("key", key);
        }

        throw new IllegalArgumentException("Unknown type: " + action.getType());
    }

}
