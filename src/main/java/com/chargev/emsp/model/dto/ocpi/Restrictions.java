package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Restrictions {
    @Schema(maxLength=5, description ="현지 시간의 시작 시간으로 시간대는 위치의 time_zone 필드에 정의되어 있습니다.", defaultValue="13:30", example="13:30")
    private String start_time;
    @Schema(maxLength=5, description ="현지 시간의 종료 시간으로 시간대는 위치의 time_zone 필드에 정의되어 있습니다.", defaultValue="19:45", example="19:45")
    private String end_time;
    @Schema(maxLength=10, description ="현지 시간의 시작 날짜로 시간대는 위치의 time_zone 필드에 정의되어 있습니다.", defaultValue="2015-12-24", example="2015-12-24")
    private String start_date;
    @Schema(maxLength=10, description ="현지 시간의 종료 날짜로 시간대는 위치의 time_zone 필드에 정의되어 있습니다.", defaultValue="2015-12-24", example="2015-12-24")
    private String end_date;
    @Schema(description ="최소 소비 에너지(hWh)입니다.")
    private Number min_kwh;
    @Schema(description ="최대 소비 에너지(hWh)입니다.")
    private Number max_kwh;
    @Schema(description ="최소 전류(A, Ampere)입니다.")
    private Number min_current;
    @Schema(description ="최대 전류(A, Ampere)입니다.")
    private Number max_current;
    @Schema(description ="최소 전력(kW)입니다.")
    private Number min_power;
    @Schema(description ="최대 전력(kW)입니다.")
    private int max_power;
    @Schema(description ="충전 [Session]이 지속되어야하는 최소 기간(초)입니다.")
    private Integer min_duration;
    @Schema(description ="충전 [Session]이 지속되어야하는 최대 기간(초)입니다.")
    private int max_duration;
    @Schema(description ="이 [TariffElement]가 활성화되는 요일입니다.")
    private List<DayOfWeek> day_of_week;
    @Schema(description ="이 필드가 정의되면 해당 [TariffElement]는 예약에 대한 Tariff를 설명합니다.")
    private ReservationRestrictionType reservation;
}
