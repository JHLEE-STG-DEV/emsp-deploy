package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum EmspStatus {
    ACTIVE("ACTIVE"),
    LOCKED("LOCKED"),
    TERMINATED("TERMINATED");

  private String value;

  EmspStatus(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static EmspStatus fromValue(String text) {
    for (EmspStatus b : EmspStatus.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}