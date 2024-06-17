package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnergyMix {
    @Schema(required = true, description = "재생 소스에서 100%인 경우 True 입니다. 재생 소스가 CO2와 핵 폐기물인 경우 0입니다.")
    private boolean is_green_energy;
    @Schema(required = true, description = "이 위치의 [Tariff]에 대한 [EnergySource] Key-value pairs (enum + percentage) 입니다.")
    private List<EnergySource> energy_sources;
    @Schema(required = true, description = "이 위치의 [Tariff]에 대한 [핵 폐기물 및 CO2 배출] Key-value pairs (enum + percentage) 입니다.")
    private List<EnvironmentImpact> environ_impact;
    @Schema(maxLength=64, description = "에너지 공급업체의 이름, 이 위치 또는 [Tariff]에 대한 에너지를 제공합니다.")
    private String supplier_name;
    @Schema(maxLength=64, description = "이 위치에서 사용되는 에너지 공급업체 제품/[Tariff] 계획의 이름입니다.")
    private String energy_product_name;
}
