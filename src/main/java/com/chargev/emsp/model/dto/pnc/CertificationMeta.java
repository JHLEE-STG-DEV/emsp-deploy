package com.chargev.emsp.model.dto.pnc;

import lombok.Data;

@Data
public class CertificationMeta {
    private String certId;
    //pem기준이다.
    private String fullCert;
}
