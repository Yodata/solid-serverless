package io.yodata;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Base64Util {

    public static String encode(byte[] value) {
        return Base64.encodeBase64URLSafeString(value);
    }

    public static String encodeUtf8(String value) {
        return encode(value.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decode(String value) {
        if (Objects.isNull(value)) {
            return new byte[0];
        }

        return Base64.decodeBase64(value);
    }

    public static String decodeUtf8(String value) {
        return new String(decode(value), StandardCharsets.UTF_8);
    }

}
