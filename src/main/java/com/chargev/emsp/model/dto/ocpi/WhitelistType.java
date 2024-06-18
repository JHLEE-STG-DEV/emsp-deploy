package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WhitelistType {
    ALWAYS("ALWAYS"),
      ALLOWED("ALLOWED"),
      ALLOWED_OFFLINE("ALLOWED_OFFLINE"),
      NEVER("NEVER");
  
    private String value;
  
    WhitelistType(String value) {
      this.value = value;
    }
  
    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }
  
    @JsonCreator
    public static WhitelistType fromValue(String text) {
      for (WhitelistType b : WhitelistType.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  