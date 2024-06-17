package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum ReservationRestrictionType {
    RESERVATION("RESERVATION"),
    RESERVATION_EXPIRES("RESERVATION_EXPIRES");

    ReservationRestrictionType(String value) {
        this.value = value;
    }

    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ReservationRestrictionType fromValue(String text) {
        for (ReservationRestrictionType b : ReservationRestrictionType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
