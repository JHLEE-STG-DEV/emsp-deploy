package com.chargev.emsp.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpipApiResponse {
    @Schema(description = "OK or FAIL", example="OK")
    private String resultCode;
    @Schema(description = "Success / Error_() / Fail_Unknown", example="Success")
    private String resultMsg;
}
