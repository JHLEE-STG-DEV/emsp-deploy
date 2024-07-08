package com.chargev.emsp.model.dto.pnc;

import lombok.Data;

@Data
public class ContractMeta {
    private String contractId;
    private int emaBaseNumber;
    private String emaId;
    private String oemId;
    private Long memberKey;
    private String memberGroupId;
    private Long memberGroupSeq;
    private String pcid;
    
    //private String certificate;

    //private Date contractStartDt;
    //private Date contractEndDt;
    private String contractStartDtString;
    private String contractEndDtString;

    private String fullContCert;
    
    public ContractInfo buildContractInfo(){
        ContractInfo contractInfo = new ContractInfo();
        contractInfo.setEmaid(emaId);
        contractInfo.setContractStartDt(contractStartDtString);
        contractInfo.setContractEndDt(contractEndDtString);
        contractInfo.setPcid(pcid);
        contractInfo.setOemId(oemId);
        contractInfo.setMemberKey(memberKey);
        contractInfo.setMemberGroupId(memberGroupId);
        contractInfo.setMemberGroupSeq(memberGroupSeq);
        return contractInfo;        
    }
}
