package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CommandResponseType {
  NOT_SUPPORTED("NOT_SUPPORTED"),
    REJECTED("REJECTED"),
    ACCEPTED("ACCEPTED"),
    UNKNOWN_SESSION("UNKNOWN_SESSION");

  private String value;

  CommandResponseType(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static CommandResponseType fromValue(String text) {
    for (CommandResponseType b : CommandResponseType.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
