package com.chargev.emsp.service.contract;

import com.chargev.emsp.model.dto.pnc.ContractMeta;
import com.chargev.emsp.service.ServiceResult;

public interface ContractManageService {
    public ServiceResult<ContractMeta> createEmptyContract();
    public ServiceResult<ContractMeta> setIssuedContract(String contractId, String emaId, String pcid, String oemId, Long memberKey, String memberGroupId, Long memberGroupSeq, String contCert);
    public ServiceResult<ContractMeta> setWhitelistedContract(String contractId);
    public ServiceResult<ContractMeta> undoWhitelistedContractg(String contractId);

    public ServiceResult<ContractMeta> getContractByEmaId(String emaId);
    public ServiceResult<ContractMeta> revokeContractById(String contractId);

    public ServiceResult<ContractMeta> findContractByMetaData(Long memberKey, String pcid, String oemId);
    // 이러면 위는 왜있는거임?
    public ServiceResult<ContractMeta> findContractByPcid(String pcid);

    public ServiceResult<String> getFullContCert(String contractId);

    // 자체적체크
    public ServiceResult<ContractMeta> checkAuth(String contractId);
}
