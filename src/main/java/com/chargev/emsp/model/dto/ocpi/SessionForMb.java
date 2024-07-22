package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SessionForMb {
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
    @Schema(required = true, description = "세선 토큰 대신 사용되는 emsp contract id (* OCPI 표준 아님)")
    private String emsp_contract_id;
    @Schema(required = true, description = "인증에 사용되는 메서드입니다.")
    private AuthMethod auth_method;
    @Schema(maxLength=36, required=true, description = "충전 세션이 생성/진행 중인 Location.id 입니다.")
    private String location_id;
    @Schema(maxLength=36, description = "충전 세션이 생성/진행 중인 EVSE.uid 입니다.")
    private String evse_uid;
    @Schema(maxLength=36, required=true, description = "충전 세션이 생성/진행 중인 Connector.id 입니다.")
    private String connector_id;
    @Schema(maxLength=3, minLength=3, required = true, description = "이 세션에 사용된 통화 코드이며, 국제 표준 ISO 4217 기준을 사용합니다.", defaultValue="KRW", example="KRW")
    private String currency;
    @Schema(description = "이 [Session]에 지정된 [currency]로 된 총 비용입니다.")
    private Price total_cost;
    @Schema(description = "[Session]의 상태입니다.", defaultValue="PENDING", example="PENDING")
    private SessionStatus status;
    @Schema(description = "$date-time")
    private String last_updated;
}
