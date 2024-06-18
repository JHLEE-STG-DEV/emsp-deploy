package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class URL {
    @Schema(maxLength=255, description = "w3.org 규격 뒤에 문자열(255)을 입력하는 URL입니다.")
    private String url;
}
