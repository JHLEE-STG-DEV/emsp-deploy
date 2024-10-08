package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Hours {
    @Schema(description = "주어진 예외를 제외하고 하루 24시간, 일주일 7일 운영여부를 나타냅니다.", defaultValue="true", example="true")
    private boolean twentyfourseven;
    @Schema(description = "평일 기반 정기 시간입니다. ([twentyfourseven] 'false'인 경우에 사용됩니다.)")
    @JsonProperty("regular_hours")
    private List<RegularHours> regularHours;
    @Schema(description = "지정된 날짜, 시간 범위 기반의 예외이며, 충전소 운영/접근 가능한 기간입니다")
    @JsonProperty("exceptional_openings")
    private ExceptionalPeriod exceptionalOpenings;
    @Schema(description = "지정된 날짜, 시간 범위 기반의 예외이며, 충전소 운영/접근 할 수 없는 기간입니다.")
    @JsonProperty("exceptional_closings")
    private ExceptionalPeriod exceptionalClosings;
}