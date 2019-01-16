package io.yodata.ldp.solid.server.exception;

public class EncodingNotSupportedException extends RuntimeException {

    public static void forEncoding(String encoding) {
        throw new EncodingNotSupportedException(encoding + " is not a supported encoding");
    }

    public EncodingNotSupportedException(String message) {
        super(message);
    }

}
