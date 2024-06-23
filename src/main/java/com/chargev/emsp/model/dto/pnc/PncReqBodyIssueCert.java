package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncReqBodyIssueCert {

    @Schema(required=true, description = "CSR")
    private String certificateSigningRequest;
    @Schema(required=true, description = "충전기 고유 식별자")
    private Long ecKey;
    @Schema(required=true, description = "NEW(신규) / UPDATE(갱신)", defaultValue="NEW", example = "NEW")
    private String issueType;
    @Schema(required=true, description = "CSMS / EVSE", defaultValue="EVSE", example = "EVSE")
    private String certType;
    @Schema(required=true, description = "KEPCO / HUBJECT", defaultValue="KEPCO", example = "KEPCO")
    private String authorities;
}
