package io.yodata.ldp.solid.server.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.yodata.GsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Subscriptions {

    public static List<Subscription> toMatchList(Subscriptions subsMap) {
        List<Subscription> subsList = new ArrayList<>();
        for (Subscription sub : subsMap.items) {
            if (!StringUtils.equals(sub.getType(), "Subscription")) {
                // This is a raw object, we don't try to understand it
                subsList.add(sub);
            } else {
                if (!sub.getSubscribes().isEmpty()) {
                    List<String> topics = sub.getSubscribes().stream()
                            .map(t -> StringUtils.substringBefore(t, "#"))
                            .distinct()
                            .collect(Collectors.toList());

                    for (String topic : topics) {
                        Subscription subNew = new Subscription();
                        subNew.setAgent(sub.getAgent());
                        subNew.setObject("/event/topic/" + topic);
                        subNew.setTarget(sub.getAgent());
                        subsList.add(subNew);
                    }
                } else {
                    if (StringUtils.isNotBlank(sub.getObject())) {
                        subsList.add(sub);
                    }
                }
            }
        }
        return subsList;
    }

    public static Map<String, Subscription> toAgentMap(Subscriptions subs) {
        Map<String, Subscription> map = new HashMap<>();
        for (Subscription sub : subs.items) {
            if (StringUtils.isNotBlank(sub.getAgent()) && StringUtils.isBlank(sub.getObject())) {
                map.put(sub.getAgent(), sub);
            }
        }
        return map;
    }

    public static JsonObject toJson(Subscriptions subs) {
        JsonObject json = new JsonObject();

        json.addProperty("version", "1");
        JsonArray items = new JsonArray();
        for (Subscription sub : subs.items) {
            items.add(GsonUtil.makeObj(sub));
        }
        json.add("items", items);

        return json;
    }

    private final String version = "0";
    private final List<Subscription> items = new ArrayList<>();

    public String getVersion() {
        return version;
    }

    public List<Subscription> getItems() {
        return items;
    }

    public List<Subscription> toMatchList() {
        return Subscriptions.toMatchList(this);
    }

    public Map<String, Subscription> toAgentMap() {
        return Subscriptions.toAgentMap(this);
    }

    public JsonObject toJson() {
        return Subscriptions.toJson(this);
    }

}
