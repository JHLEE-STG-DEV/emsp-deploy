package com.chargev.emsp.model.dto.ocpi;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CpoReqBodyStartSession {
    @Schema(required=true, description = "BENZ/BMW")
    private String oemCode;

    @Schema(required=true, description = "eMSP 거래 번호")
    private String tradeNumber;

    @Schema(description = "옵션")
    private String connectorType;

    @Schema(required=true, description = "eMSP 결제 금액")
    private BigDecimal paymentPrice;

    @Schema(required=true, description = "eMSP 단가")
    private BigDecimal emspPrice;
}
