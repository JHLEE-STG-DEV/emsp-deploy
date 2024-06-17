package com.chargev.emsp.model.dto.oem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data

public class MpayError {

    @Schema(description = "오류 id", example="00000000-0000-0000-0000-0000-00000000000")
    private String id;
    @Schema(description = "오류 type")
    private String type;
    @Schema(description = "오류 detail")
    private MpayErrorDetail error;
    @Schema(description = "Source Path")
    private String sourcePath;
    @Schema(description = "human readable text for developers and operations")
    private String message;
}
