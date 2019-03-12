package io.yodata.ldp.solid.server.subscription.pusher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpConfig {

    private Map<String, List<String>> headers = new HashMap<>();

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

}
