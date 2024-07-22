package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OemVehicle {
    @NotBlank
    @Schema(description = "vin넘버(vehicle identifier)", example = "EX01234567890")
    private String vin;

    @NotBlank
    @Schema(description = "차량타입(BEV or PHEV)", example = "BEV")
    @JsonProperty("vehicle_type")
    private String vehicleType;

    @NotBlank
    @Schema(description = "모델명", example = "EQS 580")
    @JsonProperty("model_name")
    private String modelName;
}
