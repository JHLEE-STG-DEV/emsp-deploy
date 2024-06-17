package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PowerType {
    AC_1_PHASE("AC_1_PHASE"),
    AC_2_PHASE("AC_2_PHASE"),
    AC_2_PHASE_SPLIT("AC_2_PHASE_SPLIT"),
    AC_3_PHASE("AC_3_PHASE"),
    DC("DC");

    PowerType(String value) {
        this.value = value;
    }
    
    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static PowerType fromValue(String text) {
        for (PowerType b : PowerType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
