package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Element {
    @Schema(description = "[Tariff]의 가격을 설명하는 가격 구성 요소 목록입니다.")
    private List<PriceComponent> price_components;
    @Schema(description = "[Tariff]의 적용 가능성을 설명하는 제한 사항입니다.")
    private Restrictions restrictions;
}