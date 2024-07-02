package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StartSessionForMb {
    @Schema(required=true, description = "(OCPI 규격외) 세션을 시작할 eMSP 계정정보의 emsp_contract_id 입니다.", example="KRCEVAA1234567")
    private String emsp_contract_id;
    @Schema(maxLength=255, required=true, description = "CommandResult POST를 보내야 하는 URL입니다. 이 URL에는 StartSession 요청을 구분할 수 있는 고유 ID가 포함될 수 있습니다.", example="URL/den/emsp/1.0/commands/START_SESSION")
    private String response_url;
    @Schema(maxLength=36, required=true, description = "EVSE를 예약할 위치(이 요청이 전송되는 CPO에 속함)의 Location.id 입니다.", example="PI-200006")
    private String location_id;
    @Schema(maxLength=36, description = "세션을 시작할 이 위치의 EVSE에 대한 선택적 EVSE.uid입니다. connector_id가 설정된 경우 필수입니다.", example="PI-200006-2111")
    private String evse_uid;
    @Schema(maxLength=36, description = "세션을 시작할 EVSE의 커넥터에 대한 선택적 Connector.id 입니다. 이 필드는 EVSE에서 [capability: START_SESSION_CONNECTOR_REQUIRED]가 설정된 경우에 필요합니다.")
    private String connector_id;
    @Schema(required=true, description = "eMSP(Mercedes-Benz)에서 사용하는 식별 ID 입니다.")
    private String session_id;
}
