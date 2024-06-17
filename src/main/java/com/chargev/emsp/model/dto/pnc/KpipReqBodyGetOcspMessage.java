package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpipReqBodyGetOcspMessage {
    @Schema(description = "Base64 encoded OCSP Request message", example="hMEMwQTA/MD0wOzAJBgUrDgMCGgUABBSGDHGzCIh8wV/uckQRitez9WS6UwQUPJG9EM+JaohlWxH0Bz5czv57IA4CAmVW")
    private String ocspReq;
    @Schema(description = "OCSP URL", example="http://ocsp.cpo.kepco.co.kr:7056/")
    private String ocspUrl;
    @Schema(description = "Nonce 사용여부 (현재 사용하지 않음)", example="false")
    private boolean nonce;
    @Schema(description = "file path of OCSP stapling (현재 사용하지 않음)", example="")
    private String path;
}
