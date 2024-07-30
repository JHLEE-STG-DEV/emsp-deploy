package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EmspRfidStatusForMsp {
    AVAILABLE("AVAILABLE"),
    UNAVAILABLE("UNAVAILABLE");

  private String value;

  EmspRfidStatusForMsp(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static EmspRfidStatusForMsp fromValue(String text) {
    for (EmspRfidStatusForMsp b : EmspRfidStatusForMsp.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
