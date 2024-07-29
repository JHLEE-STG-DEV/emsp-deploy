package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class CdrToken {
    @Schema(maxLength = 2, minLength = 2, description = "이 토큰을 '소유'하는 시스템이 운영중인 국가의 ISO-3166 alpha-2 국가 코드입니다.", example = "KR")
    @JsonProperty("country_code")
    private String countryCode;

    @Schema(maxLength = 3, minLength = 3, description = "이 토큰을 '소유'하는 eMSP의 ID로 ISO-15118 표준을 따릅니다.")
    @JsonProperty("party_id")
    private String partyId;

    @Schema(maxLength = 36, description = "이 토큰을 식별할 수 있는 고유 ID입니다.")
    @JsonProperty("uid")
    private String uid;

    @Schema(description = "이 토큰의 타입입니다.")
    @JsonProperty("TokenType")
    private String tokenType;

    @Schema(maxLength = 36, description = "eMSP의 플랫폼(및 하위 운영자 플랫폼) 내에서 EV 드라이버 계약 토큰을 고유하게 식별합니다.")
    @JsonProperty("contract_id")
    private String contractId;
}
