package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReserveNow {
    @Schema(maxLength=255, required=true, description = "CommandResult POST를 보내야 하는 URL입니다. 이 URL에는 ReserveNow 요청을 구분할 수 있는 고유 ID가 포함될 수 있습니다.")
    private URL response_url;
    @Schema(maxLength=255, required=true, description = "Charge Point가 예약하는 데 사용해야 하는 토큰 개체입니다. 이 요청에 제공된 토큰은 eMSP에 의해 승인됩니다.")
    private Token token;
    @Schema(required=true, description = "$date-time 이 예약이 종료되는 날짜/시간(UTC)입니다.")
    private String expiry_date;
    @Schema(maxLength=36, required=true, description = "이 예약에 대해 고유한 예약 ID입니다. 수신자(일반적으로 CPO) 지점에 해당 위치에 대한 이 reservationId와 일치하는 예약이 이미 있는 경우 예약이 대체됩니다.")
    private String reservation_id;
    @Schema(maxLength=36, required=true, description = "EVSE를 예약할 위치(이 요청이 전송되는 CPO에 속함)의 Location.id 입니다.")
    private String location_id;
    @Schema(maxLength=36, description = "특정 EVSE를 예약해야 하는 경우 이 위치의 EVSE에 대한 선택적 EVSE.uid입니다.")
    private String evse_uid;
    @Schema(maxLength=36, description = "eMSP가 제공한 승인에 대한 참조가 제공된 경우 이 참조는 관련 [Session] 또는 [CDR]에서 제공됩니다.")
    private String authorization_reference;
}
