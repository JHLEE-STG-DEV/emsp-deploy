package com.chargev.emsp.service;

import java.util.concurrent.CompletableFuture;

import com.chargev.emsp.model.dto.pnc.PncReqBodyAuthorize;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractInfo;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractSuspension;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueContract;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeContractCert;

public interface CertificateService {
    ServiceResult<Void> suspension(PncReqBodyContractSuspension request, String trackId);
    CompletableFuture<ServiceResult<Void>> issueCertificate(PncReqBodyIssueCert request, String trackId);
    CompletableFuture<ServiceResult<Void>> revokeCertificate(PncReqBodyRevokeCert request, String trackId);
    CompletableFuture<ServiceResult<Void>> issueContract(PncReqBodyIssueContract request, String trackId);
    CompletableFuture<ServiceResult<Void>> revokeContract(PncReqBodyRevokeContractCert request, String trackId);
    ServiceResult<Void> getContractInfo(PncReqBodyContractInfo request, String trackId);
    ServiceResult<Void> pncAuthorize(PncReqBodyAuthorize request, String trackId);
}
