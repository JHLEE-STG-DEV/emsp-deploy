package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdditionalGeoLocation {
    @Schema(maxLength=10, description ="위도 (예: 50.770774)", example="50.770774")
    private String latitude;
    @Schema(maxLength=10, description ="경도 (예: -126.104965)", example="-126.104965")
    private String longitude;
    private String name;
}
