package com.chargev.emsp.model.dto.oem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MpayAssetData {
    @Schema(description = "credit card brand (KCP는 koreanlocal)", example="koreanlocal")
    private String brand;
    @Schema(description = "카드번호 앞 6자리", example="370000")
    private String firstDigits;
    @Schema(description = "카드번호 뒷 4자리", example="0002")
    private String lastDigits;
    @Schema(description = "유효기간(MM)", example="03")
    private int expiryMonth;
    @Schema(description = "유효기간(YYYY)", example="2030")
    private int expiryYear;
    @Schema(description = "카드 소지자 이름", example="Anna Meyer")
    private String holder;
    @Schema(description = "만료 여부 (유효기간 기준으로 계산한 값)", example="false")
    private boolean expired;
    @Schema(description = "검증되었는지 여부를 나타내는 플래그(예: zero-auth이 수행된 경우)", example="true")
    private boolean qualified;
    @Schema(description = "거래 승인 시 사용된 인증 유형(NoAuth / Auth / Auth3ds1 / Auth3ds2)", example="Auth")
    private String authenticationStatus;
    @Schema(description = "ISO-3166-1 alpha-2에 정의된 2자리 국가 코드", example="KR")
    private String issuingCountryCode;
    @Schema(description = "일부 PSP에 필요한 선택사항 (예를 들어 PayU India PreAuth 결제는 신용카드로만 가능)  credit / debit / prepaid / gift / virtual", example="credit")
    private String cardType;
}
