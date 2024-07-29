package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspRfidCardOrder {
    @Schema(description = "RFID 카드 주문 ID")
    @JsonProperty("order_id")
    private String orderId;

    @Schema(description = "주문 날짜")
    @JsonProperty("issued_date")
    @JsonFormat(pattern = "YYYY-MM-DDTHH:MM:SS.sssZ", timezone = "UTC")
    private String issuedAt;
}
