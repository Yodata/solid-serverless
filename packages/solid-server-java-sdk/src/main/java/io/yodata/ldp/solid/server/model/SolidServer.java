package io.yodata.ldp.solid.server.model;

import io.yodata.ldp.solid.server.model.container.ContainerHandler;
import io.yodata.ldp.solid.server.model.resource.ResourceHandler;

public class SolidServer {

    private final Store store;
    private final ContainerHandler folder;
    private final ResourceHandler file;
    private final SecurityProcessor sec;

    public SolidServer(Store store) {
        this.store = store;
        this.folder = new ContainerHandler(store);
        this.file = new ResourceHandler(store);
        this.sec = SecurityProcessor.getDefault(store);
    }

    public SecurityProcessor security() {
        return sec;
    }

    public Store store() {
        return store;
    }

    public Response head(Request in) {
        if (in.getTarget().getPath().endsWith("/")) {
            return folder.head(in);
        } else {
            return file.head(in);
        }
    }

    public Response get(Request in) {
        if (in.getTarget().getPath().endsWith("/")) {
            return folder.get(in);
        } else {
            return file.get(in);
        }
    }

    public Response delete(Request in) {
        if (in.getTarget().getPath().endsWith("/")) {
            return folder.delete(in);
        } else {
            return file.delete(in);
        }
    }

    public Response post(Request in) {
        if (in.getTarget().getPath().endsWith("/")) {
            return folder.post(in);
        } else {
            return file.post(in);
        }
    }

    public Response put(Request in) {
        if (in.getTarget().getPath().endsWith("/")) {
            return folder.put(in);
        } else {
            return file.put(in);
        }
    }

}
