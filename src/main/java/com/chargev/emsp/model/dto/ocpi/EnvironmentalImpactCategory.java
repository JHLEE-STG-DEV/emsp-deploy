package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnvironmentalImpactCategory {
    NUCLEAR_WASTE("NUCLEAR_WASTE"),
    CARBON_DIOXIDE("CARBON_DIOXIDE");

    EnvironmentalImpactCategory(String value) {
        this.value = value;
    }

    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static EnvironmentalImpactCategory fromValue(String text) {
        for (EnvironmentalImpactCategory b : EnvironmentalImpactCategory.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}