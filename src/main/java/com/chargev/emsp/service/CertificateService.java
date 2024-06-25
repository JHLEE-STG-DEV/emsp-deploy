package com.chargev.emsp.service;

import java.util.concurrent.CompletableFuture;

import com.chargev.emsp.model.dto.pnc.KpipReqBodyEmaid;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractInfo;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractSuspension;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueContract;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeCert;

public interface CertificateService {
    ServiceResult<Void> suspension(PncReqBodyContractSuspension request);
    CompletableFuture<ServiceResult<Void>> issueCertificate(PncReqBodyIssueCert request);
    CompletableFuture<ServiceResult<Void>> revokeCertificate(PncReqBodyRevokeCert request);
    CompletableFuture<ServiceResult<Void>> issueContract(PncReqBodyIssueContract request);
    CompletableFuture<ServiceResult<Void>> revokeContract(KpipReqBodyEmaid request);
    ServiceResult<Void> getContractInfo(PncReqBodyContractInfo request);
    ServiceResult<Void> pncAuthorize(KpipReqBodyEmaid request);
}
