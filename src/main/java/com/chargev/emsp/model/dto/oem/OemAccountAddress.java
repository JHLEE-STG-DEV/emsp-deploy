package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OemAccountAddress {
    @NotBlank
    @JsonProperty("zip_code")
    @Schema(description = "우편번호", example="12345")
    private String zipCode;

    @NotBlank
    @Schema(description = "도로명", example="예시로")
    private String street;

    @NotBlank
    @Schema(description = "건물번호", example="120")
    @JsonProperty("house_number")
    private String houseNumber;

    @NotBlank
    @Schema(description = "도시", example="서울시 중구")
    private String city;

    @NotBlank
    @Schema(description = "국가", example="대한민국")
    private String country;
}
