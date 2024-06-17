package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnergySource {
    @Schema(required = true, description = "[EnergySource]의 종류입니다.")
    private EnergySourceCategory source;
    @Schema(required = true, description = "[source]의 백분율로 0-100으로 나타냅니다.")
    private Integer percentage;
}
