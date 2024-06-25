package com.chargev.emsp.model.dto.response;

public enum PncResponseResult {
    SUCCESS("SUCCESS"),
    FAIL("FAIL");

    private final String value;

    PncResponseResult(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
