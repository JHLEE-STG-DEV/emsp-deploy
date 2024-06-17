package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Price {
    @Schema(required = true, description = "부가가치세 제외, 가격/원가 입니다.")
    private Number excl_vat;
    @Schema(description = "부가가치세 포함, 가격/원가 입니다.")
    private Number incl_vat;
}
