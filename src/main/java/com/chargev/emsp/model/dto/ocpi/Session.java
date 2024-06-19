package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Session {
    @Schema(maxLength=2, required = true, description = "이 세션을 소유하는 CPO가 운영중인 국가의 ISO-3166 alpha-2 국가 코드입니다.", defaultValue="KR", example="KR")
    private String country_code;
    @Schema(maxLength=3, required = true, description = "이 세션을 소유하는 CPO의 ID로 ISO-15118 표준을 따릅니다.")
    private String party_id;
    @Schema(maxLength=36, required = true, description = "CPO에서 충전 세션을 식별하는 고유 ID입니다.")
    private String id;
    @Schema(required = true, description = "$date-time 충전 시작 일시입니다.")
    private String start_date_time;
    @Schema(description = "충전 완료/종료 일시입니다. (예: 충전이 완료되었지만 주차 비용도 지불해야 합니다.)")
    private String end_date_time;
    @Schema(required = true, description = "충전된 kWh를 나타냅니다.")
    private Number kwh;
    @Schema(required = true, description = "충전 [Session]을 시작하는데 사용되는 토큰으로, 고유 토큰을 식별하기 위한 관련 정보를 모두 포함하고 있습니다.")
    private CdrToken cdr_token;
    @Schema(required = true, description = "인증에 사용되는 메서드입니다.")
    private AuthMethod auth_method;
    @Schema(maxLength=36, description = "eMSP가 부여한 권한에 대한 참조입니다.")
    private String authorization_reference;
    @Schema(maxLength=36, required=true, description = "충전 세션이 생성/진행 중인 Location.id 입니다.")
    private String location_id;
    @Schema(maxLength=36, description = "충전 세션이 생성/진행 중인 EVSE.uid 입니다.")
    private String evse_uid;
    @Schema(maxLength=36, required=true, description = "충전 세션이 생성/진행 중인 Connector.id 입니다.")
    private String connector_id;
    @Schema(maxLength=255, description = "kWh 미터의 선택적 식별자 입니다.")
    private String meter_id;
    @Schema(maxLength=3, minLength=3, required = true, description = "이 세션에 사용된 통화 코드이며, 국제 표준 ISO 4217 기준을 사용합니다.", defaultValue="KRW", example="KRW")
    private String currency;
    @Schema(description = "총 비용을 계산하고 확인하는 데 사용할 수 있는 [ChargingPeriod] 목록 입니다.")
    private List<ChargingPeriod> charging_periods;
    @Schema(description = "이 [Session]에 지정된 [currency]로 된 총 비용입니다.")
    private Price total_cost;
    @Schema(description = "[Session]의 상태입니다.", defaultValue="PENDING", example="PENDING")
    private SessionStatus status;
    @Schema(description = "$date-time")
    private String last_updated;
}