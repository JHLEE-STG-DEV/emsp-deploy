package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ConnectorFormat {
    SOCKET("SOCKET"),
      CABLE("CABLE");
  
    private String value;
  
    ConnectorFormat(String value) {
      this.value = value;
    }
  
    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }
  
    @JsonCreator
    public static ConnectorFormat fromValue(String text) {
      for (ConnectorFormat b : ConnectorFormat.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  