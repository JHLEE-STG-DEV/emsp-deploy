package com.chargev.emsp.model.dto.pnc;

public enum Authorities {
    KEPCO("KEPCO"),
    HUBJECT("HUBJECT");

    private final String value;

    Authorities(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static Authorities fromValue(String value) {
        for (Authorities authority : Authorities.values()) {
            if (authority.value.equals(value)) {
                return authority;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + value);
    }
}
