package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StartSession {
    @Schema(maxLength=255, description = "CommandResult POST를 보내야 하는 URL입니다. 이 URL에는 StartSession 요청을 구분할 수 있는 고유 ID가 포함될 수 있습니다.")
    @JsonProperty("response_url")
    private URI responseUrl;
    @Schema(maxLength=255, description = "Charge Point가 새 세션을 시작하는 데 사용해야 하는 토큰 개체입니다. 이 요청에 제공된 토큰은 eMSP에 의해 승인됩니다.")
    private Token token;
    @Schema(maxLength=36, description = "EVSE를 예약할 위치(이 요청이 전송되는 CPO에 속함)의 Location.id 입니다.")
    @JsonProperty("location_id")
    private String locationId;
    @Schema(maxLength=36, description = "세션을 시작할 이 위치의 EVSE에 대한 선택적 EVSE.uid입니다. connector_id가 설정된 경우 필수입니다.")
    @JsonProperty("evse_uid")
    private String evseUid;
    @Schema(maxLength=36, description = "세션을 시작할 EVSE의 커넥터에 대한 선택적 Connector.id 입니다. 이 필드는 EVSE에서 [capability: START_SESSION_CONNECTOR_REQUIRED]가 설정된 경우에 필요합니다.")
    @JsonProperty("connector_id")
    private String connectorId;
    @Schema(maxLength=36, description = "eMSP가 제공한 승인에 대한 참조가 제공된 경우 이 참조는 관련 [Session] 또는 [CDR]에서 제공됩니다.")
    @JsonProperty("authorization_reference")
    private String authorizationReference;
}
