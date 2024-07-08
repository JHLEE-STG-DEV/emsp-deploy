package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Access {
    PUBLIC("PUBLIC"),
    SEMIPUBLIC("SEMIPUBLIC"),
    PRIVATE("PRIVATE");
  
    private String value;
  
    Access(String value) {
      this.value = value;
    }
  
    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }
  
    @JsonCreator
    public static Access fromValue(String text) {
      for (Access b : Access.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }