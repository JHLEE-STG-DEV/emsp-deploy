package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EVSE {
    @Schema(maxLength=36, description = "EVSE 식별키")
    private String uid;
    @Schema(maxLength=48, description = "CPO의 플랫폼(및 하위 운영자 플랫폼) 내에서 EVSE를 식별할 수 있는 eMI3 표준의 식별자입니다.")
    private String evse_id;
    @Schema(description = "Status (enum), EVSE 상태 입니다.")
    private Status status;
    @Schema(description = "계획된 상태 업데이트를 나타냅니다.")
    private List<StatusSchedule> status_schedule;
    @Schema(description = "EVSE가 수행할 수 있는 기능 목록 입니다.")
    private List<Capability> capabilities;
    @Schema(description = "EVSE의 사용 가능한 커넥터 목록입니다.")
    private List<Connector> connectors;
    @Schema(maxLength=4, description = "현지에서 표시된 번호 체계의 충전 지점이 (차고 건물에) 위치하는 수준입니다.")
    private String floor_level;
    @Schema(description = "위도/경도")
    private GeoLocation coordinates;
    @Schema(maxLength=16, description = "시각적 식별을 위해 EVSE 외부에 인쇄된 번호/문자열입니다.")
    private String physical_reference;
    @Schema(description = "위치에서 EVSE에 도달하는 방법에 대한 보다 자세한 정보가 필요한 경우 다중 언어로 사람이 읽을 수 있는 지침입니다.")
    private List<DisplayText> directions;
    @Schema(description = "주차 장소에 적용되는 제한입니다.")
    private List<ParkingRestriction> parking_restrictions;
    @Schema(description = "사진 또는 로고 등 EVSE와 관련된 이미지 링크 목록입니다.")
    private List<Image> images;
    @Schema(description = "지불방법에 대한 String List입니다. (* OCPI 표준 아님)")
    private List<String> payment_methods;
    @Schema(description = "Access에 대한 String Enum입니다. (* OCPI 표준 아님)")
    private Access access;
    @Schema(description = "$date-time")
    private String last_updated;
}
