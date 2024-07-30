package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EmspRfidStatusReason {
    LOST("LOST"), // 분실 : 사용자의 요청에 의한 변경, 혹은 RFID 발급 요청 시 발급 사유로 인한 시스템 변경
    ISSUED("ISSUED"), // 발급요청 : RFID 발급 요청 시 발급 사유로 인한 시스템 변경
    CONTRACT_LOCKED("CONTRACT_LOCKED"), // 계약 잠김 : 상위 잠김으로 인한 시스템 변경
    ACCOUNT_LOCKED("ACCOUNT_LOCKED"), // 사용자 잠김 : 상위 잠김으로 인한 시스템 변경
    DAMAGED("DAMAGED"); // 훼손 : 사용자의 요청에 의한 변경, 혹은 RFID 발급 요청 시 발급 사유로 인한 시스템 변경

  private String value;

  EmspRfidStatusReason(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static EmspRfidStatusReason fromValue(String text) {
    for (EmspRfidStatusReason b : EmspRfidStatusReason.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
