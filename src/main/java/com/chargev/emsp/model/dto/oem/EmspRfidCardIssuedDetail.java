package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspRfidCardIssuedDetail {
    @Schema(description = "RFID 카드 신청 ID")
    @JsonProperty("issued_id")
    private String issuedId;

    @Schema(description = "RFID 카드 신청 상태")
    @JsonProperty("issued_status")
    private String issuedStatus;

    @Schema(description = "eMSP Contract ID")
    @JsonProperty("contarct_id")
    private String contarctId;
    
    @Schema(description = "RFID 카드 신청 정보")
    @JsonProperty("rfid_card_request")
    private EmspRfidCardRequest rfidCardReuest;

    @Schema(description = "주문 정보")
    private EmspRfidCardOrder order;
}
