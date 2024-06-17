package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum TokenType {
    AD_HOC_USER("AD_HOC_USER"),
    APP_USER("APP_USER"),
    OTHER("OTHER"),
    RFID("RFID");

    TokenType(String value) {
        this.value = value;
    }

    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static TokenType fromValue(String text) {
        for (TokenType b : TokenType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
