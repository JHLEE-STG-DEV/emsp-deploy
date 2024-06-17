package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpipReqBodyIssueCert {
    @Schema(description = "인증서 타입 (EVSE/CSMS)", example="EVSE")
    private String certType;
    @Schema(description = "Base 64 encoded CSR of Issuing Leaf Certificate", example="MIICmDCC...SP2jaE5Y=")
    private String csr;
    @Schema(description = "발행(N)/재발행(R)", example="N")
    private String flag; 
}
