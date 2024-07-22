package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncReqBodyVerifyOcsp {
    @Schema(description = "Base64 encoded certificate")
    private String cert;
    @Schema(description = "Nonce 사용여부 (현재 사용하지 않음)", example="false")
    private Boolean nonce;
    @Schema(required=true, description = "ecKey CPO에서 올라오는 충전기 고유 식별자")
    private Long ecKey;
}
