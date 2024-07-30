package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EmspAccountStatusReason {
    ADMIN_LOCKED("ADMIN_LOCKED"),
    OUTSTANDING_PAYMENT("OUTSTANDING_PAYMENT"),
    IN_TERMINATE("IN_TERMINATE");

  private String value;

  EmspAccountStatusReason(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static EmspAccountStatusReason fromValue(String text) {
    for (EmspAccountStatusReason b : EmspAccountStatusReason.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
