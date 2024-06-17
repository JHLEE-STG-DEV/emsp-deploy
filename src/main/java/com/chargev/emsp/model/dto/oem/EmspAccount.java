package com.chargev.emsp.model.dto.oem;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspAccount {
    @Schema(description = "Customer Identifier in ChargeEV")
    private String emsp_account_key;
    @Schema(description = "Account status on ChargeEV")
    private String account_status;
    @Schema(description = "Contract information from Charge EV")
    private List<EmspContarct> contracts;
    @Schema(description = "회원명", example="홍길동")
    private String name;
    @Schema(description = "주소(OEM 주소체계와 같은 object사용하는지 확인 필요)")
    private OemAccountAddress address;
    @Schema(description = "이메일", example="test@email.com")
    private String email;
    @Schema(description = "핸드폰번호(대시기호 포함여부, 자리수 등 정의 필요함)", example="00000000000")
    private String mobile_number;
}
