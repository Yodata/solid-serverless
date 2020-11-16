package io.yodata;

import org.apache.commons.codec.binary.Base64;

import java.util.Objects;

public class Base64Util {

    public static String encode(byte[] value) {
        return Base64.encodeBase64URLSafeString(value);
    }

    public static byte[] decode(String value) {
        if (Objects.isNull(value)) {
            return new byte[0];
        }

        return Base64.decodeBase64(value);
    }

}
