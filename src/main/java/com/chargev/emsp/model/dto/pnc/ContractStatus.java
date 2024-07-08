package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ContractStatus {
    @Schema(description="")
    private int status;

    @Schema(description = "status가 정상일 경우, 인증서 정보")
    private ContractInfo contractInfo;
    
}
