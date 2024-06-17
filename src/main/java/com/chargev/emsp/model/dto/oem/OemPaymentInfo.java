package com.chargev.emsp.model.dto.oem;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OemPaymentInfo {
    @Schema(description = "자산 ID")
    private String asset_id;
    @Schema(description = "자산 ID 만료 일시")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private ZonedDateTime expiration_date;
}
