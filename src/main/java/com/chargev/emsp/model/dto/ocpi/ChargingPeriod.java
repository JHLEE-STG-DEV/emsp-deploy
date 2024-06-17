package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChargingPeriod {
    @Schema(required=true, description = "충전 기간의 타임 스탬프를 시작합니다.")
    private String start_date_time;
    @Schema(required=true, description = "이 [ChargingPeriod] 동안 관련 값 목록입니다.")
    private List<CdrDimension> dimensions;
    @Schema(maxLength=36, description = "이 [ChargingPeriod]와 관련된 [Tariff]의 고유 식별자입니다. 제공되지 않을 경우 [No Tariff]가 적용됩니다.")
    private String tariff_id;
}
