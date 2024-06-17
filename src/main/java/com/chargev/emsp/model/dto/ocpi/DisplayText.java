package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DisplayText {
    @Schema(maxLength=2, description = "언어코드로 ISO 639-1을 기준으로 합니다.", defaultValue="ko", example="ko")
    private String language;
    @Schema(maxLength=512, description = "사용자에게 표시할 텍스트로 마크업, html 등 허용되지 않습니다.")
    private String text;
}
