package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CommandResponse {
    @Schema(required=true, description = "CommandResponseType (enum), eMSP에서 CPO로의 명령 요청에 대한 응답입니다.")
    private CommandResponseType result;
    @Schema(required=true, description = "이 명령에 대한 시간 제한(초)입니다. 이 제한 시간 내에 결과가 수신되지 않으면 eMSP는 메시지가 전송되지 않을 수 있다고 가정할 수 있습니다.")
    private Integer timeout;
    @Schema(description = "사람이 읽을 수 있는 결과 설명(제공할 수 있는 경우), 여러 언어를 제공할 수 있습니다.")
    private DisplayText message;
}
