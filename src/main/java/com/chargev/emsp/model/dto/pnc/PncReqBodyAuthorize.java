package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncReqBodyAuthorize {
    @Schema(description = "emaId(계약ID)", example = "KRCEVCA0123456")
    private String emaId;
    @Schema(description = "계약인증서 HashData(OCSP용도)")
    private String contractCertificateHashData;
    @Schema(description = "CPO에서 올라오는 충전기 고유 식별자")
    private Long ecKey;
}
