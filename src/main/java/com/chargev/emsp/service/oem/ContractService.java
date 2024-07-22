package com.chargev.emsp.service.oem;
import java.util.List;

import com.chargev.emsp.model.dto.oem.EmspContract;
import com.chargev.emsp.model.dto.oem.EmspContractRequest;
import com.chargev.emsp.service.ServiceResult;

public interface ContractService {
    ServiceResult<EmspContract> createContract(String emspAccountKey, EmspContractRequest contractRequest);
    ServiceResult<List<EmspContract>> getContractsByAccountKey(String emspAccountKey);
    ServiceResult<EmspContract> getContract(String emspAccountKey, String contractId);
}
