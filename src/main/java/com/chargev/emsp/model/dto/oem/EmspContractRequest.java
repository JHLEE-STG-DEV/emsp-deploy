package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmspContractRequest {
    @NotNull
    @Valid
    @Schema(description = "계약 정보 중 차량 관련 정보", type="OemVehicle")
    private OemVehicle vehicle;

    @NotNull
    @Valid
    @Schema(description = "contains the enabled service for the mentioned vin. service describes specific package to be assigned to the customer.", type = "EmspServicePackage")
    @JsonProperty("service_package")
    private EmspServicePackage servicePackage;

    @NotNull
    @Valid
    @Schema(description = "계약 정보 중 결제 관련 정보 (MPay에서 제공되는 정보)", type="OemPaymentInfo")
    @JsonProperty("payment_info")
    private OemPaymentInfo paymentInfo;
}
