package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Subscriptions {

    public static List<Subscription> toList(Subscriptions subsMap) {
        List<Subscription> subsList = new ArrayList<>();
        for (SubscriptionEvent.Subscription sub : subsMap.getItems().values()) {
            if (!StringUtils.equals(sub.getType(), "Subscription")) {
                continue; // FIXME must be able to handle multi-format!
            }

            for (String topic : sub.getSubscribes()) {
                Subscription subNew = new Subscription();
                subNew.setObject("/event/topic/" + topic);
                subNew.setTarget(sub.getAgent());
                subsList.add(subNew);
            }
        }
        return subsList;
    }

    public static JsonObject toJson(Subscriptions subs) {
        JsonObject json = new JsonObject();

        json.addProperty("version", "1");
        JsonArray items = new JsonArray();
        for (SubscriptionEvent.Subscription sub : subs.getItems().values()) {
            items.add(GsonUtil.makeObj(sub));
        }
        json.add("items", items);

        return json;

    }

    private Map<String, SubscriptionEvent.Subscription> items = new HashMap<>();

    public List<Subscription> toList() {
        return Subscriptions.toList(this);
    }

    public JsonObject toJson() {
        return Subscriptions.toJson(this);
    }

    public Map<String, SubscriptionEvent.Subscription> getItems() {
        return items;
    }

}
