package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DayOfWeek {
    MONDAY("MONDAY"),
      TUESDAY("TUESDAY"),
      WEDNESDAY("WEDNESDAY"),
      THURSDAY("THURSDAY"),
      FRIDAY("FRIDAY"),
      SATURDAY("SATURDAY"),
      SUNDAY("SUNDAY");
  
    private String value;
  
    DayOfWeek(String value) {
      this.value = value;
    }
  
    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }
  
    @JsonCreator
    public static DayOfWeek fromValue(String text) {
      for (DayOfWeek b : DayOfWeek.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  