package com.chargev.emsp.model.dto.response;

import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PncApiResponseObject {
    @Schema(description = "SUCCESS, FAIL", example="SUCCESS")
    private PncResponseResult result;

    @Schema(description = "Exception Type에 정의 된 Type (FAIL 일 경우 String Error Message가 아닌 Response Server에서 FAIL 별 처리가 필요한 경우)", example="200")
    private String code;

    @Schema(description = "시스템에 정의된 에러 메세지", example="")
    private String message;

    @Schema(description = "반환할 데이터가 있을 때에만 사용")
    private Optional<Object> data = Optional.empty();

    @Builder
    public PncApiResponseObject(PncResponseResult result, String code, String message, Optional<Object> data) {
        this.result = result;
        this.code = code;
        this.message = message;
        this.data = data != null ? data : Optional.empty();
    }
}
