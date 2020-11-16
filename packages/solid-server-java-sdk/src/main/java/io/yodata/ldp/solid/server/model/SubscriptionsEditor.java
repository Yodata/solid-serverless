package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class SubscriptionsEditor {

    private final JsonObject raw;

    public SubscriptionsEditor(JsonObject o) {
        raw = o;

        // We check if we have version for the format. If not, we set it to the current default version
        if (!raw.has("version")) {
            raw.addProperty("version", "1");
        }

        // We ensure that the items key exists and is of valid format
        JsonElement el = raw.remove("items");
        if (Objects.isNull(el) || !el.isJsonArray()) {
            el = new JsonArray();
        }
        raw.add("items", el);
    }

    public JsonObject getRaw() {
        return raw;
    }

    public JsonArray remove(JsonObject sub) {
        JsonArray items = GsonUtil.getArray(raw, "items");

        String type = GsonUtil.findString(sub, "type").orElse("");
        if (!StringUtils.equals("Subscription", type)) {
            return items;
        }

        String agent = GsonUtil.findString(sub, "agent").orElse("");
        if (StringUtils.isBlank(agent)) {
            return items;
        }

        String object = GsonUtil.findString(sub, "object").orElse("");
        if (StringUtils.isNotBlank(object)) {
            return items;
        }

        for (JsonElement el : GsonUtil.asList(items)) {
            if (!el.isJsonObject()) {
                continue;
            }

            type = GsonUtil.findString(sub, "type").orElse("");
            if (!StringUtils.equals("Subscription", type)) {
                continue;
            }

            object = GsonUtil.findString(sub, "object").orElse("");
            if (StringUtils.isNotBlank(object)) {
                continue;
            }

            JsonObject obj = el.getAsJsonObject();
            String typeMatch = GsonUtil.findString(obj, "type").orElse("");
            String agentMatch = GsonUtil.findString(obj, "agent").orElse("");
            if (!StringUtils.equals(type, typeMatch)) {
                continue;
            }

            if (!StringUtils.equals(agent, agentMatch)) {
                continue;
            }

            items.remove(el);
        }

        return items;
    }

    public void updateOrAdd(JsonObject sub) {
        remove(sub).add(sub);
    }
}
