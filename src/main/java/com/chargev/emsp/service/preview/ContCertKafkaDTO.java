package com.chargev.emsp.service.preview;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContCertKafkaDTO {
    private boolean success;
    private String emaId;
    private String oemId;
    private String pcid;
    private Long memberKey;
    private String memberGroupId;
    private Long memberGroupSeq;
    private String contCert;
    private String message;

}