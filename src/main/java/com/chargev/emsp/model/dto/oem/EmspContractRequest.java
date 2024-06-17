package com.chargev.emsp.model.dto.oem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspContractRequest {
    @Schema(description = "계약 정보 중 차량 관련 정보")
    private OemVehicle vehicle;
    @Schema(description = "contains the enabled service for the mentioned vin. service describes specific package to be assigned to the customer.")
    private EmspServicePackage service_package;
    @Schema(description = "계약 정보 중 결제 관련 정보 (MPay에서 제공되는 정보)")
    private OemPaymentInfo payment_info;
}
