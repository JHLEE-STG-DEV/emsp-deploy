package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ImageCategory {
    CHARGER("CHARGER"),
    ENTRANCE("ENTRANCE"),
    LOCATION("LOCATION"),
    NETWORK("NETWORK"),
    OPERATOR("OPERATOR"),
    OWNER("OWNER"),
    OTHER("OTHER");

    ImageCategory(String value) {
        this.value = value;
    }

    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ImageCategory fromValue(String text) {
        for (ImageCategory b : ImageCategory.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}