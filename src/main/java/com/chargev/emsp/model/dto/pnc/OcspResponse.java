package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OcspResponse {
    @Schema(description="한전에서 status라는 이름으로 내려오는 값",  example="Good")
    private String status;
    @Schema(description="한전에서 ocspRes라는 이름으로 내려오는 값")
    private String ocspRes;
    
}
