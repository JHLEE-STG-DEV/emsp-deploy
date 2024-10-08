package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ExceptionalPeriod {
    @Schema(description = "예외 시작 시각 ([0-1][0-9]|2[0-3]):[0-5][0-9]", defaultValue="09:00", example="09:00")
    @JsonProperty("period_begin")
    private String periodBegin;
    @Schema(description = "예외 끝 시각 ([0-1][0-9]|2[0-3]):[0-5][0-9]", defaultValue="23:15", example="23:15")
    @JsonProperty("period_end")
    private String periodEnd;
}
