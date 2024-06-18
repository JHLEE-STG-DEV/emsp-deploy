package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SessionStatus {
  ACTIVE("ACTIVE"),
    COMPLETED("COMPLETED"),
    INVALID("INVALID"),
    PENDING("PENDING"),
    RESERVATION("RESERVATION");

  private String value;

  SessionStatus(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static SessionStatus fromValue(String text) {
    for (SessionStatus b : SessionStatus.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
