package io.yodata.ldp.solid.server.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        this("This request is not allowed");
    }

    public ForbiddenException(String message) {
        super(message);
    }

}
