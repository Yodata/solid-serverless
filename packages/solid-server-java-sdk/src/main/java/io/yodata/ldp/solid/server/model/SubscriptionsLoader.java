package io.yodata.ldp.solid.server.model;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionsLoader {

    private final List<SubscriptionEvent.Subscription> items = new ArrayList<>();

    public List<SubscriptionEvent.Subscription> getItems() {
        return items;
    }

    public Subscriptions toMap() {
        Subscriptions subs = new Subscriptions();
        for (SubscriptionEvent.Subscription sub : items) {
            subs.getItems().put(sub.getAgent(), sub);
        }
        return subs;
    }

}
