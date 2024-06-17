package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BusinessDetails {
    @Schema(description = "사업자명")
    private String name;
    @Schema(description = "웹사이트 URL")
    private String website;
    @Schema(description = "로고이미지 URL")
    private String logo;
}
