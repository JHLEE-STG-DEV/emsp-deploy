package com.chargev.emsp.model.dto.cpo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RfidVerify {
    @Schema(required=true, description = "Charger API에서 생성한 Charge Key")
    private String chargeNumber;
    @Schema(required=true, description = "- 없이 숫자 16자리 전송")
    private String rfId;
    @Schema(required=true, description = "ecKey CPO에서 올라오는 충전기 고유 식별자")
    private String ecKey;
}
