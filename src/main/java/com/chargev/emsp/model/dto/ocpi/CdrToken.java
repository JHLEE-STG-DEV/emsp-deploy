package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CdrToken {
    @Schema(maxLength=2, minLength=2, required = true, description = "이 토큰을 '소유'하는 시스템이 운영중인 국가의 ISO-3166 alpha-2 국가 코드입니다.", example="KR")
    private String country_code;
    @Schema(maxLength=3, minLength=3, required = true, description = "이 토큰을 '소유'하는 eMSP의 ID로 ISO-15118 표준을 따릅니다.")
    private String party_id;
    @Schema(maxLength=36, required = true, description = "이 토큰을 식별할 수 있는 고유 ID입니다.")
    private String uid;
    @Schema(required = true, description = "이 토큰의 타입입니다.")
    private String TokenType;
    @Schema(maxLength=36, required = true, description = "eMSP의 플랫폼(및 하위 운영자 플랫폼) 내에서 EV 드라이버 계약 토큰을 고유하게 식별합니다.")
    private String contract_id;
}
