package com.chargev.emsp.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiResponseString {
    @Schema(description = "상태 코드(OCPI 기준)", example="1000")
    @JsonProperty("status_code")
    private OcpiResponseStatusCode statusCode;
    @Schema(description = "상태 메시지", example="Success")
    @JsonProperty("status_message")
    private String statusMessage;
    @Schema(description = "타임스탬프")
    @JsonFormat(pattern = "YYYY-MM-DDTHH:MM:SS.sssZ", timezone = "UTC")
    private String timestamp;
    @Schema(description = "데이터")
    private String data;
}
