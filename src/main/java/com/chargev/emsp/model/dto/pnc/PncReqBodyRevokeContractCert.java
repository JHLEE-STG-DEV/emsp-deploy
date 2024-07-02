package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncReqBodyRevokeContractCert {
    @Schema(description = "emaId(계약ID)", example = "KRCEVCA0123456")
    private String emaId;
}
