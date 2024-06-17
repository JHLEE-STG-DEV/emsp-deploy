package com.chargev.emsp.model.dto.oem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspRfidCardModifyRequest {
    @Schema(description = "RFID 카드 번호")
    private String card_number;
    @Schema(description = "RFID 카드 상태")
    private String status;
    @Schema(description = "RFID 카드 상태에 대한 이유(분실 등)")
    private String reason;
}
