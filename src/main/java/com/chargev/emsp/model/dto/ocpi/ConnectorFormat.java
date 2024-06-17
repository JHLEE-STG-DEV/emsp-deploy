package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConnectorFormat {
    SOCKET("SOCKET"),
    CABLE("CABLE");

    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ConnectorFormat fromValue(String text) {
        for (ConnectorFormat b : ConnectorFormat.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
