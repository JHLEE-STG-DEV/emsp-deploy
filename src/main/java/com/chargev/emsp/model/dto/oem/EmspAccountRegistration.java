package com.chargev.emsp.model.dto.oem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspAccountRegistration {
    @Schema(description = "OEM Account")
    private OemAccount account;
    @Schema(description = "OEM-eMSP Contract 생성을 위한 정보")
    private EmspContractRequest contract_request;
}
