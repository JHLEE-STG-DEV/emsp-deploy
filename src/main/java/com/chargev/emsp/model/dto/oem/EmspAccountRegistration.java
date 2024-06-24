package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonProperty;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspAccountRegistration {
    @Schema(description = "OEM Account", type="OemAccount")
    private OemAccount account;
    @Schema(description = "OEM-eMSP Contract 생성을 위한 정보", type="EmspContractRequest")
    @JsonProperty("contract_request")
    private EmspContractRequest contractRequest;
}
