package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncReqBodyOCSPMessage {
    @Schema(description = "Base64 encoded OCSP Request message", example="hMEMwQTA/MD0wOzAJBgUrDgMCGgUABBSGDHGzCIh8wV/uckQRitez9WS6UwQUPJG9EM+JaohlWxH0Bz5czv57IA4CAmVW")
    private String ocspRequestData;
    @Schema(required=true, description = "ecKey CPO에서 올라오는 충전기 고유 식별자")
    private Long ecKey;
}
