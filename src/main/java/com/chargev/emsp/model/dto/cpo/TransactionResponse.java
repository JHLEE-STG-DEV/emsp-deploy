package com.chargev.emsp.model.dto.cpo;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransactionResponse {
    @Schema(description = "eMSP 거래번호", example="123456789")
    private String tradeNumber;

    @Schema(description = "eMSP 결제 금액", example="30000")
    private BigDecimal paymentPrice;

    @Schema(description = "eMSP 단가", example="15000")
    private BigDecimal emspPrice;
}
