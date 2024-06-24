package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspRfidCardRequest {
    @Schema(description = "수취인 명", example = "홍길동")
    private String name;
    @Schema(description = "수취인 번호", example = "010-0000-0000")
    @JsonProperty("mobile_number")
    private String mobileNumber;
    @Schema(description = "수취인 주소")
    private OemAccountAddress adress;
    @JsonProperty("payment_consented")
    @Schema(description = "payment_consented", example="true")
    private boolean paymentConsented;
}
