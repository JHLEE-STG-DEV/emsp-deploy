package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EmspContractStatusReason {
    INACTIVE("INACTIVE"),
    MISSING_PAYMENT("MISSING_PAYMENT"),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED"),
    IN_TERMINATE("IN_TERMINATE");

  private String value;

  EmspContractStatusReason(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static EmspContractStatusReason fromValue(String text) {
    for (EmspContractStatusReason b : EmspContractStatusReason.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
