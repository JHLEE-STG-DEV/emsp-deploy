package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum TariffType {
    AD_HOC_PAYMENT("AD_HOC_PAYMENT"),
    PROFILE_CHEAP("PROFILE_CHEAP"),
    PROFILE_FAST("PROFILE_FAST"),
    PROFILE_GREEN("PROFILE_GREEN"),
    REGULAR("REGULAR");

    private final String value;

    TariffType(String value) {
        this.value = value;
    }
    
    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static TariffType fromValue(String text) {
        for (TariffType b : TariffType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
