package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RegularHours {
    @Schema(maxLength=1, required = true, description = "요일 : 월요일(1)부터 일요일(7)까지를 나타냅니다.", defaultValue="1", example="1")
    private Integer weekday;
    @Schema(maxLength=5, required = true, description = "정규 기간의 시작 시각 ([0-1][0-9]|2[0-3]):[0-5][0-9]", defaultValue="09:00", example="09:00")
    private String period_begin;
    @Schema(maxLength=5, required = true, description = "정규 기간의 끝 시각 ([0-1][0-9]|2[0-3]):[0-5][0-9]", defaultValue="09:00", example="09:00")
    private String period_end;
}
