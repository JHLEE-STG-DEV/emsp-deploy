package com.chargev.emsp.service.cpo;

import com.chargev.emsp.model.dto.cpo.RfidVerify;
import com.chargev.emsp.model.dto.cpo.RfidVerifyResponse;
import com.chargev.emsp.service.ServiceResult;

public interface CpoService {
    // ServiceResult<EmspContract> createContract(String emspAccountKey, EmspContractRequest contractRequest);
    ServiceResult<RfidVerifyResponse> validateRfid(RfidVerify request, String trackId);
    ServiceResult<String> validateContract(String contractId, String trackId);
}
