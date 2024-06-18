package com.chargev.emsp.model.dto.oem;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspRfidCard {
    @Schema(description = "RFID 카드 ID")
    private String card_id;
    @Schema(description = "RFID 카드 번호")
    private String card_number;
    @Schema(description = "RFID 카드 상태")
    private String status;
    @Schema(description = "RFID 카드 상태에 대한 이유(분실 등)")
    private String reason;
    @Schema(description = "issued_at")
    @JsonFormat(pattern = "YYYY-MM-DDTHH:MM:SS.sssZ", timezone = "UTC")
    private ZonedDateTime issued_at;
}
