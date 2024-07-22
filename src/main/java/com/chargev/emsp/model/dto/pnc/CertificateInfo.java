package com.chargev.emsp.model.dto.pnc;

import java.util.Date;

import lombok.Data;

@Data
public class CertificateInfo {
    private String crlDistributionPoints;
    private String ocspUrl;
    private Date expirationDate;
    private Date issueDate;
    private String issuerDN;
    private String issuerCN;
    private String serialNumber;
    private String formattedSerialNumber;
    private String publicKey;
    private String signatureAlgorithm;
    private String subjectDN;
    private String subjectCN;
}
