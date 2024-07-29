package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Price {
    @Schema(description = "부가가치세 제외, 가격/원가 입니다.")
    @JsonProperty("excl_vat")
    private Number exclVat;
    @Schema(description = "부가가치세 포함, 가격/원가 입니다.")
    @JsonProperty("incl_vat")
    private Number inclVat;
}
