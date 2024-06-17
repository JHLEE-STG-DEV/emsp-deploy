package com.chargev.emsp.model.dto.oem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OemAccountAddress {
    @Schema(description = "우편번호", example="12345")
    private String zip_code;
    @Schema(description = "도로명", example="예시로")
    private String street;
    @Schema(description = "건물번호", example="120")
    private String house_number;
    @Schema(description = "도시", example="서울시 중구")
    private String city;
    @Schema(description = "국가", example="대한민국")
    private String country;
}
