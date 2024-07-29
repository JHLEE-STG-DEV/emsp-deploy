package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PublishTokenType {
    @Schema(maxLength=36, description = "이 Token의 [TokenType]과 함께 식별할 수 있는 고유 ID 입니다.")
    private String uid;
    @Schema(description = "이 토큰의 타입입니다.", defaultValue="RFID", example="RFID")
    private TokenType type;
    @Schema(maxLength=64, description = "토큰(RFID 카드)에 인쇄된 육안으로 읽을 수 있는 번호/식별은 contract_id와 같을 수 있습니다.")
    @JsonProperty("visual_number")
    private String visualNumber;
    @Schema(maxLength=64, description = "발행 회사, 대부분의 경우 토큰(RFID 카드)에 인쇄된 회사 이름, 반드시 eMSP는 아닙니다.")
    private String issuer;
    @Schema(maxLength=36, description = "이 ID는 몇 개의 토큰을 그룹화합니다. 이를 통해 두 개 이상의 토큰이 하나로 작동하여 한 토큰으로 세션을 시작하고 다른 토큰으로 중지할 수 있으며, EV 드라이버에 카드와 리모트키를 제공할 때 편리합니다.")
    @JsonProperty("group_id")
    private String groupId;
}
