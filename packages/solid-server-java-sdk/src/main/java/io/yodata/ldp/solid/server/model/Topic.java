package io.yodata.ldp.solid.server.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class Topic {

    public static boolean matches(String ref, String match) {
        if (StringUtils.equals(ref, match)) {
            return true;
        }

        if (Objects.isNull(ref) || Objects.isNull(match)) {
            return false;
        }

        String subMatch = StringUtils.substringBefore(match, "#");
        return StringUtils.equals(ref, subMatch);
    }

}
