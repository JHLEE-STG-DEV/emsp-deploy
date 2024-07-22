package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpipReqBodyIssueContractCert {
    @Schema(description = "요청타입 (New/Update/Add)", example="New")
    private String reqType;
    @Schema(description = "pcid", example="KMKPNC123456789AB0")
    private String pcid;
    @Schema(description = "emaid 계약 ID", example="KRCEVCA5347803")
    private String emaid;
    @Schema(description = "OEM ID", example="KMK")
    private String oemid;
    @Schema(description = "Month (up to 24, default 24month)", example="24")
    private String expPolicy;
}
