package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnergySourceCategory {
    NUCLEAR("NUCLEAR"),
    GENERAL_FOSSIL("GENERAL_FOSSIL"),
    COAL("COAL"),
    GAS("GAS"),
    GENERAL_GREEN("GENERAL_GREEN"),
    SOLAR("SOLAR"),
    WIND("WIND"),
    WATER("WATER");

    EnergySourceCategory(String value) {
        this.value = value;
    }

    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static EnergySourceCategory fromValue(String text) {
        for (EnergySourceCategory b : EnergySourceCategory.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}