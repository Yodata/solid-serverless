package io.yodata.ldp.solid.server.exception;

public class UnauthorizedException extends RuntimeException {

    @Deprecated
    public UnauthorizedException() {
        this("You are not authenticated to access this resource");
    }

    public UnauthorizedException(String message) {
        super(message);
    }

}
