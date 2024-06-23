package com.chargev.emsp.model.dto.pnc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CertStatus {
    NORMAL("NORMAL"),
    TERMINATION("TERMINATION"),
    EXPIRED("EXPIRED"),
    DELETED("DELETED");

    private final String value;

    CertStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CertStatus fromValue(String value) {
        for (CertStatus status : CertStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown cert status: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
