package io.yodata.ldp.solid.server.model.data;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class Page {

    private String next;
    private List<JsonElement> contains = new ArrayList<>();

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public List<JsonElement> getContains() {
        return contains;
    }

    public void setContains(List<JsonElement> contains) {
        this.contains = contains;
    }

}
