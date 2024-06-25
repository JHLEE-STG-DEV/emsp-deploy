package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncReqBodyContractInfo {
    @Schema(required=true, description = "차량 VIN 번호", example = "KMKPNC123456789AB0")
    private String pcid;
    @Schema(required=true, description = "OEM ID", example = "BMW")
    private String oemId;
    @Schema(required=true, description = "Member Key")
    private Long memberKey;
}
