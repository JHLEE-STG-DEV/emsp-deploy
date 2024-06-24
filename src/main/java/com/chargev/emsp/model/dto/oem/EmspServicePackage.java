package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspServicePackage {
    @Schema(description = "패키지 id")
    private String id;
    @Schema(description = "패키지 명")
    private String name;
    @Schema(description = "패키지 만료 시점", type = "string", format = "date-time")
    @JsonFormat(pattern = "YYYY-MM-DDTHH:MM:SS.sssZ", timezone = "UTC")
    @JsonProperty("expiration_date")
    private String expirationDate;
}
