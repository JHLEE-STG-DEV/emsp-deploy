package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpipReqBodyPncAuthorize {

    @Schema(description = "계약인증서(The X.509 certificated presented by EV and encoded in PEM format)")
    private String certificate;
    @Schema(description = "충전기 고유 식별자")
    private String ecKey;
}
