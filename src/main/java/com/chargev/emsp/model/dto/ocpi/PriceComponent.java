package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PriceComponent {
    @Schema(description = "TariffDimensionType (enum), 요금 단위 유형입니다.", defaultValue="ENERGY")
    private TariffDimensionType type;
    @Schema(description = "[Tariff]의 적용 가능성을 설명하는 제한 사항입니다.")
    private Restrictions restrictions;
}
