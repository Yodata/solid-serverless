package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonElement;

import java.util.HashSet;
import java.util.Set;

public class PublishContext {

    private String topic;
    private JsonElement recipientJson;
    private final Set<String> recipients = new HashSet<>();

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public JsonElement getRecipientJson() {
        return recipientJson;
    }

    public void setRecipientJson(JsonElement recipientJson) {
        this.recipientJson = recipientJson;
    }

    public Set<String> getRecipients() {
        return recipients;
    }

}
