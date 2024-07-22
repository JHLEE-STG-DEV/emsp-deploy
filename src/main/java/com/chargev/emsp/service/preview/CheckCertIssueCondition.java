package com.chargev.emsp.service.preview;

import lombok.Data;

@Data
public class CheckCertIssueCondition {
    private String certId;
    private CertReqType reqType;
}
