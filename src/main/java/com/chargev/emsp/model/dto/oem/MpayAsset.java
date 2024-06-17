package com.chargev.emsp.model.dto.oem;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MpayAsset {
    @Schema(description = "GPP ID of the asset")
    private String id;
    @Schema(description = "asset")
    private String objectType;
    @Schema(description = "자산 생성 시간의 Unix timestamp", example="1604567408")
    private long createdAt;
    @Schema(description = "자산 타입 creditcard / paypal", example="creditcard")
    private String type;
    @Schema(description = "자산 재사용 가능 여부", example="true")
    private boolean recurring;
    @Schema(description = "자산을 저장소에 저장하는 것에 대한 고객 동의 여부", example="true")
    private boolean consentGiven;
    @Schema(description = "유효한(사용 가능한) 자산인지 여부 (false가 유효함)", example="false")
    private boolean invalid;
    @Schema(description = "Network Transaction Reference. This field is only returned at the end of card tokenization with Javascript library. If an NTR is generated during tokenization GPP will add it to asset that is returned with the Javascript SDK's callback. This field won't appear in Asset responses in any other endpoint. This value can be used for creating transactions that are initiated by merchant. see CreateTransactionRequest for details.")
    private String networkTransactionReference;
    @Schema(description = "자산이 creditcard일 경우 자산 상세 정보")
    private MpayAssetData data;
    @Schema(description = "자산이 paypal 경우 자산 상세 정보")
    private MpayAssetPayPalBillingData paypalBillingData;
    @Schema(description = "Merchant Tags (array of strings)", example="[\"subscription\"]")
    private List<String> merchantTags; // 
}
