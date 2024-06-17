package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncReqBodyContractSuspension {
    @Schema(description = "OEM 프로비저닝 상태(계약 삭제 사유) : 'Update' or 'Delete'", example = "Update")
    private String reqType;
    @Schema(description = "PCID", example = "KMKPNC123456789AB0")
    private String pcid;
}
