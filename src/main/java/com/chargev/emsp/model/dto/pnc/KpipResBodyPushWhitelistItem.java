package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpipResBodyPushWhitelistItem {
    @Schema(description = "처리 유형 (Add/Delete)", example="Add")
    private String type;
    @Schema(description = "emaid 계약 ID", example="KRCEVCA5347803")
    private String emaid;
    @Schema(description = "처리 결과", example="Success")
    private String resultMsg;
}
