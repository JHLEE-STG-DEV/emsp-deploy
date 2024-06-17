package com.chargev.emsp.model.dto.oem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspRfidCardRequest {
    @Schema(description = "수취인 명", example = "홍길동")
    private String name;
    @Schema(description = "수취인 번호", example = "010-0000-0000")
    private String mobile_number;
    @Schema(description = "수취인 주소")
    private OemAccountAddress adress;
    @Schema(description = "payment_consented", example="true")
    private boolean payment_consented;
}
