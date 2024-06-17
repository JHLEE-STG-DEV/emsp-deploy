package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CdrDimensionType {
    CURRENT("CURRENT"),
    ENERGY("ENERGY"),
    ENERGY_EXPORT("ENERGY_EXPORT"),
    ENERGY_IMPORT("ENERGY_IMPORT"),
    MAX_CURRENT("MAX_CURRENT"),
    MIN_CURRENT("MIN_CURRENT"),
    MAX_POWER("MAX_POWER"),
    MIN_POWER("MIN_POWER"),
    PARKING_TIME("PARKING_TIME"),
    POWER("POWER"),
    RESERVATION_TIME("RESERVATION_TIME"),
    STATE_OF_CHARGE("STATE_OF_CHARGE"),
    TIME("TIME");

    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static CdrDimensionType fromValue(String text) {
        for (CdrDimensionType b : CdrDimensionType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
