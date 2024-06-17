package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpipReqBodyRevokeCert {
    @Schema(description = "인증서 CN", example="ChargEV0123")
    private String certCn;
}
