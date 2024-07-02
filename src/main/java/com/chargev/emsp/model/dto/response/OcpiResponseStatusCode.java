package com.chargev.emsp.model.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OcpiResponseStatusCode {
    // Success codes
    SUCCESS(1000, "일반 성공 코드"),
    CUSTOM_SUCCESS(1900, "사용자 지정 성공 상태 코드에 대해 예약된 범위"),

    // Client errors
    CLIENT_ERROR(2000, "일반 클라이언트 오류 코드"),
    INVALID_PARAMETER(2001, "유효하지 않거나 누락된 매개 변수"),
    INSUFFICIENT_INFORMATION(2002, "정보가 충분하지 않음"),
    UNKNOWN_LOCATION(2003, "알 수 없는 위치"),
    UNKNOWN_TOKEN(2004, "알 수 없는 토큰"),
    CUSTOM_CLIENT_ERROR(2900, "사용자 지정 클라이언트 오류 상태 코드에 대해 예약된 범위"),
    UNKNOWN_EMSP_CONTRACT_ID(2901, "유효한 eMSP 계약 ID가 없음"),

    // Server errors
    SERVER_ERROR(3000, "일반 서버 오류 코드"),
    API_UNAVAILABLE(3001, "클라이언트의 API를 사용할 수 없음"),
    UNSUPPORTED_VERSION(3002, "지원되지 않는 버전"),
    ENDPOINT_MISMATCH(3003, "EndPoint(or expected EndPoint)가 일치하지 않음"),
    CUSTOM_SERVER_ERROR(3900, "사용자 지정 서버 오류 상태 코드에 대해 예약된 범위"),
    ID_MISMATCH(3903, "EVSE UID, LOCATION ID 등 일부 PARAMETER를 서버에서 찾을 수 없음"),

    // Hub errors
    HUB_ERROR(4000, "일반 오류 코드"),
    UNKNOWN_RECIPIENT(4001, "알 수 없는 수신자"),
    REQUEST_TIMEOUT(4002, "전달된 요청에 대한 시간 제한"),
    CONNECTION_ISSUE(4003, "연결 문제"),
    CUSTOM_HUB_ERROR(4900, "사용자 지정 허브 오류 상태 코드에 대해 예약된 범위");
  
    private final int code;
    private final String description;

    OcpiResponseStatusCode(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    @JsonValue
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code + " - " + description;
    }

    @JsonCreator
    public static OcpiResponseStatusCode fromCode(int code) {
        for (OcpiResponseStatusCode statusCode : OcpiResponseStatusCode.values()) {
            if (statusCode.code == code) {
                return statusCode;
            }
        }
        throw new IllegalArgumentException("Unknown OCPI response status code: " + code);
    }
}
