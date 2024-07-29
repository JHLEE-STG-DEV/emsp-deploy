package com.chargev.emsp.service.oem;
import java.util.List;

import com.chargev.emsp.model.dto.oem.EmspContract;
import com.chargev.emsp.model.dto.oem.EmspContractRequest;
import com.chargev.emsp.model.dto.oem.EmspDriverGroupInfo;
import com.chargev.emsp.service.ServiceResult;

public interface ContractService {
    ServiceResult<EmspContract> createContract(String emspAccountKey, EmspContractRequest contractRequest);
    ServiceResult<EmspContract> modifyContract(String emspAccountKey, String contractId, EmspContract contractRequest);
    ServiceResult<String> terminateContract(String emspAccountKey, String contractId);
    ServiceResult<List<EmspContract>> getContractsByAccountKey(String emspAccountKey);
    ServiceResult<EmspContract> getContract(String emspAccountKey, String contractId);
    ServiceResult<EmspDriverGroupInfo> getDriverGroup(String emspAccountKey, String contractId);
}
