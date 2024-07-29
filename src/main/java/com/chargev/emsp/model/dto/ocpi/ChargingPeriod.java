package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChargingPeriod {
    @Schema(description = "충전 기간의 타임 스탬프를 시작합니다.")
    @JsonProperty("start_date_time")
    private String startDateTime;

    @Schema(description = "이 [ChargingPeriod] 동안 관련 값 목록입니다.")
    @JsonProperty("dimensions")
    private List<CdrDimension> dimensions;

    @Schema(maxLength = 36, description = "이 [ChargingPeriod]와 관련된 [Tariff]의 고유 식별자입니다. 제공되지 않을 경우 [No Tariff]가 적용됩니다.")
    @JsonProperty("tariff_id")
    private String tariffId;
}