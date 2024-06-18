package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AuthMethod {
    AUTH_REQUEST("AUTH_REQUEST"),
      COMMAND("COMMAND"),
      WHITELIST("WHITELIST");
  
    private String value;
  
    AuthMethod(String value) {
      this.value = value;
    }
  
    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }
  
    @JsonCreator
    public static AuthMethod fromValue(String text) {
      for (AuthMethod b : AuthMethod.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  