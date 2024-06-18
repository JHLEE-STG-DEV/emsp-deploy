package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ParkingRestriction {
    EV_ONLY("EV_ONLY"),
      PLUGGED("PLUGGED"),
      DISABLED("DISABLED"),
      CUSTOMERS("CUSTOMERS"),
      MOTORCYCLES("MOTORCYCLES");
  
    private String value;
  
    ParkingRestriction(String value) {
      this.value = value;
    }
  
    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }
  
    @JsonCreator
    public static ParkingRestriction fromValue(String text) {
      for (ParkingRestriction b : ParkingRestriction.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  