package com.chargev.emsp.service.preview;

import lombok.Data;

@Data
public class CheckContractIssueCondition {
    private ContractReqType reqType;
    private String contractId;
}
