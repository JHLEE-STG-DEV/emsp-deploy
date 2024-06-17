package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TariffDimensionType {
    ENERGY("ENERGY"),
    FLAT("FLAT"),
    PARKING_TIME("PARKING_TIME"),
    TIME("TIME");

    TariffDimensionType(String value) {
        this.value = value;
    }
    
    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static TariffDimensionType fromValue(String text) {
        for (TariffDimensionType b : TariffDimensionType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
