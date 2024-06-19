package com.chargev.emsp.model.dto.ocpi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChargingPreferences {
    @Schema(description = "운전자가 선택한 스마트 충전 프로파일 유형입니다.", defaultValue="REGULAR", example="REGULAR")
    private ProfileType profile_type;
    @Schema(description = "$date-time 운전자가 지정한 출발 일시입니다. 이는 추정일 뿐이며 반드시 실제 출발 일시일 필요는 없습니다.")
    private String departure_time;
    @Schema(description = "kWh 단위의 에너지 요구량입니다. EV 운전자는 이 정도의 에너지를 충전하고 싶어합니다.")
    private Number energy_need;
    @Schema(description = "방전 허용, 운전자는 다른 기본 설정이 충족되는 한 필요할 때 EV를 방전할 수 있습니다.", defaultValue="false", example="false")
    private boolean discharge_allowed;
}