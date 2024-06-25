package com.chargev.emsp.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncApiResponse {
    @Schema(description = "SUCCESS, FAIL", example="SUCCESS")
    private PncResponseResult result;
    @Schema(description = "Exception Type에 정의 된 Type (FAIL 일 경우 String Error Message가 아닌 Response Server에서 FAIL 별 처리가 필요한 경우)", example="202")
    private String code;
    @Schema(description = "시스템에 정의된 에러 메세지", example="Request received, processing initiated.")
    private String message;
}
