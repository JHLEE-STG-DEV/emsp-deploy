package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Capability {
    CHARGING_PROFILE_CAPABLE("CHARGING_PROFILE_CAPABLE"),
      CHARGING_PREFERENCES_CAPABLE("CHARGING_PREFERENCES_CAPABLE"),
      CHIP_CARD_SUPPORT("CHIP_CARD_SUPPORT"),
      CONTACTLESS_CARD_SUPPORT("CONTACTLESS_CARD_SUPPORT"),
      CREDIT_CARD_PAYABLE("CREDIT_CARD_PAYABLE"),
      DEBIT_CARD_PAYABLE("DEBIT_CARD_PAYABLE"),
      PED_TERMINAL("PED_TERMINAL"),
      REMOTE_START_STOP_CAPABLE("REMOTE_START_STOP_CAPABLE"),
      RESERVABLE("RESERVABLE"),
      RFID_READER("RFID_READER"),
      START_SESSION_CONNECTOR_REQUIRED("START_SESSION_CONNECTOR_REQUIRED"),
      TOKEN_GROUP_CAPABLE("TOKEN_GROUP_CAPABLE"),
      UNLOCK_CAPABLE("UNLOCK_CAPABLE");
  
    private String value;
  
    Capability(String value) {
      this.value = value;
    }
  
    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }
  
    @JsonCreator
    public static Capability fromValue(String text) {
      for (Capability b : Capability.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  