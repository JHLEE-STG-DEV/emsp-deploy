package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProfileType {
  CHEAP("CHEAP"),
    FAST("FAST"),
    GREEN("GREEN"),
    REGULAR("REGULAR");

  private String value;

  ProfileType(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ProfileType fromValue(String text) {
    for (ProfileType b : ProfileType.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
