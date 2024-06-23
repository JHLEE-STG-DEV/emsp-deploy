package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpipReqBodyRevokeContractCert {

    @Schema(description = "emaId(계약ID)", example = "KRCEVCA0123456")
    private String emaid;
    @Schema(description = "emaId(계약ID)", example = "KRCEVCA0123456")
    private CertificateHashData contractCertificateHashData;
    @Schema(description = "emaId(계약ID)", example = "KRCEVCA0123456")
    private Long ecKey;
}
