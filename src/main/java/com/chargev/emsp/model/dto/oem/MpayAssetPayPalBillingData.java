package com.chargev.emsp.model.dto.oem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MpayAssetPayPalBillingData {

    @Schema(description = "PSP brand", example="PAYPAL")
    private String brand;
    @Schema(description = "Billing Agreement Id")
    private String billingAgreementId;
    @Schema(description = "Customer Account Id", example="Max Mustermann")
    private String customerAccountId;
    @Schema(description = "Owner Merchant Id")
    private String ownerMerchantId;
}
