package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Facility {
    HOTEL("HOTEL"),
    RESTAURANT("RESTAURANT"),
    CAFE("CAFE"),
    MALL("MALL"),
    SUPERMARKET("SUPERMARKET"),
    SPORT("SPORT"),
    RECREATION_AREA("RECREATION_AREA"),
    NATURE("NATURE"),
    MUSEUM("MUSEUM"),
    BIKE_SHARING("BIKE_SHARING"),
    BUS_STOP("BUS_STOP"),
    TAXI_STAND("TAXI_STAND"),
    TRAM_STOP("TRAM_STOP"),
    METRO_STATION("METRO_STATION"),
    TRAIN_STATION("TRAIN_STATION"),
    AIRPORT("AIRPORT"),
    PARKING_LOT("PARKING_LOT"),
    CARPOOL_PARKING("CARPOOL_PARKING"),
    FUEL_STATION("FUEL_STATION"),
    WIFI("WIFI");

    Facility(String value) {
        this.value = value;
    }

    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static Facility fromValue(String text) {
        for (Facility b : Facility.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
