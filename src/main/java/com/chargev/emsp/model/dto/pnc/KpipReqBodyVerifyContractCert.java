package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpipReqBodyVerifyContractCert {
    @Schema(description = "Base64 Encoded DER format of Contract Certificate", example="MIICmDCC...SP2jaE5Y=")
    private String contCert;
}
