package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StopSession {
    @Schema(maxLength=255, required=true, description = "CommandResult POST를 보내야 하는 URL입니다. 이 URL에는 StopSession 요청을 구분할 수 있는 고유 ID가 포함될 수 있습니다.")
    private URL response_url;
    @Schema(maxLength=36, required=true, description = "중지하도록 요청된 세션의 Session.id 입니다.")
    private String session_id;
}
