package com.chargev.emsp.model.dto.oem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MpayErrorDetail {

    @Schema(description = "Enum [InternalServerError, APIError, AuthorizationError, PSPError, CardCaptureError, ...]", example="InternalServerError")
    private String category;
    @Schema(description = "Error Detail", example="UnsupportedMediaType")
    private String detail;

}
