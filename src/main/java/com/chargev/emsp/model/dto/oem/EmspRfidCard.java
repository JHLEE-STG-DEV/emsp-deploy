package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspRfidCard {
    @Schema(description = "RFID 카드 ID")
    @JsonProperty("card_id")
    private String cardId;
    @Schema(description = "RFID 카드 번호")
    @JsonProperty("card_number")
    private String cardNumber;
    @Schema(description = "RFID 카드 상태")
    private String status;
    @Schema(description = "RFID 카드 상태에 대한 이유(분실 등)")
    private String reason;
    @Schema(description = "registration_date")
    @JsonProperty("registration_date")
    @JsonFormat(pattern = "YYYY-MM-DDTHH:MM:SS.sssZ", timezone = "UTC")
    private String issuedAt;
}
