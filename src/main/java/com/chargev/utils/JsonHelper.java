package com.chargev.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHelper {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    private JsonHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> String objectToString(T objectData) {

        String asString = "";
        try {
            asString = mapper.writeValueAsString(objectData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return asString;
    }
    public static <T> T stringToObject(String jsonString, Class<T> classType) {
        T object = null;
        try {
            object = mapper.readValue(jsonString, classType);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return object;
    }
}
