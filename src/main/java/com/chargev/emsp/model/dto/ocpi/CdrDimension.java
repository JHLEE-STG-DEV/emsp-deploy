package com.chargev.emsp.model.dto.ocpi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CdrDimension {
    @Schema(description = "CdrDimension의 유형입니다.")
    private CdrDimensionType type;
    @Schema(description = "[CdrDimensionType]에 따라 측정되는 치수의 값입니다.")
    private Number volume;
}
