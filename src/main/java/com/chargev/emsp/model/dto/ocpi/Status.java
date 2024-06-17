package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
    AVAILABLE("AVAILABLE"),
    BLOCKED("BLOCKED"),
    CHARGING("CHARGING"),
    INOPERATIVE("INOPERATIVE"),
    OUTOFORDER("OUTOFORDER"),
    PLANNED("PLANNED"),
    REMOVED("REMOVED"),
    RESERVED("RESERVED"),
    UNKNOWN("UNKNOWN");

    Status(String value) {
        this.value = value;
    }

    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static Status fromValue(String text) {
        for (Status b : Status.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }
}
