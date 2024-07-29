package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnergyContract {
    @Schema(maxLength=64, description = "이 토큰에 대한 에너지 공급자의 이름입니다.")
    @JsonProperty("supplier_name")
    private String supplierName;
    @Schema(maxLength=64, description = "이 토큰의 소유자에게 속한 에너지 공급자의 계약 ID입니다.")
    @JsonProperty("contract_id")
    private String contractId;
}
