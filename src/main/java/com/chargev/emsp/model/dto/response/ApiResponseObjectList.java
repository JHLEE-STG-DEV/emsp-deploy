package com.chargev.emsp.model.dto.response;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiResponseObjectList<T> {
    @Schema(description = "상태 코드(OCPI 기준)", example="1000")
    private Integer status_code;
    @Schema(description = "상태 메시지", example="Success")
    private String status_message;
    @Schema(description = "타임스탬프")
    @JsonFormat(pattern = "YYYY-MM-DDTHH:MM:SS.sssZ", timezone = "UTC")
    private ZonedDateTime timestamp;
    @Schema(description = "데이터")
    private List<T> data;
}
