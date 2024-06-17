package com.chargev.emsp.model.dto.oem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OemVehicle {
    @Schema(description = "vin넘버(vehicle identifier)", example = "EX01234567890")
    private String vin;
    @Schema(description = "차량타입(BEV or PHEV)", example = "BEV")
    private String vehicle_type;
    @Schema(description = "모델명", example = "EQS 580")
    private String model_name;
}
