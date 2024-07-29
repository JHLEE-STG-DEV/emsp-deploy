package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Connector {
    @Schema(maxLength=36, description = "EVSE 내 커넥터 식별키")
    private String id;
    @Schema(description = "ConnectorType (enum), 충전기 소켓 또는 플러그 규격 입니다.")
    private ConnectorType standard;
    @Schema(description = "커넥터의 형식, ConnectorFormat (enum)")
    private ConnectorFormat format;
    @Schema(description = "PowerType (enum)")
    @JsonProperty("power_type")
    private PowerType powerType;
    @Schema(description = "커넥터의 최대 전압[V, Voltage]입니다.")
    @JsonProperty("max_voltage")
    private Integer maxVoltage;
    @Schema(description = "커넥터의 최대 전류량[A, Ampere]입니다.")
    @JsonProperty("max_amperage")
    private Integer maxAmperage;
    @Schema(description = "커넥터가 전달할 수 있는 최대 전력[W, Watt]입니다.")
    @JsonProperty("max_electric_power")
    private Integer maxElectricPower;
    @Schema(description = "현재 유효한 충전 요금제 식별자 입니다. 여러 요금제가 가능하지만 각 \"Tarify.type\" 중 하나만 동시에 활성화할 수 있습니다.")
    @JsonProperty("tariff_ids")
    private List<String> tariffIds;
    @Schema(description = "사업자 약관 URL로, w3.org 규격 뒤에 문자열(255)을 입력하는 URL입니다.")
    @JsonProperty("terms_and_conditions")
    private URI termsAndConditions;
    @Schema(description = "$date-time")
    @JsonProperty("last_updated")
    private String lastUpdated;
}
