package io.yodata.ldp.solid.server.exception;

public class EncodingNotSupportedException extends RuntimeException {

    public static EncodingNotSupportedException forEncoding(String encoding) {
        return new EncodingNotSupportedException(encoding + " is not a supported encoding");
    }

    public EncodingNotSupportedException(String message) {
        super(message);
    }

}
