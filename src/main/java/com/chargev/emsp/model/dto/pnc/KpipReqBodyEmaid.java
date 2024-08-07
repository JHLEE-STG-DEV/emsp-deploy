package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpipReqBodyEmaid {
    @Schema(description = "emaid(계약ID)", example = "KRCEVCA0123456")
    private String emaid;
}
