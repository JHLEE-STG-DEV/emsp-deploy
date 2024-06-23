package com.chargev.emsp.model.dto.pnc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HashAlgorithm {
    SHA256("SHA-256"),
    SHA384("SHA-384"),
    SHA512("SHA-512");

    private final String algorithm;

    HashAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @JsonValue
    public String getAlgorithm() {
        return algorithm;
    }

    @JsonCreator
    public static HashAlgorithm fromValue(String value) {
        for (HashAlgorithm hashAlgorithm : HashAlgorithm.values()) {
            if (hashAlgorithm.algorithm.equalsIgnoreCase(value)) {
                return hashAlgorithm;
            }
        }
        throw new IllegalArgumentException("Unknown hash algorithm: " + value);
    }

    @Override
    public String toString() {
        return algorithm;
    }
}
