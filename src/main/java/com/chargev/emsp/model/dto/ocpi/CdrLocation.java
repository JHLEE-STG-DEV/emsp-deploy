package com.chargev.emsp.model.dto.ocpi;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CdrLocation {
    @Schema(maxLength=36, required = true, description = "CPO의 플랫폼(및 하위 운영자 플랫폼) 내에서 위치를 고유하게 식별합니다. 이 필드는 변경, 수정 또는 이름을 바꿀 수 없습니다.")
    private String id;
    @Schema(maxLength=255, description = "위치의 표시 이름입니다.")
    private String name;
    @Schema(maxLength=45, required = true, description = "거리/ 블록 이름 및 집번호, 상세주소 입니다.")
    private String address;
    @Schema(maxLength=45, required = true, description = "도시, 시/도/구 단위 주소 입니다.")
    private String city;
    @Schema(maxLength=10, description = "우편번호, 위치의 우편번호가 없는 경우 생략할 수 있습니다.")
    private String postal_code;
    @Schema(maxLength=20, description = "위치가 주 또는 지방과 관련된 경우에만 사용됩니다.")
    private String state;
    @Schema(maxLength=3, required = true, description = "이 위치의 국가에 대한 ISO-3166-1 alpha-3 입니다.", defaultValue="KOR", example="KOR")
    private String country;
    @Schema(required = true, description = "위도/경도")
    private GeoLocation coordinates;
    @Schema(maxLength=36, required = true, description = "CPO의 플랫폼(및 하위 운영자 플랫폼) 내에서 EVSE를 고유하게 식별합니다. (예: 데이터베이스 고유 ID 또는 실제 EVSE ID) 이 필드는 변경/수정/이름변경 등 할 수 없으며, 기술적 식별로 사람이 읽을 수 있는 식별로 사용되지 않도록 합니다.")
    private String evse_uid;
    @Schema(maxLength=48, required = true, description = "CPO의 플랫폼(및 하위 운영자 플랫폼) 내에서 EVSE를 식별할 수 있는 eMI3 표준의 식별자입니다.")
    private String evse_id;
    @Schema(maxLength=36, required = true, description = "EVSE 내 커넥터의 식별자입니다.")
    private String connector_id;
    @Schema(required = true, description = "설치된 커넥터의 표준입니다. 예약을 위해 생성된 CDR일 경우 임의의 값으로 설정될 수 있으며, 수신자는 이를 무시해야 합니다.")
    private ConnectorType connector_standard;
    @Schema(required = true, description = "설치된 커넥터의 형식(소켓/케이블)입니다. 예약을 위해 생성된 CDR일 경우 임의의 값으로 설정될 수 있으며, 수신자는 이를 무시해야 합니다.")
    private ConnectorFormat connector_format;
    @Schema(required = true, description = "예약을 위해 생성된 CDR일 경우 임의의 값으로 설정될 수 있으며, 수신자는 이를 무시해야 합니다.")
    private PowerType connector_power_type;
}
