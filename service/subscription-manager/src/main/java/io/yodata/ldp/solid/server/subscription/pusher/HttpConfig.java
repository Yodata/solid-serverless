package io.yodata.ldp.solid.server.subscription.pusher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpConfig {

    public static class Signature {

        private String type;
        private String salt;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSalt() {
            return salt;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }

    }

    private Map<String, List<String>> headers = new HashMap<>();
    private Signature sign = new Signature();

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public Signature getSign() {
        return sign;
    }

    public void getSign(Signature sign) {
        this.sign = sign;
    }

}
