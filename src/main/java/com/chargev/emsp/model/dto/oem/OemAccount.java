package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonProperty;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OemAccount {
    @NotBlank
    @Schema(description = "Id of the customer in MB side (MEID)")
    @JsonProperty("ciam_id")
    private String ciamId;

    @NotBlank
    @Schema(description = "Name of the customer which includes the first name and last name", example="Gildong Hong")
    private String name;

    @NotBlank
    @Email
    @Schema(description = "고객 이메일", example="test@email.com")
    private String email;

    @NotBlank
    @Schema(description = "고객 핸드폰 번호", example="010-0000-0000")
    @JsonProperty("mobile_number")
    private String mobileNumber;

    @NotNull
    @Valid
    @Schema(description = "고객 주소", type="OemAccountAddress")
    private OemAccountAddress address;
}
