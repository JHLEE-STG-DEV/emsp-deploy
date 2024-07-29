package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Hotline {
    @Schema(description ="핫라인 전화번호", example="15222573")
    @JsonProperty("phone_number")
    private String phoneNumber;
}
