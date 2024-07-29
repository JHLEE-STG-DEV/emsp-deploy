package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Token {
    @Schema(maxLength=2, minLength=2, description = "이 Token을 소유하는 MSP가 운영중인 국가의 ISO-3166 alpha-2 국가 코드입니다.", defaultValue="KR", example="KR")
    @JsonProperty("country_code")
    private String countryCode;
    @Schema(maxLength=3, minLength=3, description = "이 Token을 소유하는 MSP의 ID로 ISO-15118 표준을 따릅니다.")
    @JsonProperty("party_id")
    private String partyId;
    @Schema(maxLength=36, description = "이 Token의 [TokenType]과 함께 식별할 수 있는 고유 ID 입니다.")
    private String uid;
    @Schema(description = "이 토큰의 타입입니다.", defaultValue="RFID", example="RFID")
    private TokenType type;
    @Schema(maxLength=36, description = "eMSP의 플랫폼(및 하위 운영자 플랫폼) 내에서 EV 드라이버 계약 토큰을 고유하게 식별합니다.")
    @JsonProperty("contract_id")
    private String contractId;
    @Schema(maxLength=64, description = "토큰(RFID 카드)에 인쇄된 육안으로 읽을 수 있는 번호/식별은 contract_id와 같을 수 있습니다.")
    @JsonProperty("visual_number")
    private String visualNumber;
    @Schema(maxLength=64, description = "발행 회사, 대부분의 경우 토큰(RFID 카드)에 인쇄된 회사 이름, 반드시 eMSP는 아닙니다.")
    private String issuer;
    @Schema(maxLength=36, description = "이 ID는 몇 개의 토큰을 그룹화합니다. 이를 통해 두 개 이상의 토큰이 하나로 작동하여 한 토큰으로 세션을 시작하고 다른 토큰으로 중지할 수 있으며, EV 드라이버에 카드와 리모트키를 제공할 때 편리합니다.")
    @JsonProperty("group_id")
    private String groupId;
    @Schema(description = "이 토큰의 유효 여부입니다.")
    private boolean valid;
    @Schema(description = "허용되는 WhitelistType(화이트리스트 유형)을 나타냅니다.")
    private WhitelistType whitelist;
    @Schema(maxLength=2, description = "언어코드로 ISO 639-1을 기준으로 합니다.", defaultValue="ko", example="ko")
    private String language;
    @Schema(description = "기본 [Charging Preference] 입니다.")
    @JsonProperty("default_profile_type")
    private ProfileType defaultProfileType;
    @Schema(description = "충전 지점이 충전 지점에서 자체 에너지 공급업체/계약 사용을 지원하는 경우 CPO가 사용할 에너지 공급업체를 알 수 있도록 에너지 공급업체/계약에 대한 정보가 필요합니다.")
    @JsonProperty("energy_contract")
    private EnergyContract energyContract;
    @Schema(description = "$date-time")
    @JsonProperty("last_updated")
    private String lastUpdated;
}
