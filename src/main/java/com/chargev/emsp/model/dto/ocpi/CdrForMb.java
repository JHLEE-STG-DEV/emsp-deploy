package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CdrForMb {
    @Schema(maxLength = 2, minLength = 2, description = "시스템이 운영중인 국가의 ISO-3166 alpha-2 국가 코드입니다.", example = "KR")
    @JsonProperty("country_code")
    private String countryCode;
    
    @Schema(maxLength = 3, minLength = 3, description = "CDR을 소유하는 CPO의 ID로 ISO-15118 표준을 따릅니다.")
    @JsonProperty("party_id")
    private String partyId;
    
    @Schema(maxLength = 39,description = "CDR을 고유하게 식별하며, ID는 [country_code]/[party_id] 조합별로 고유해야 합니다. 크레딧 CDR이 원래 ID에 무언가를 추가할 수 있도록 일반적인 36자보다 길며, 일반(비크레딧) CDR은 최대 길이가 36인 ID만 가져야 합니다.")
    @JsonProperty("id")
    private String id;
    
    @Schema(description = "충전 시작 일시입니다. 충전이 보류중(PENDING)일 경우, Session이 생성된 시간으로 설정됩니다.")
    @JsonProperty("start_date_time")
    private String startDateTime;
    
    @Schema(description = "충전 완료/종료 일시입니다. (예: 충전이 완료되었지만 주차 비용도 지불해야 합니다.)")
    @JsonProperty("end_date_time")
    private String endDateTime;
    
    @Schema(description = "충전 [Session]을 시작하는데 사용되는 토큰으로, 고유 토큰을 식별하기 위한 관련 정보를 모두 포함하고 있습니다.")
    @JsonProperty("cdr_token")
    private CdrToken cdrToken;
    
    @Schema(description = "인증에 사용되는 방법으로, [Session] 중에, 여러 <mod_cdrs_authmethod_enum,AuthMethods>가 가능합니다.")
    @JsonProperty("auth_method")
    private AuthMethod authMethod;
    
    @Schema(maxLength = 36, description = "eMSP에서 제공한 승인(권한 부여)에 대한 참조입니다. eMSP가 [Real-time authorization], [StartSession] 또는 [ReserveNow]에서 authorization_reference 제공한 경우 이 필드에는 동일한 값이 포함되어야 합니다. eMSP가 이 세션과 관련된 다른 authorization_reference 값을 제공한 경우 마지막으로 제공된 값을 여기에 사용해야 합니다.")
    @JsonProperty("authorization_reference")
    private String authorizationReference;
    
    @Schema(description = "충전 관련 EVSE 및 커넥터만 포함하여 [Session]이 이루어진 위치입니다.")
    @JsonProperty("cdr_location")
    private CdrLocationForMb cdrLocation;
    
    @Schema(maxLength = 255, description = "충전 지점 내부의 미터 식별자 입니다.")
    @JsonProperty("meter_id")
    private String meterId;
    
    @Schema(maxLength = 3, minLength = 3,description = "CDR에 사용된 통화 코드이며, 국제 표준 ISO 4217 기준을 사용합니다.", defaultValue = "KRW", example = "KRW")
    @JsonProperty("currency")
    private String currency;
    
    @Schema(description = "[Tariff Elements] 관련 목록입니다. [Tariff]를 참조하십시오. 해당되는 경우 무료 [Tariff]도 이 목록에 있어야 하며, 정의된 무료 [Tariff]를 가리켜야 합니다.")
    @JsonProperty("tariffs")
    private List<Tariff> tariffs;
    
    @Schema(description = "CDR을 구성하는 [ChargingPeriod] 목록입니다. 이는 총 비용을 계산하고 확인하는 데 사용할 수 있습니다.")
    @JsonProperty("charging_periods")
    private List<ChargingPeriod> chargingPeriods;
    
    @Schema(description = "CDR에 속하는 서명된 데이터입니다.")
    @JsonProperty("signed_data")
    private SignedData signedData;
    
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
    
    @Schema(maxLength = 255, description = "비고는 CDR에 사람이 읽을 수 있는 추가 정보(예: 트랜잭션이 중지된 이유)를 제공하는 데 사용할 수 있습니다.")
    @JsonProperty("remark")
    private String remark;
    
    @Schema(maxLength = 39, description = "이 필드는 나중에 이 CDR에 대해 전송될 청구서(invoice)을 참조하는 데 사용할 수 있습니다. CDR을 지정된 청구서(invoice)에 더 쉽게 연결할 수 있습니다. 어쩌면 동일한 청구서(invoice)에 있는 CDR을 그룹화할 수도 있습니다.")
    @JsonProperty("invoice_reference_id")
    private String invoiceReferenceId;
    
    @Schema(description = "크레딧 CDR이며 true로 설정하면 [credit_reference_id]도 설정해야 합니다.")
    @JsonProperty("credit")
    private boolean credit;
    
    @Schema(maxLength = 39, description = "크레딧 CDR에 대해 설정해야 합니다. 여기에는 크레딧 CDR인 CDR의 ID가 포함되어야 합니다.")
    @JsonProperty("credit_reference_id")
    private String creditReferenceId;
    
    @Schema(description = "true로 설정하면 이 CDR은 EV 드라이버의 가정용 충전기를 사용하여 에너지 비용을 EV 드라이버에 금전적으로 보상해야 하는 충전 세션용입니다.")
    @JsonProperty("home_charging_compensation")
    private boolean homeChargingCompensation;
    
    @Schema(description = "$date-time")
    @JsonProperty("last_updated")
    private String lastUpdated;

    public Object getTotal_parking_cost() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTotal_parking_cost'");
    }
}
