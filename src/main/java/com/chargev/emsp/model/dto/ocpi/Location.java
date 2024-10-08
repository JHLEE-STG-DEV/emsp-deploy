package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Location {
    @Schema(maxLength=2, description = "시스템이 운영중인 국가의 ISO-3166 alpha-2 국가 코드입니다.", defaultValue="KR", example="KR")
    @JsonProperty("country_code")
    private String countryCode;
    @Schema(maxLength=3, description = "CPO/eMSP 등에 대한 ID로 ISO-15118 표준을 따릅니다.")
    @JsonProperty("party_id")
    private String partyId;
    @Schema(maxLength=36, description = "Location Id, station_id")
    private String id;
    @Schema(defaultValue="true", description = "정보 수신자가 위치를 게시할 수 있는지 여부로, 위치 게시가 불가능한 경우에는 조회 결과에 포함하지 않습니다.")
    private boolean publish;
    @Schema(description = "[publish] 'false'일 경우에만 사용하며, [PublishTokenType]이 일치하는 사용자에게만 이 위치를 표시할 수 있습니다.")
    @JsonProperty("publish_allowed_to")
    private List<PublishTokenType> publishAllowedTo;
    @Schema(maxLength=255)
    private String name;
    @Schema(maxLength=45, description = "거리/ 블록 이름 및 집번호, 상세주소 입니다.")
    private String address;
    @Schema(maxLength=45, description = "도시, 시/도/구 단위 주소 입니다.")
    private String city;
    @Schema(maxLength=10, description = "우편번호, 위치의 우편번호가 없는 경우 생략할 수 있습니다.")
    @JsonProperty("postal_code")
    private String postalCode;
    @Schema(maxLength=20, description = "위치가 주 또는 지방과 관련된 경우에만 사용됩니다.")
    private String state;
    @Schema(maxLength=3, description = "이 위치의 국가에 대한 ISO-3166-1 alpha-3 입니다.", defaultValue="KOR", example="KOR")
    private String country;
    @Schema(description = "GeoLocation class 위도/경도")
    private GeoLocation coordinates;
    @Schema(description = "사용자와 관련된 관련지점의 지리적 위치 목록입니다.")
    @JsonProperty("related_locations")
    private List<AdditionalGeoLocation> relatedLocations;
    @Schema(description = "충전소 위치의 일반적인 주차 유형 입니다. (String enum)")
    @JsonProperty("parking_type")
    private ParkingType parkingType;
    @Schema(description = "Hotline (* OCPI 표준 아님)")
    private Hotline hotline;
    @Schema(description = "충전소 내에 EVSE 목록입니다.")
    private List<EVSE> evses;
    @Schema(description = "위치에 도달하는 방법에 대한 사람이 읽을 수 있는 지침입니다.")
    private List<DisplayText> directions;
    @Schema(description = "운영자의 정보로 지정되지 않은 경우 [Credentials module]에 검색된 정보를 대신 사용해야 합니다.")
    private BusinessDetails operator;
    @Schema(description = "하위 운영자의 정보입니다.")
    private BusinessDetails suboperator;
    @Schema(description = "소유자의 정보입니다.")
    private BusinessDetails owner;
    @Schema(description = "이 충전 장소가 직접 속한 시설의 선택적 목록입니다. (String Enum)")
    private Facility facilities;
    @Schema(maxLength=255, description = "위치의 시간대를 나타내는 IANA 기준 TZ(Time Zone) 값 입니다.", defaultValue="Asia/Seoul", example="Asia/Seoul")
    @JsonProperty("time_zone")
    private String timeZone;
    @Schema(description = "충전을 위해 해당 위치의 EVSE가 운영/접근 가능한 시간입니다.")
    @JsonProperty("opening_times")
    private Hours openingTimes;
    @Schema(description = "해당 위치의 EVSE가 개방 시간 외에 여전히 충전 가능한지 여부를 나타냅니다.")
    @JsonProperty("charging_when_closed")
    private boolean chargingWhenClosed;
    @Schema(description = "사진 또는 로고 등 위치와 관련된 이미지 링크 목록입니다.")
    private List<Image> images;
    @Schema(description = "이 위치에서 공급되는 에너지에 대한 세부 정보입니다.")
    @JsonProperty("energy_mix")
    private EnergyMix energyMix;
    @Schema(description = "ConnectorTypeCount (* OCPI 표준 아님)")
    @JsonProperty("connector_type_counts")
    private List<ConnectorTypeCount> connectorTypeCounts;
    @Schema(description = "$date-time")
    @JsonProperty("last_updated")
    private String lastUpdated;
}
