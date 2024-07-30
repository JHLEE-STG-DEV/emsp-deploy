package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EmspRfidOrderStatus {
    ORDERED("ORDERED"),
    IN_SHIPPING("IN_SHIPPING"),
    DELIVERED("DELIVERED"),
    CANCELLED("CANCELLED"),
    NOT_DELIVERED("NOT_DELIVERED");

  private String value;

  EmspRfidOrderStatus(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static EmspRfidOrderStatus fromValue(String text) {
    for (EmspRfidOrderStatus b : EmspRfidOrderStatus.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
