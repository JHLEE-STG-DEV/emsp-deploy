package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CertificateHashData {
    @Schema(required=true, description="hashAlgorithm")
    private HashAlgorithm hashAlgorithm;
    @Schema(minLength=0, maxLength=128, required=true, description="Hashed value of the Issuer DN")
    private String issuerNameHash;
    @Schema(minLength=0, maxLength=128, required=true, description="Hashed value of the issuers public key")
    private String issuerKeyHash;
    @Schema(minLength=0, maxLength=40, required=true, description="The serial number of the certificate")
    private String serialNumber;
}
