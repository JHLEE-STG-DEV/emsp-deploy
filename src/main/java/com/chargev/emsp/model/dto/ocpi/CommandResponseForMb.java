package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CommandResponseForMb {
    @Schema(required=true, description = "CommandResponseType (enum), eMSP에서 CPO로의 명령 요청에 대한 응답입니다.", example="ACCEPTED")
    private CommandResponseType result;
    @Schema(maxLength=36, required=true, description = "충전 세션이 생성/진행 중인 Location.id 입니다.", example="PI-200006")
    private String location_id;
    @Schema(maxLength=36, description = "세션을 시작할 이 위치의 EVSE에 대한 선택적 EVSE.uid입니다. connector_id가 설정된 경우 필수입니다.", example="PI-200006-2111")
    private String evse_uid;
    @Schema(maxLength=36, description = "세션을 시작할 EVSE의 커넥터에 대한 선택적 Connector.id 입니다. 이 필드는 EVSE에서 [capability: START_SESSION_CONNECTOR_REQUIRED]가 설정된 경우에 필요합니다.")
    private String connector_id;
}
