package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncReqBodyIssueCert {

    @Schema(description = "CSR")
    private String certificateSigningRequest;
    @Schema(description = "충전기 고유 식별자")
    private String ecKey;
    @Schema(description = "신규(NEW) / 갱신(UPDATE)", example = "NEW")
    private String type;
}
