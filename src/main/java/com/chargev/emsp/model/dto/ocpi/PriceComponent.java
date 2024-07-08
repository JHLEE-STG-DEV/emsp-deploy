package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PriceComponent {
    @Schema(description = "TariffDimensionType (enum), 요금 단위 유형입니다.", defaultValue="ENERGY")
    private TariffDimensionType type;
    @Schema(description = "Price per unit (excl. VAT) for this tariff dimension.")
    private Number price;
    @Schema(description = "Applicable VAT percentage for this tariff dimension. If omitted, no VAT is applicable. Not providing a VAT is different from 0% VAT, which would be a value of 0.0 here.")
    private Number vat;
    @Schema(description = "Minimum amount to be billed.")
    private int step_size;
}
