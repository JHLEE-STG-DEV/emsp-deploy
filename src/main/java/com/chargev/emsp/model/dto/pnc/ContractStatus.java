package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ContractStatus {
    @Schema(description="-1 : 존재를 확인할 수 없는, 혹은 발급 절차 진행 중인 인증서 \n0 : 정상\n 1 : 만료\n 2: 파기(revoked)")
    private int status;
    @Schema(description="에러일 경우, 메세지")
    private String message;

    @Schema(description = "status가 정상일 경우, 인증서 정보")
    private ContractInfo contractInfo;
    
}
