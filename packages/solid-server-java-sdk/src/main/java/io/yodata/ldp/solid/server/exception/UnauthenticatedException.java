package io.yodata.ldp.solid.server.exception;

public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException() {
        this("You are not authenticated to access this resource");
    }

    public UnauthenticatedException(String message) {
        super(message);
    }

}
