package com.theboys.aoe.opp.info.utils;

import tools.jackson.databind.ObjectMapper;

public final class ObjectUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    private ObjectUtils() {
    }

    public static String toString(Object o) {
        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(o);
    }
}
