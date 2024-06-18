package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnergyContract {
    @Schema(maxLength=64, required=true, description = "이 토큰에 대한 에너지 공급자의 이름입니다.")
    private String supplier_name;
    @Schema(maxLength=64, description = "이 토큰의 소유자에게 속한 에너지 공급자의 계약 ID입니다.")
    private String contract_id;
}
