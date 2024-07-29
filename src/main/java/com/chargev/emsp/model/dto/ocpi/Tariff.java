package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Tariff {
    @Schema(maxLength=2, minLength=2, description = "이 Tariff를 소유하는 CPO가 운영중인 국가의 ISO-3166 alpha-2 국가 코드입니다.", defaultValue="KR", example="KR")
    @JsonProperty("country_code")
    private String countryCode;
    @Schema(maxLength=3, minLength=3, description = "이 Tariff를 소유하는 CPO의 ID로 ISO-15118 표준을 따릅니다.")
    @JsonProperty("party_id")
    private String partyId;
    @Schema(maxLength=36, description = "CPO의 플랫폼(및 하위 운영자 플랫폼) 내에서 Tariff를 고유하게 식별합니다.")
    private String id;
    @Schema(description = "driver_groups (* OPCI 표준 아님)")
    @JsonProperty("driver_groups")
    private List<String> driverGroups;
    @Schema(maxLength=3, minLength=3, description = "이 Tariff에 대한 통화 코드이며, 국제 표준 ISO 4217 기준을 사용합니다.", defaultValue="KRW", example="KRW")
    private String currency;
    @Schema(description = "Tariff의 유형을 정의합니다. 이를 통해 주어진 [Charging Preferences]를 구별할 수 있으며, 생략시 이 Tariff의 모든 [Session]은 유효합니다.", defaultValue="REGULAR", example="REGULAR")
    private TariffType type;
    @Schema(description = "다국어 대체 Tariff 정보 텍스트 목록 입니다.")
    @JsonProperty("tariff_alt_text")
    private List<DisplayText> tariffAltText;
    @Schema(maxLength=255, description = "사람이 읽을 수 있는 형식으로 된 Tariff 정보에 대한 설명이 포함된 웹 페이지의 URL입니다.")
    @JsonProperty("tariff_alt_url")    
    private String tariffAltUrl;
    @Schema(description = "이 필드를 설정하면 이 Tariff의 충전 Session은 최소한 이 금액의 비용이 듭니다.")
    @JsonProperty("min_price")    
    private Price minPrice;
    @Schema(description = "이 필드를 설정하면 이 Tariff의 충전 Session은 이 금액을 초과하지 않습니다.")
    @JsonProperty("max_price")    
    private Price maxPrice;
    @Schema(description = "[Tariff] 요소 목록입니다.")
    private List<Element> elements;
    @Schema(description = "[Tariff]가 활성화되는 시간(UTC)으로, 위치의 [time_zone] 필드를 사용하여 현지 시간으로 변환할 수 있습니다.")
    @JsonProperty("start_date_time")
    private String startDateTime;
    @Schema(description = "[Tariff]가 더 이상 유효하지 않은 시간(UTC)으로, 위치의 [time_zone] 필드를 사용하여 현지 시간으로 변환할 수 있습니다.")
    @JsonProperty("end_date_time")
    private String endDateTime;
    @Schema(description = "이 [Tariff]로 공급되는 에너지에 대한 세부 정보입니다.")
    @JsonProperty("energy_mix")
    private EnergyMix energyMix;
    @Schema(description = "$date-time")
    @JsonProperty("last_updated")
    private String lastUpdated;
}
