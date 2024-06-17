package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpipReqBodyVerifyOcsp {
    @Schema(description = "Base64 encoded DER format of verifying certificate", example="MIICmDCC...SP2jaE5Y=")
    private String cert;
    @Schema(description = "Nonce 사용여부 (현재 사용하지 않음)", example="false")
    private String nonce;
}
