package com.chargev.emsp.model.dto.oem;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspServicePackage {
    @Schema(description = "패키지 id")
    private String id;
    @Schema(description = "패키지 명")
    private String name;
    @Schema(description = "패키지 만료 시점")
    @JsonFormat(pattern = "YYYY-MM-DDTHH:MM:SS.sssZ", timezone = "UTC")
    private ZonedDateTime expiration_date;
}
