package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class CdrStatistics {
    @Schema(maxLength = 3, minLength = 3, description = "CDR에 사용된 통화 코드이며, 국제 표준 ISO 4217 기준을 사용합니다.", defaultValue = "KRW", example = "KRW")
    @JsonProperty("currency")
    private String currency;

    @Schema(description = "CDR에 모든 비용의 합계입니다.")
    @JsonProperty("total_cost")
    private Price totalCost;

    @Schema(description = "CDR에 주차 및 예약의 고정 가격을 제외한 고정 비용의 합계입니다. 사용된 시간/에너지 등에 의존하지 않으며, 시작 [Tariff]와 같은 비용을 포함할 수 있습니다.")
    @JsonProperty("total_fixed_cost")
    private Price totalFixedCost;

    @Schema(description = "지정된 통화로 CDR에 충전된 총 에너지(kWh) 입니다.")
    @JsonProperty("total_energy")
    private Number totalEnergy;

    @Schema(description = "지정된 통화로 CDR에 충전된 총 에너지에 대한 비용의 합계입니다.")
    @JsonProperty("total_energy_cost")
    private Price totalEnergyCost;

    @Schema(description = "CDR에 속하는 총 시간(hours)입니다. 충전 및 충전하지 않은 시간 모두 포함합니다.")
    @JsonProperty("total_time")
    private Number totalTime;

    @Schema(description = "지정된 통화로 CDR에 속하는 총 시간(hours)과 관련된 비용의 합계입니다.")
    @JsonProperty("total_time_cost")
    private Price totalTimeCost;

    @Schema(description = "CDR에 속하는 총 시간(hours) 중 EV가 충전되지 않은 시간(hours)입니다. (EVSE와 EV 간에 에너지가 전송되지 않음)")
    @JsonProperty("total_parking_time")
    private int totalParkingTime;

    @Schema(description = "CDR에 주차와 관련된 모든 비용의 총 합계입니다. 지정된 통화로 고정 가격 구성 요소를 포함합니다.")
    @JsonProperty("total_parking_cost")
    private Price totalParkingCost;

    @Schema(description = "CDR에 예약과 관련된 모든 비용의 총 합계입니다. 지정된 통화로 고정 가격 구성 요소를 포함합니다.")
    @JsonProperty("total_reservation_cost")
    private Price totalReservationCost;
}