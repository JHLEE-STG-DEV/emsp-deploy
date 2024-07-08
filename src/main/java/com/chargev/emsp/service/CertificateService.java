package com.chargev.emsp.service;

import java.util.concurrent.CompletableFuture;

import com.chargev.emsp.model.dto.pnc.ContractInfo;
import com.chargev.emsp.model.dto.pnc.PncReqBodyAuthorize;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractInfo;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractSuspension;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueContract;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeContractCert;

public interface CertificateService {
    ServiceResult<String> suspension(PncReqBodyContractSuspension request, String trackId);
    CompletableFuture<ServiceResult<String>> issueCertificate(PncReqBodyIssueCert request, String trackId);
    CompletableFuture<ServiceResult<String>> revokeCertificate(PncReqBodyRevokeCert request, String trackId);
    CompletableFuture<ServiceResult<String>> issueContract(PncReqBodyIssueContract request, String trackId);
    CompletableFuture<ServiceResult<String>> revokeContract(PncReqBodyRevokeContractCert request, String trackId);
    ServiceResult<ContractInfo> getContractInfo(PncReqBodyContractInfo request, String trackId);
    ServiceResult<String> pncAuthorize(PncReqBodyAuthorize request, String trackId);
}
