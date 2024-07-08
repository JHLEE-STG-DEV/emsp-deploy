package com.chargev.emsp.model.dto.cpo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ExcludeSettlement {
    @Schema(required=true, description = "정산 제외 요청 할 주문 번호")
    private String tradeNumber;
    @Schema(description = "정산 제외 사유")
    private String reason;
}
