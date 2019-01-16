package io.yodata.ldp.solid.server.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        this("The resource does not exist");
    }

    public NotFoundException(String message) {
        super(message);
    }

}
