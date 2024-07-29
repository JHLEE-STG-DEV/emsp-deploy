package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Restrictions {
    @Schema(maxLength=5, description ="현지 시간의 시작 시간으로 시간대는 위치의 time_zone 필드에 정의되어 있습니다.", defaultValue="13:30", example="13:30")
    @JsonProperty("start_time")
    private String startTime;
    @Schema(maxLength=5, description ="현지 시간의 종료 시간으로 시간대는 위치의 time_zone 필드에 정의되어 있습니다.", defaultValue="19:45", example="19:45")
    @JsonProperty("end_time")
    private String endTime;
    @Schema(maxLength=10, description ="현지 시간의 시작 날짜로 시간대는 위치의 time_zone 필드에 정의되어 있습니다.", defaultValue="2015-12-24", example="2015-12-24")
    @JsonProperty("start_date")
    private String startDate;
    @Schema(maxLength=10, description ="현지 시간의 종료 날짜로 시간대는 위치의 time_zone 필드에 정의되어 있습니다.", defaultValue="2015-12-24", example="2015-12-24")
    @JsonProperty("end_date")
    private String endDate;
    @Schema(description ="최소 소비 에너지(hWh)입니다.")
    @JsonProperty("min_kwh")
    private Number minKwh;
    @Schema(description ="최대 소비 에너지(hWh)입니다.")
    @JsonProperty("max_kwh")
    private Number maxKwh;
    @Schema(description ="최소 전류(A, Ampere)입니다.")
    @JsonProperty("min_current")
    private Number minCurrent;
    @Schema(description ="최대 전류(A, Ampere)입니다.")
    @JsonProperty("max_current")
    private Number maxCurrent;
    @Schema(description ="최소 전력(kW)입니다.")
    @JsonProperty("min_power")
    private Number minPower;
    @Schema(description ="최대 전력(kW)입니다.")
    @JsonProperty("max_power")
    private int maxPower;
    @Schema(description ="충전 [Session]이 지속되어야하는 최소 기간(초)입니다.")
    @JsonProperty("min_duration")
    private Integer minDuration;
    @Schema(description ="충전 [Session]이 지속되어야하는 최대 기간(초)입니다.")
    @JsonProperty("max_duration")
    private int maxDuration;
    @Schema(description ="이 [TariffElement]가 활성화되는 요일입니다.")
    @JsonProperty("day_of_week")
    private List<DayOfWeek> dayOfWeek;
    @Schema(description ="이 필드가 정의되면 해당 [TariffElement]는 예약에 대한 Tariff를 설명합니다.")
    private ReservationRestrictionType reservation;
}
