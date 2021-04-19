package io.yodata.ldp.solid.server.exception;

public class ForbiddenException extends RuntimeException {

    private boolean shouldBeLoggedFlag = true;

    public ForbiddenException() {
        this("This request is not allowed");
    }

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException notLogged() {
        shouldBeLoggedFlag = false;

        return this;
    }

    public boolean shouldBeLogged() {
        return shouldBeLoggedFlag;
    }

}
