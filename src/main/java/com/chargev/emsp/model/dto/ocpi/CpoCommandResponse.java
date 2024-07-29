package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CpoCommandResponse {
    @Schema(description="CPO에서 result라는 이름으로 내려오는 값",  example="SUCCESS")
    private String result;
    @Schema(description="CPO에서 code라는 이름으로 내려오는 값",  example="NOT_CHARGER_STATUS_C00")
    private String code;
    @Schema(description="CPO에서 message라는 이름으로 내려오는 값")
    private String message;
    @Schema(description="CPO에서 chargeNumber라는 이름으로 내려오는 값")
    private String chargeNumber;
}
