package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OemPaymentInfo {
    @NotBlank
    @Schema(description = "자산 ID")
    @JsonProperty("asset_id")
    private String assetId;

    @NotBlank
    @Schema(description = "자산 ID 만료 일시")
    @JsonFormat(pattern = "YYYY-MM-DDTHH:MM:SS.sssZ", timezone = "UTC")
    @JsonProperty("expiration_date")
    private String expirationDate;
}
