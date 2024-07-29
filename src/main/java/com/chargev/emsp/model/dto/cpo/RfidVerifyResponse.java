package com.chargev.emsp.model.dto.cpo;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RfidVerifyResponse {
    @Schema(required=true, description = "eMSP 거래번호")
    private String tradeNumber;

    @Schema(required=true, description = "eMSP 결제 금액")
    private BigDecimal paymentPrice;

    @Schema(required=true, description = "eMSP 단가")
    private BigDecimal emspPrice;

    @Schema(description = "커넥터타입")
    private String connectorType;
}
