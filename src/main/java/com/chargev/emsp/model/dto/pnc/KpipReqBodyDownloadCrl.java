package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpipReqBodyDownloadCrl {
    @Schema(description = "URL of CRL distribution point", example="http://120.216.20.21:9092/cpo/crl/cdp1dp26.crl")
    private String crlUrl;
}
