package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ParkingType {
    ALONG_MOTORWAY("ALONG_MOTORWAY"),
    PARKING_GARAGE("PARKING_GARAGE"),
    PARKING_LOT("PARKING_LOT"),
    ON_DRIVEWAY("ON_DRIVEWAY"),
    ON_STREET("ON_STREET"),
    UNDERGROUND_GARAGE("UNDERGROUND_GARAGE");

    ParkingType(String value) {
        this.value = value;
    }

    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ParkingType fromValue(String text) {
        for (ParkingType b : ParkingType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
