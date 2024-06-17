package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnvironmentImpact {
    @Schema(required = true, description = "이 값의 환경 영향 범주입니다.")
    private EnvironmentalImpactCategory category;
    @Schema(required = true, description = "[EnvironmentalImpactCategory]의 Kilo/Watt/Hour당 발생/배출되는 양(g, Gram)을 나타냅니다. 단위 : g/kWh")
    private Integer amount;
}
