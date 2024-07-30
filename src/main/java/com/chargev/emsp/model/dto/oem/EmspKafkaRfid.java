package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspKafkaRfid {
    @Schema(description = "oemCode", example = "BMW")
    private String oemCode;
    
    @Schema(description = "rfid", example = "0000000000000000")
    private String rfId;

    @Schema(description = "rfid status", example = "AVAILABLE")
    private String rfIdStatus;

    @Schema(description = "request date", example = "2024-01-01T00:00:00Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private String requestDate;
}
