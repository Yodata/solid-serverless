package io.yodata.ldp.solid.server.model.store.fs;

import java.util.ArrayList;
import java.util.List;

public class BasicFsPage implements FsPage {

    private List<String> elements = new ArrayList<>();
    private String next;

    public void addElement(String element) {
        elements.add(element);
    }

    @Override
    public List<String> getElements() {
        return elements;
    }

    @Override
    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

}
