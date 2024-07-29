package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspRfidCardRegistration {
    @Schema(description = "RFID 카드 번호")
    @JsonProperty("rfid_card_number")
    private String rfidCardNumber;
    
    @Schema(description = "RFID 카드 주문 ID")
    @JsonProperty("issued_id")
    private String issuedId;
}
