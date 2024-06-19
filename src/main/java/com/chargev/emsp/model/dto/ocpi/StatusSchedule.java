package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StatusSchedule {
    @Schema(description = "$date-time 예정된 기간의 시작일시 입니다.")
    private String period_begin;
    @Schema(description = "$date-time 예정된 기간의 종료일시 입니다. 생략 가능 합니다.")
    private String period_end;
    @Schema(description = "Status (enum), EVSE 상태 입니다.")
    private Status status;
}