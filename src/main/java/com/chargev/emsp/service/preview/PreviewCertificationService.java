package com.chargev.emsp.service.preview;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.chargev.emsp.model.dto.pnc.Authorities;
import com.chargev.emsp.model.dto.pnc.CertStatus;
import com.chargev.emsp.model.dto.pnc.CertificateInfo;
import com.chargev.emsp.model.dto.pnc.CertificationMeta;
import com.chargev.emsp.model.dto.pnc.ContractInfo;
import com.chargev.emsp.model.dto.pnc.ContractMeta;
import com.chargev.emsp.model.dto.pnc.ContractStatus;
import com.chargev.emsp.model.dto.pnc.EvseCertificate;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyEmaid;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyGetOcspMessage;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyIssueContractCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyPushWhitelistItem;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyRevokeCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyVerifyContractCert;
import com.chargev.emsp.model.dto.pnc.OcspResponse;
import com.chargev.emsp.model.dto.pnc.PncContract;
import com.chargev.emsp.model.dto.pnc.PncReqBodyAuthorize;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractInfo;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractSuspension;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueContract;
import com.chargev.emsp.model.dto.pnc.PncReqBodyOCSPMessage;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeContractCert;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.cryptography.CertificateConversionService;
import com.chargev.emsp.service.http.KpipApiService;
import com.chargev.emsp.service.kafka.KafkaManageService;
import com.chargev.utils.DateTimeFormatHelper;
import com.chargev.utils.IdHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreviewCertificationService {

    @Lazy
    private final ObjectProvider<PreviewCertificationService> selfProvider;
    // private final PreviewCertificationService self;
    private final ObjectMapper objectMapper;
    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");
    private final CertificateManageService certService;
    private final ContractManageService contractManageService;
    private final KpipApiService kpipApiService;
    private final KafkaManageService kafkaService;
    private final CertificateConversionService certificateConversionService;

    private PreviewCertificationService getSelf() {
        return selfProvider.getIfAvailable();
    }
    // #region SUSPENSION

    // 이건 딱히 비동기로 해달라는 말이 없다.
    public ServiceResult<String> suspension(PncReqBodyContractSuspension request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        ServiceResult<List<PncContract>> cert = suspensionProcess(request, trackId);

        if (cert.getSuccess()) {
            for (PncContract contract : cert.get()) {
                kafkaService.sendSuspensionResult(contract, trackId);
            }
        }
        serviceResult.succeed("OK");
        return serviceResult;
    }

    // 여러개를 날릴 수 있다.
    private ServiceResult<List<PncContract>> suspensionProcess(PncReqBodyContractSuspension request, String trackId) {
        ServiceResult<List<PncContract>> serviceResult = new ServiceResult<>();
        List<PncContract> kafkaObjects = new ArrayList<>();
        String reqType;
        String pcid;
        try {
            // 1-1. 프로비저닝 변경 사유 ('Update' or 'Delete')
            reqType = (String) request.getReqType();
            // 1-2. 삭제할 계약 PCID
            pcid = (String) request.getPcid();

        } catch (Exception ex) {
            // 이건 실패를 안보내도 될 것 같다. 그쪽이 요청한게 아니니까.
            serviceResult.fail(400, "요청값 캐스팅 실패");
            String bodyJson = "";
            if (request != null) {
                try {
                    bodyJson = objectMapper.writeValueAsString(request);
                } catch (Exception e) {

                }
            }
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG:CERT_ISSUE_ERROR, TRACK ID: {}, MESSAGE: {}, Request: {}", trackId,
                        "요청값 캐스팅실패.", bodyJson);
            }
            return serviceResult;
        }

        // 2. 해당 pcid와 매칭되는 계약건 찾아서 TERMINATION 상태로 변경 처리
        ServiceResult<List<ContractMeta>> queryResult = contractManageService.findActiveContractsByPcid(pcid);
        if (queryResult.isFail()) {
            // DB에 없음. 그럼 날릴게 없으니 성공이라봐도 좋지않을까?
            // 변경된게 없으니 kafka를 보낼것도 없다.
            serviceResult.succeed(kafkaObjects);
            return serviceResult;
        }

        List<ContractMeta> terminateTargets = queryResult.get();
        for (ContractMeta contract : terminateTargets) {
            // revoke가 된것이므로, kpip에 보낼 이유는 없고, 내부에서만 처리한다.
            ServiceResult<ContractMeta> revokeResult = contractManageService
                    .revokeContractById(contract.getContractId());
            if (revokeResult.isFail()) {
                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG:CONTRACT_DB_ERROR, TRACK ID: {}, MESSAGE: {}, EMAID: {}", trackId,
                            "Contract가 Revoke됨을 DB에 저장 실패.", contract.getEmaId());
                }
            }
            // undo whitelist를 한다.

            ServiceResult<KpipWhitelistFactory> kpipWhitelistResult = kpipUndoWhitelist(contract.getEmaId(), trackId);
            if (kpipWhitelistResult.isFail()) {
                // db에 저장이 안되었는데 발급취소도 못했다.
                // 당장은 할수있는건 없다. 일단 로그에 쌓아만두자.
                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG:CONTRACT_REVOKE_ERROR, TRACK ID: {}, MESSAGE: {}, emaId: {}", trackId,
                            "Revoke요청이 온 인증서를 Whitelist에서 제거하지 못했습니다.",
                            contract.getEmaId());
                }
            }
            checkContractUndoWhitelistResult(contract.getContractId(), revokeResult.getSuccess());

            // 3. 변경된 계약 정보를 kafka로 전송
            PncContract kafkaObject = new PncContract();
            kafkaObject.setEmaId(contract.getEmaId());
            kafkaObject.setMemberKey(contract.getMemberKey());
            kafkaObject.setMemberGroupId(contract.getMemberGroupId());
            kafkaObject.setOemCode(contract.getOemId());
            // Vin이란? pcid라고 다른곳에 쓰여있었으니 일단 따라가본다.
            kafkaObject.setVin(contract.getPcid());
            // DER이라면 PEM으로 변경하는 CertificateConversionService 사용 필요
            // PEM으로 관리된다고 가정하자.
            kafkaObject.setCertificate(contract.getFullContCert());
            kafkaObject.setAuthorities(Authorities.KEPCO);
            kafkaObject.setStatus(CertStatus.DELETED);
            // 인증서 넣어서 만료날짜 추출하는 CertificateConversionService 사용? or DB에 저장해뒀다면 꺼내어서 사용?
            kafkaObject.setExpiredDate(contract.getContractStartDtString());
            // 인증서 넣어서 시작날짜 추출하는 CertificateConversionService 사용? or DB에 저장해뒀다면 꺼내어서 사용?
            kafkaObject.setRequestDate(contract.getContractEndDtString());

            kafkaObjects.add(kafkaObject);
        }
        serviceResult.succeed(kafkaObjects);
        return serviceResult;
    }
    // #endregion
    // #region CERT-ISSUE

    // 저쪽이 폴링할 수 있는 id를 리턴
    public ServiceResult<String> issueCertificate(PncReqBodyIssueCert request,
            String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        // Issue를 어떻게 진행할지 결정
        boolean requestNew = "NEW".equalsIgnoreCase(request.getIssueType());
        // 풀링용 아이디.
        ServiceResult<CheckCertIssueCondition> checkCondition = certService.checkCertCondition(request.getEcKey(),
                trackId,requestNew);

        if (checkCondition.isFail()) {
            serviceResult.fail(checkCondition.getErrorCode(),  checkCondition.getErrorMessage());
            certService.finishIssueCertIdentity(request.getEcKey(), trackId, false, checkCondition.getErrorCode(),
                    checkCondition.getErrorMessage());
            return serviceResult;
        } else if (checkCondition.get().getReqType().equals(ContractReqType.WORKING)) {
            // 누군가 작업중이면 그냥 조회하라고 id를 넘긴다.
            serviceResult.succeed(checkCondition.get().getCertId());
            return serviceResult;
        }
        String id = checkCondition.get().getCertId();

        // 백그라운드로 발급 넘김.
        CompletableFuture<ServiceResult<EvseCertificate>> certFuture = getSelf().issueCertificateAsync(id, request,
                trackId);
        // 발급의 종료에 kafka를 구독시킴
        // 비동기 작업 완료 후 처리
        certFuture.thenAccept(certResult -> {
            if (certResult.getSuccess()) {
                try{
                    kafkaService.sendCertResult(certResult.get(), trackId);
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                certService.finishIssueCertIdentity(request.getEcKey(), trackId, true, 200, certResult.getErrorMessage());
            }else{
                certService.finishIssueCertIdentity(request.getEcKey(), trackId, false, certResult.getErrorCode(), certResult.getErrorMessage());
            }
        }).exceptionally(ex -> {
            // 예외 처리
            apiLogger.error("TAG:CERT_UNKNOWN_ERROR, TRACK ID: {}, MESSAGE: {}", trackId,
                    ex.getMessage());
            return null;
        });

        // 위 과정이 오래걸리니 일단은 무조건 성공으로 id를 먼저 리턴.
        serviceResult.succeed(id);
        return serviceResult;
    }

    public ServiceResult<String> issueCertificatePrevious(PncReqBodyIssueCert request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        // 풀링용 아이디.
        String id = IdHelper.genLowerUUID32();

        // 백그라운드로 발급 넘김.
        CompletableFuture<ServiceResult<EvseCertificate>> certFuture = getSelf().issueCertificateAsync(id, request,
                trackId);
        // 발급의 종료에 kafka를 구독시킴
        // 비동기 작업 완료 후 처리
        certFuture.thenAccept(certResult -> {
            if (certResult.getSuccess()) {
                kafkaService.sendCertResult(certResult.get(), trackId);
            }
        }).exceptionally(ex -> {
            // 예외 처리
            apiLogger.error("TAG:CERT_UNKNOWN_ERROR, TRACK ID: {}, MESSAGE: {}", trackId,
                    ex.getMessage());
            return null;
        });

        // 위 과정이 오래걸리니 일단은 무조건 성공으로 id를 먼저 리턴.
        serviceResult.succeed(id);
        return serviceResult;
    }

    @Async
    public CompletableFuture<ServiceResult<EvseCertificate>> issueCertificateAsync(String certId,
            PncReqBodyIssueCert request,
            String trackId) {
        ServiceResult<EvseCertificate> serviceResult = new ServiceResult<>();

        // 조회할 수 있도록 DB에서 관리하는 식이 되어야한다.

        // 1. 요청바디의 값 확인
        String csr;
        Long ecKey;
        String issueType;
        String certType;
        String authorities;
        try {
            // 1-1. csr
            csr = request.getCertificateSigningRequest();
            // 1-2. 충전기 고유 식별자: KEPCO로 인증서 서명 요청 보낼 때에는 사용되지 않음. kafka쪽으로 돌려줄 때만 사용.
            ecKey = request.getEcKey();
            // 1-3. 요청 주체: "CSMS" / "EVSE"로 들어오고, KEPCO쪽도 같은 형식으로 받으니 그대로 보내주면 됨.
            issueType = request.getIssueType();
            // 1-4. 요청 종류: "NEW" / "UPDATE"로 들어오고, KEPCO쪽에는 "N" / "R" 로 변경해서 보내줘야 함.
            certType = request.getCertType();
            // 1-5. 발급 주체: "KEPCO" / "HUBJECT" KEPCO로 인증서 서명 요청 보낼 때에는 사용되지 않음. kafka쪽으로 돌려줄
            // 때만 사용.
            authorities = request.getAuthorities();

        } catch (Exception ex) {
            // 이런실패도 보내달라고하는것같다.

            EvseCertificate kafkaObject = new EvseCertificate();
            kafkaObject.setResult("FAIL");
            kafkaObject.setMessage("요청값이 유효하지 않습니다.");

            serviceResult.succeed(kafkaObject);

            // serviceResult.fail(400, "요청값이 유효하지 않습니다.");
            String bodyJson = "";
            if (request != null) {
                try {
                    bodyJson = objectMapper.writeValueAsString(request);
                } catch (Exception e) {

                }
            }
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG:CERT_ISSUE_ERROR, TRACK ID: {}, MESSAGE: {}, Request: {}", trackId,
                        "요청값 캐스팅실패.",
                        bodyJson);
            }
            return CompletableFuture.completedFuture(serviceResult);
        }

        EvseCertificate kafkaObject = new EvseCertificate();
        kafkaObject.setEcKey(ecKey);
        kafkaObject.setIssueType(issueType);
        kafkaObject.setCertType(certType);
        kafkaObject.setAuthorities(authorities);
        kafkaObject.setResult("FAIL");
        

        // 2. DB에 풀링 저장
        ServiceResult<String> initCertResult = certService.initNewCert(certId, csr);
        if (initCertResult.isFail()) {
            // serviceResult.fail(500, "요청을 생성하지 못했습니다.");
            kafkaObject.setMessage("요청을 생성하지 못했습니다.");
            serviceResult.succeed(kafkaObject);
            return CompletableFuture.completedFuture(serviceResult);
        }

        // 3. KPIP에게 인증서 요청
        // csr이 PEM 으로 들어오는 경우를 대비해서, 헤더/푸터와 모든 개행문자를 제거하고 보낸다.
        String derCsr = certificateConversionService.convertToBase64DER(csr);
        ServiceResult<KpipIssueCertFactory> kpipIssueResult = kpipIssue(certType, derCsr, issueType, trackId);
        if (kpipIssueResult.isFail()) {
            // serviceResult.fail(500, "kpip에서의 발급이 실패하였습니다.");
            kafkaObject.setMessage(kpipIssueResult.getErrorMessage());
            serviceResult.succeed(kafkaObject);
            return CompletableFuture.completedFuture(serviceResult);
        }

        KpipIssueCertFactory issueFactory = kpipIssueResult.get();

        // 4. 검증해서 kafka에 보낼 값을 세팅함.
        checkIssueResult(certId, kafkaObject, issueFactory, trackId);

        serviceResult.succeed(kafkaObject);
        return CompletableFuture.completedFuture(serviceResult);
    }

    private EvseCertificate checkIssueResult(String certId, EvseCertificate kafkaObject,
            KpipIssueCertFactory issueFactory,
            String trackId) {

        kafkaObject.setResult("FAIL");
        kafkaObject.setMessage(issueFactory.getResultMessage());
        if (issueFactory.isOK()) {
            // 결과값 처리
            String resCertType = issueFactory.getResCertType();
            if (resCertType == null) {
                kafkaObject.setMessage("kpip의 리턴값 해석에 실패하였습니다 : resCertType");
                return kafkaObject;
            }
            String leafCert = issueFactory.getLeafCert();
            if (leafCert == null) {
                kafkaObject.setMessage("kpip의 리턴값 해석에 실패하였습니다 : leafCert");
                return kafkaObject;
            }
            String subCa2 = issueFactory.getSubCa2Cert();
            if (subCa2 == null) {
                kafkaObject.setMessage("kpip의 리턴값 해석에 실패하였습니다 : subCa2");
                return kafkaObject;
            }

            // 받은 Base64 DER Encoded 인증서를 PEM으로 변환하여 넣기
            String pemLeafCert = certificateConversionService.convertToPEM(leafCert);
            if (pemLeafCert == null) {
                kafkaObject.setMessage("kpip의 leaf 인증서를 PEM으로 변환에 실패하였습니다");
                return kafkaObject;
            }
            // 받은 Base64 DER Encoded 인증서 두개로 PEM 인증서 체인 만들기
            String certChain = convertAndMergeCerts(leafCert, subCa2);
            if (certChain == null) {
                kafkaObject.setMessage("인증서 체인 작성에 실패하였습니다.");
                return kafkaObject;
            }
            kafkaObject.setCertificateChain(certChain);
            kafkaObject.setCertificate(pemLeafCert);

            try {
                ZonedDateTime zonedExpiredDate = ZonedDateTime
                        .ofInstant(certificateConversionService.getExpiredDate(leafCert).toInstant(), ZoneId.of("UTC"));
                String expiredDateString = DateTimeFormatHelper.formatToSimpleStyle(zonedExpiredDate);
                kafkaObject.setExpiredDate(expiredDateString);
            } catch (Exception ex) {
                kafkaObject.setMessage("인증서의 만료일자를 읽는데 실패하였습니다");
                return kafkaObject;
            }
            kafkaObject.setCertificateStatus(CertStatus.NORMAL);
            kafkaObject.setResult("SUCCESS");

            // DB에 저장

            ServiceResult<String> subResult = certService.saveSubCert(kafkaObject.getEcKey(), subCa2);
            if (subResult.getSuccess()) {

                ServiceResult<String> leafResult = certService.saveNewCert(certId, kafkaObject.getEcKey(),
                        subResult.get(), pemLeafCert,
                        !kafkaObject.getIssueType().equals("NEW"));
                if (leafResult.isFail()) {
                    kafkaObject.setResult("Fail");
                    kafkaObject.setMessage("발급에 성공하였으나 Leaf 인증서를 읽는데 실패하였습니다.");
                    if (apiLogger.isErrorEnabled()) {
                        apiLogger.error("TAG:CERT_DB_ERROR_LEAF, TRACK ID: {}, MESSAGE: {}, PEM: {}", trackId,
                                "LEAF를 DB에 저장 실패.", pemLeafCert);
                    }
                }
            } else {
                // db에 저장실패했다 어떻게 처리할 것인가?
                // 로깅만하고 넘긴다. 풀할때 없으면 그때 체크하면되지
                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG:CERT_DB_ERROR_SUBCA2, TRACK ID: {}, MESSAGE: {}, DER: {}", trackId,
                            "SubCa2를 DB에 저장 실패.", subCa2);
                }
            }
        }
        return kafkaObject;

    }

    private ServiceResult<KpipIssueCertFactory> kpipIssue(String certType, String csr, String issueType,
            String trackId) {
        ServiceResult<KpipIssueCertFactory> result = new ServiceResult<>();
        KpipReqBodyIssueCert kpipRequest = new KpipReqBodyIssueCert();
        kpipRequest.setCertType(certType);
        kpipRequest.setCsr(csr);
        kpipRequest.setFlag(issueType.equals("NEW") ? "N" : "R");

        ServiceResult<Map<String, Object>> kpipResult = kpipApiService.issueCert(kpipRequest, trackId);

        if (kpipResult.isFail()) {
            result.fail(500, "ChargeLink Error : " + kpipResult.getErrorMessage());
            return result;
        }

        KpipIssueCertFactory factory = new KpipIssueCertFactory(kpipResult.get());

        result.succeed(factory);
        return result;

    }

    private class KpipIssueCertFactory {
        private Map<String, Object> rawResult;

        private boolean systemStatus = true;

        @Getter
        private String resultMessage;
        private String resultCode;

        public KpipIssueCertFactory(Map<String, Object> rawResult) {
            this.rawResult = rawResult;
            try {
                resultCode = (String) rawResult.get("resultCode");
                resultMessage = (String) rawResult.get("resultMsg");
            } catch (Exception ex) {
                systemStatus = false;
                resultCode = "INTERNAL_FAIL";
                resultMessage = "resultCode 및 resultMsg 형식 불일치";
            }
        }

        public boolean isOK() {
            if (!systemStatus || resultCode == null) {
                return false;
            } else if (resultCode.equals("OK")) {
                return true;
            } else {
                return false;
            }
        }

        public String getResCertType() {
            try {
                return (String) rawResult.get("certType");
            } catch (Exception ex) {
                return null;
            }
        }

        public String getLeafCert() {
            try {
                return (String) rawResult.get("leafCert");
            } catch (Exception ex) {
                return null;
            }
        }

        public String getSubCa2Cert() {
            try {
                return (String) rawResult.get("subCa2");
            } catch (Exception ex) {
                return null;
            }
        }
    }

    // #endregion

    // #region CERT-REVOKE
    public ServiceResult<String> revokeCertificate(PncReqBodyRevokeCert request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        // 풀링용 아이디.
        // 조건을 보고 DB에서 조회해봐야한다.
        String id = null;
        String decodedCert = request.getCertificate();
        ServiceResult<CertificationMeta> certMetaResult = certService.findCertByPEM(request.getEcKey(),
                decodedCert);
        if (certMetaResult.getSuccess()) {
            id = certMetaResult.get().getCertId();
        }
        if (id == null) {
            serviceResult.fail(404, "일치하는 인증서가 존재하지 않습니다.");
        }
        // 백그라운드로 삭제 넘김.
        CompletableFuture<ServiceResult<EvseCertificate>> certFuture = getSelf().revokeCertificateAsync(id, request,
                trackId);
        // 발급의 종료에 kafka를 구독시킴
        // 비동기 작업 완료 후 처리
        certFuture.thenAccept(certResult -> {
            if (certResult.getSuccess()) {
                kafkaService.sendCertResult(certResult.get(), trackId);
            }
        }).exceptionally(ex -> {
            // 예외 처리
            apiLogger.error("TAG:CERT_UNKNOWN_ERROR, TRACK ID: {}, MESSAGE: {}", trackId,
                    ex.getMessage());
            return null;
        });

        // 위 과정이 오래걸리니 일단은 무조건 성공으로 id를 먼저 리턴.
        serviceResult.succeed(id);
        return serviceResult;
    }

    @Async
    public CompletableFuture<ServiceResult<EvseCertificate>> revokeCertificateAsync(String certId,
            PncReqBodyRevokeCert request,
            String trackId) {
        ServiceResult<EvseCertificate> serviceResult = new ServiceResult<>();

        // 조회할 수 있도록 DB에서 관리하는 식이 되어야한다.

        // 1. 요청바디의 값 확인
        // 1-1. CertificateHashData
        String pem;
        // 1-2. 충전기 고유 식별자
        Long ecKey;
        // 1-3. 요청 종류 (여기에는 항상 "DELETE"만 들어올 예정이고 KEPCO에 다시 보내줄 필요는 없음. kafka에 보내줄 때
        // 필요함)
        String issueType;
        try {
            // 1-1. CertificateHashData
            pem = request.getCertificate();
            // 1-2. 충전기 고유 식별자
            ecKey = request.getEcKey();
            // 1-3. 요청 종류 (여기에는 항상 "DELETE"만 들어올 예정이고 KEPCO에 다시 보내줄 필요는 없음. kafka에 보내줄 때
            // 필요함)
            issueType = request.getIssueType();

        } catch (Exception ex) {
            // 이런실패도 보내달라고하는것같다.

            EvseCertificate kafkaObject = new EvseCertificate();
            kafkaObject.setResult("FAIL");
            kafkaObject.setMessage("요청값이 유효하지 않습니다.");
            serviceResult.succeed(kafkaObject);
            // serviceResult.fail(400, "요청값이 유효하지 않습니다.");
            String bodyJson = "";
            if (request != null) {
                try {
                    bodyJson = objectMapper.writeValueAsString(request);
                } catch (Exception e) {

                }
            }
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG:CERT_ISSUE_ERROR, TRACK ID: {}, MESSAGE: {}, Request: {}", trackId,
                        "요청값 캐스팅실패.",
                        bodyJson);
            }
            return CompletableFuture.completedFuture(serviceResult);
        }

        String decodedCert = pem;
        EvseCertificate kafkaObject = new EvseCertificate();
        kafkaObject.setEcKey(ecKey);
        kafkaObject.setIssueType(issueType);
        kafkaObject.setCertType("EVSE");
        kafkaObject.setAuthorities("KEPCO");
        kafkaObject.setResult("FAIL");

        // 2. KPIP에게 revoke 요청
        // KEPCO에 보낼때에는 "인증서 CN"을 보내야 하는데, 받은 것은 "인증서 해시"와 "충전기 고유 식별자"임
        // 해시값, 혹은 충전기 고유 식별자 값 둘 중 하나 (혹은 둘 모두)로 매칭되는 인증서를 찾고 해당 인증서의 CN을 추출하는 작업 필요

        // 2-1 값 가져옴.
        ServiceResult<CertificationMeta> queryCertResult = certService.findCertByPEM(ecKey, decodedCert);
        if (queryCertResult.isFail()) {
            kafkaObject.setMessage("PEM값으로 인증서를 조회하는 데 실패하였습니다");
            serviceResult.succeed(kafkaObject);
            return CompletableFuture.completedFuture(serviceResult);
        }
        String certCn;
        try {

            CertificateInfo info = certificateConversionService.getCertInfoFromPEM(queryCertResult.get().getFullCert());

            certCn = info.getSubjectCN();
        } catch (Exception ex) {
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG:CONTRACT_PARSE_ERROR,  MESSAGE: {}, Request: {}",
                        "인증서 분해 실패.", queryCertResult.get().getFullCert());
                kafkaObject.setMessage("인증서 분해에 실패하였습니다");
            }
            ex.printStackTrace();
            serviceResult.succeed(kafkaObject);
            return CompletableFuture.completedFuture(serviceResult);
        }

        ServiceResult<KpipRevokeCertFactory> kpipIssueResult = kpipRevoke(certCn, trackId);
        if (kpipIssueResult.isFail()) {
            kafkaObject.setMessage(kpipIssueResult.getErrorMessage());
            serviceResult.succeed(kafkaObject);
            return CompletableFuture.completedFuture(serviceResult);
        }

        KpipRevokeCertFactory issueFactory = kpipIssueResult.get();
        // 4. 검증해서 kafka에 결과를 보냄.
        checkRevokeResult(certId, kafkaObject, issueFactory, trackId);

        serviceResult.succeed(kafkaObject);
        return CompletableFuture.completedFuture(serviceResult);
    }

    private EvseCertificate checkRevokeResult(String certId, EvseCertificate kafkaObject,
            KpipRevokeCertFactory revokeFactory,
            String trackId) {

        kafkaObject.setResult("FAIL");
        kafkaObject.setMessage("인증서 폐기 실패");
        if (revokeFactory.isOK()) {
            // 결과값 처리
            kafkaObject.setCertificateStatus(CertStatus.DELETED);
            kafkaObject.setResult("SUCCESS");
            kafkaObject.setMessage(revokeFactory.getResultMessage());

            // DB에 저장

            ServiceResult<String> revokeResult = certService.revokeCertByCertId(certId);
            if (revokeResult.isFail()) {
                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG:CERT_DB_ERROR_REVOKE, TRACK ID: {}, MESSAGE: {}, CERT ID: {}", trackId,
                            "REVOKE사항 DB에 저장 실패.", certId);
                }
            }

        }
        return kafkaObject;

    }

    private ServiceResult<KpipRevokeCertFactory> kpipRevoke(String certCn,
            String trackId) {
        ServiceResult<KpipRevokeCertFactory> result = new ServiceResult<>();
        KpipReqBodyRevokeCert kpipRequest = new KpipReqBodyRevokeCert();
        kpipRequest.setCertCn(certCn);

        ServiceResult<Map<String, Object>> kpipResult = kpipApiService.revokeCert(kpipRequest, trackId);

        if (kpipResult.isFail()) {
            result.fail(500, "ChargeLink Error : " + kpipResult.getErrorMessage());
            return result;
        }

        KpipRevokeCertFactory factory = new KpipRevokeCertFactory(kpipResult.get());

        result.succeed(factory);
        return result;

    }

    private class KpipRevokeCertFactory {
        private Map<String, Object> rawResult;

        private boolean systemStatus = true;

        @Getter
        private String resultMessage;
        private String resultCode;

        public KpipRevokeCertFactory(Map<String, Object> rawResult) {
            this.rawResult = rawResult;
            try {
                resultCode = (String) rawResult.get("resultCode");
                resultMessage = (String) rawResult.get("resultMsg");
            } catch (Exception ex) {
                systemStatus = false;
                resultCode = "INTERNAL_FAIL";
                resultMessage = "resultCode 및 resultMsg 형식 불일치";
            }
        }

        public boolean isOK() {
            if (!systemStatus || resultCode == null) {
                return false;
            } else if (resultCode.equals("OK")) {
                return true;
            } else {
                return false;
            }
        }

    }

    // #endregion
    // #region CERT-RESPONSE
    public ServiceResult<OcspResponse> ocspCertCheck(PncReqBodyOCSPMessage request, String trackId) {
        ServiceResult<OcspResponse> serviceResult = new ServiceResult<>();
        // 풀링용 아이디.
        Long ecKey = request.getEcKey();
        if(ecKey == null){
            serviceResult.fail(400, "EC KEY가 올바르지 않습니다.");
            return serviceResult;
        }
        // 그냥 그대로 전달하고 리턴값도 그대로 달라고 해서 그렇게 짠다.
        // 그렇게 말해놓고 막상 값은 안보내줘서 내부에서 파싱해야된다. 그니까 주워온다.
        ServiceResult<CertificationMeta> certResult =  certService.findCertByEcKey(ecKey);
        if(certResult.isFail()){
            serviceResult.fail(certResult.getErrorCode(), certResult.getErrorMessage());
            return serviceResult;
        }
        // cert에서 ocsp url을 파싱한다.
        String ocspUrl;
        try{
            String fullCert = certResult.get().getFullCert();
            CertificateInfo info = certificateConversionService.getCertInfoFromPEM(fullCert);

            ocspUrl = info.getOcspUrl();

        }catch(Exception ex){
            ex.printStackTrace();
            serviceResult.fail(400, "인증서의 ocspUrl값을 분석하는 데 실패하였습니다.");
            return serviceResult;
        }


        KpipReqBodyGetOcspMessage kpipRequest = new KpipReqBodyGetOcspMessage();
        kpipRequest.setOcspReq(request.getOcspRequestData());
        kpipRequest.setOcspUrl(ocspUrl);

        ServiceResult<Map<String, Object>> kpipResult = kpipApiService.getOcspResponseMessageFull(kpipRequest, trackId);

        if(kpipResult.isFail()){
            serviceResult.fail(500, kpipResult.getErrorMessage());
        }else{
            KpipOcspFactory factory = new KpipOcspFactory(kpipResult.get());
            OcspResponse ocspRes = new OcspResponse();
            ocspRes.setOcspRes(factory.getOcspRes());
            ocspRes.setStatus(factory.getStatus());
            serviceResult.succeed(ocspRes);
        }
        return serviceResult;
    }
    private class KpipOcspFactory{
        private Map<String, Object> rawResult;

        private boolean systemStatus = true;
        @Getter
        private String resultMessage;
        private String resultCode;

        @Getter
        private String ocspRes;
        @Getter
        private String status;

        public KpipOcspFactory(Map<String, Object> rawResult) {
            this.rawResult = rawResult;
            try {
                resultCode = (String) rawResult.get("resultCode");
                resultMessage = (String) rawResult.get("resultMsg");
                ocspRes = (String) rawResult.get("ocspRes");
                status = (String) rawResult.get("status");

            } catch (Exception ex) {
                systemStatus = false;
                resultCode = "INTERNAL_FAIL";
                resultMessage = "resultCode 및 resultMsg 형식 불일치";
            }
        }
    }

    // #endregion
    // #region CONTRACT-ISSUE
    // 저쪽이 폴링할 수 있는 id를 리턴
    public ServiceResult<String> issueContract(PncReqBodyIssueContract request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        // Issue를 어떻게 진행할지 결정
        boolean requestNew = "NEW".equalsIgnoreCase(request.getIssueType());
        ServiceResult<CheckContractIssueCondition> checkCondition = contractManageService
                .checkCondition(request.getPcid(), request.getMemberKey(), trackId, requestNew);
        if (checkCondition.isFail()) {
            serviceResult.fail(checkCondition.getErrorCode(), checkCondition.getErrorMessage());
            contractManageService.finishIssueContractIdentity(request.getPcid(), request.getMemberKey(), trackId, false,
                    checkCondition.getErrorCode(), checkCondition.getErrorMessage());
            return serviceResult;
        } else if (checkCondition.get().getReqType().equals(ContractReqType.WORKING)) {
            // 누군가 작업중이면 그냥 조회하라고 id를 넘긴다.
            serviceResult.succeed(checkCondition.get().getContractId());
            return serviceResult;
        }

        String id = checkCondition.get().getContractId();
        ContractReqType reqType = checkCondition.get().getReqType();

        // 백그라운드로 발급 넘김.
        CompletableFuture<ServiceResult<ContCertKafkaDTO>> certFuture = getSelf().issueContractAsync(id, request,
                reqType, trackId);
        // 발급의 종료에 kafka를 구독시킴
        // 비동기 작업 완료 후 처리
        certFuture.thenAccept(certResult -> {
            if (certResult.getSuccess()) {
                ContCertKafkaDTO dto = certResult.get();
                try {
                    if (dto.isSuccess()) {

                        kafkaService.sendContCertSuccessKafka(dto.getEmaId(),
                                dto.getOemId(),
                                dto.getPcid(),
                                dto.getMemberKey(),
                                dto.getMemberGroupId(),
                                dto.getMemberGroupSeq(),
                                dto.getContCert(),
                                trackId,
                                "Normal");
                    } else {
                        kafkaService.sendContCertFailKafka(dto.getEmaId(),
                                dto.getOemId(),
                                dto.getPcid(),
                                dto.getMemberKey(),
                                dto.getMemberGroupId(),
                                dto.getMemberGroupSeq(),
                                trackId,
                                dto.getMessage());
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                contractManageService.finishIssueContractIdentity(request.getPcid(), request.getMemberKey(), trackId,
                        dto.isSuccess(), 200, dto.getMessage());

            } else {
                contractManageService.finishIssueContractIdentity(request.getPcid(), request.getMemberKey(), trackId,
                        false, certResult.getErrorCode(), certResult.getErrorMessage());
            }
        }).exceptionally(ex -> {
            // 예외 처리
            apiLogger.error("TAG:CONTRACT_UNKNOWN_ERROR, TRACK ID: {}, MESSAGE: {}", trackId,
                    ex.getMessage());
            return null;
        });

        // 위 과정이 오래걸리니 일단은 무조건 성공으로 id를 먼저 리턴.
        serviceResult.succeed(id);
        return serviceResult;
    }

    // 이걸 실패하면, 원본의 구독해제도 해줘야할 것이다. (실패면 죽은것과 다름없기때문)
    @Async
    public CompletableFuture<ServiceResult<ContCertKafkaDTO>> issueContractAsync(String contractId,
            PncReqBodyIssueContract request,
            ContractReqType reqType,
            String trackId) {
        ServiceResult<ContCertKafkaDTO> serviceResult = new ServiceResult<>();
        // 1. 요청바디의 값 확인 // 1-1. pcid
        String pcid;
        // 1-2. oemId
        String oemId;
        // 1-3. memberKey
        Long memberKey;
        // 1-4. memberGroupId
        String memberGroupId;
        // 1-5. memberGroupSeq
        Long memberGroupSeq;
        try {
            // 1-1. pcid
            pcid = request.getPcid();
            // 1-2. oemId
            oemId = request.getOemId();
            // 1-3. memberKey
            memberKey = request.getMemberKey();
            // 1-4. memberGroupId
            memberGroupId = request.getMemberGroupId();
            // 1-5. memberGroupSeq
            memberGroupSeq = request.getMemberGroupSeq();

        } catch (Exception ex) {
            ContCertKafkaDTO kafkaObject = new ContCertKafkaDTO();
            kafkaObject.setSuccess(false);
            kafkaObject.setMessage("요청값이 유효하지 않습니다.");
            serviceResult.succeed(kafkaObject);
            // serviceResult.fail(400, "요청값이 유효하지 않습니다.");
            String bodyJson = "";
            if (request != null) {
                try {
                    bodyJson = objectMapper.writeValueAsString(request);
                } catch (Exception e) {

                }
            }
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG:CONTRACT_ISSUE_ERROR, TRACK ID: {}, MESSAGE: {}, Request: {}", trackId,
                        "요청값 캐스팅실패.",
                        bodyJson);
            }
            return CompletableFuture.completedFuture(serviceResult);
        }

        ContCertKafkaDTO kafkaObject = new ContCertKafkaDTO();
        kafkaObject.setOemId(oemId);
        kafkaObject.setPcid(pcid);
        kafkaObject.setMemberKey(memberKey);
        kafkaObject.setMemberGroupId(memberGroupId);
        kafkaObject.setMemberGroupSeq(memberGroupSeq);
        kafkaObject.setSuccess(false);

        // 2. DB에 풀링은 저장되어있다. 그냥 가져와서 쓴다.

        ServiceResult<ContractMeta> initContractResult = contractManageService.findById(contractId);
        if (initContractResult.isFail()) {
            // serviceResult.fail(500, "요청을 생성하지 못했습니다.");
            kafkaObject.setMessage("요청을 생성하지 못했습니다.");
            kafkaObject.setSuccess(false);
            serviceResult.succeed(kafkaObject);
            return CompletableFuture.completedFuture(serviceResult);
        }
        String emaId = initContractResult.get().getEmaId();
        if (emaId == null) {
            // 신규
            emaId = "KR" + "CEV" + "CA" + String.format("%07d", initContractResult.get().getEmaBaseNumber());
        }
        kafkaObject.setEmaId(emaId);

        // 단순저장이고, 잘된다면 후에 저장될것이니 일단 체크하지않는다.
        contractManageService.setIssuedContractInput(contractId,
                kafkaObject.getEmaId(),
                kafkaObject.getPcid(),
                kafkaObject.getOemId(),
                kafkaObject.getMemberKey(),
                kafkaObject.getMemberGroupId(),
                kafkaObject.getMemberGroupSeq());
        // 3. KPIP에게 인증서 요청
        ServiceResult<KpipIssueContractFactory> kpipIssueResult = kpipIssueContract(pcid, emaId, oemId, reqType,
                trackId);
        if (kpipIssueResult.isFail()) {
            // serviceResult.fail(500, "kpip에서의 발급이 실패하였습니다.");
            kafkaObject.setMessage(kpipIssueResult.getErrorMessage());
            kafkaObject.setSuccess(false);
            serviceResult.succeed(kafkaObject);
            return CompletableFuture.completedFuture(serviceResult);
        }

        KpipIssueContractFactory issueFactory = kpipIssueResult.get();

        // 4. 발급된것을 DB에 저장한다.
        // 메세지같은게 안쓰여서 딱히 세팅할건없음. DB저장을 메인으로
        int savedResult = checkContractIssueResult(contractId, kafkaObject, issueFactory, trackId);
        boolean needRevoke = false;

        if (savedResult > 0) {
            // 발급을 성공했다. Whitelist시킨다.
            ServiceResult<KpipWhitelistFactory> kpipWhitelistResult = kpipWhitelist(emaId, trackId);
            // 2-2. 여기에서 실패 처리, 현재 논의된 방향은 whitelist 업데이트에 실패한 건들만 모아놨다가
            // 하루에 한 번 정도 (배치) 요청을 다시 보내는 방향으로 정리되었음.
            checkContractWhitelist(contractId, kpipWhitelistResult.getSuccess());
            kafkaObject.setSuccess(true);
        } else if (savedResult < 0) {
            // 인증서는 정상이더라도 우리 DB에서 실패했으므로 revoke를 요청시킨다.
            kafkaObject.setMessage("내부 관리중 오류가 발견되었습니다. 발급된 인증서를 Revoke합니다.");
            needRevoke = true;
        } else {
            // 그냥 발급자체가 실패다.
            kafkaObject.setMessage(issueFactory.getResultMessage());
        }

        if (needRevoke) {
            ServiceResult<KpipRevokeContractFactory> revokeResult = kpipRevokeContract(emaId, trackId);
            if (revokeResult.isFail()) {
                // db에 저장이 안되었는데 발급취소도 못했다.
                // 당장은 할수있는건 없다. 일단 로그에 쌓아만두자.
                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG:CONTRACT_ISSUE_ERROR, TRACK ID: {}, MESSAGE: {}, emaId: {}", trackId,
                            "DB 저장에 실패한 인증서를 파기하는데 실패하였습니다..",
                            emaId);
                }
            }
        }

        serviceResult.succeed(kafkaObject);
        return CompletableFuture.completedFuture(serviceResult);
    }

    // 발급자체가 성공해야한다.
    private int checkContractIssueResult(String contractId,
            ContCertKafkaDTO kafkaObject,
            KpipIssueContractFactory issueFactory,
            String trackId) {
        if (issueFactory.isOK()) {
            // 결과값 처리
            String contCert = issueFactory.getContCert();
            kafkaObject.setContCert(contCert);
            // DB에 저장
            ServiceResult<ContractMeta> issueResult = contractManageService.setIssuedContract(contractId,
                    kafkaObject.getEmaId(),
                    kafkaObject.getPcid(),
                    kafkaObject.getOemId(),
                    kafkaObject.getMemberKey(),
                    kafkaObject.getMemberGroupId(),
                    kafkaObject.getMemberGroupSeq(),
                    contCert);

            if (issueResult.isFail()) {
                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG:CONTRACT_DB_ERROR, TRACK ID: {}, MESSAGE: {}, EMAID: {}", trackId,
                            "Contract를 DB에 저장 실패.", kafkaObject.getEmaId());
                }
                return -1;
            }

            return 1;
        }

        // 이건 케이스가 3개다. 1. 발급이 아예안됨 2. 발급이 됐는데 저장못함. 3. 다성공
        return 0;

    }

    private boolean checkContractWhitelist(String contractId, boolean whitelisted) {
        // contract에 whitelist 되었음을 체크한다.
        if (whitelisted) {
            ServiceResult<ContractMeta> whitelistResult = contractManageService.setWhitelistedContract(contractId);
            if (!whitelistResult.getSuccess()) {
                // contract에 whitelist가 아니지만, 차후에 되어야함을 체크한다?
                // => 배치를 돌릴 때 살아있고 화이트리스트 아닌걸 추가하게 한다고 생각하면, 계속 리트라이를 하게될것이므로 에러를 신경쓰지 않아도 된다.

                return false;
            }
        }

        return true;

    }

    private ServiceResult<KpipWhitelistFactory> kpipWhitelist(String emaId, String trackId) {
        ServiceResult<KpipWhitelistFactory> result = new ServiceResult<>();
        KpipReqBodyPushWhitelistItem item = new KpipReqBodyPushWhitelistItem();
        item.setType("Add");
        item.setEmaid(emaId);
        List<KpipReqBodyPushWhitelistItem> reqList = new ArrayList<>();
        reqList.add(item);
        ServiceResult<Map<String, Object>> kpipResult = kpipApiService.pushWhitelist(reqList, trackId);
        if (kpipResult.isFail()) {
            result.fail(500, "ChargeLink Error : " + kpipResult.getErrorMessage());
            return result;
        }
        KpipWhitelistFactory factory = new KpipWhitelistFactory(kpipResult.get());
        result.succeed(factory);
        return result;
    }

    private ServiceResult<KpipIssueContractFactory> kpipIssueContract(String pcid, String emaId, String oemId,
            ContractReqType reqType,
            String trackId) {
        ServiceResult<KpipIssueContractFactory> result = new ServiceResult<>();
        KpipReqBodyIssueContractCert kpipRequest = new KpipReqBodyIssueContractCert();
        if (reqType == null || reqType.equals(ContractReqType.NEW) || reqType.equals(ContractReqType.ADD)) {
            kpipRequest.setReqType("New");
        } else if (reqType.equals(ContractReqType.UPDATE)) {
            kpipRequest.setReqType("Update");
        } else {
            kpipRequest.setReqType("New");
        }
        kpipRequest.setPcid(pcid);
        kpipRequest.setEmaid(emaId);
        kpipRequest.setOemid(oemId);
        kpipRequest.setExpPolicy("24");

        ServiceResult<Map<String, Object>> kpipResult = kpipApiService.issueContractCert(kpipRequest, trackId);

        if (kpipResult.isFail()) {
            result.fail(500, "ChargeLink Error : " + kpipResult.getErrorMessage());
            return result;
        }

        KpipIssueContractFactory factory = new KpipIssueContractFactory(kpipResult.get());

        result.succeed(factory);
        return result;

    }

    private ServiceResult<KpipIssueContractFactory> kpipIssueContract(String pcid, String emaId, String oemId,
            String trackId) {
        ServiceResult<KpipIssueContractFactory> result = new ServiceResult<>();
        KpipReqBodyIssueContractCert kpipRequest = new KpipReqBodyIssueContractCert();
        kpipRequest.setReqType("New");
        kpipRequest.setPcid(pcid);
        kpipRequest.setEmaid(emaId);
        kpipRequest.setOemid(oemId);
        kpipRequest.setExpPolicy("24");

        ServiceResult<Map<String, Object>> kpipResult = kpipApiService.issueContractCert(kpipRequest, trackId);

        if (kpipResult.isFail()) {
            result.fail(500, "ChargeLink Error : " + kpipResult.getErrorMessage());
            return result;
        }

        KpipIssueContractFactory factory = new KpipIssueContractFactory(kpipResult.get());

        result.succeed(factory);
        return result;

    }

    private class KpipWhitelistFactory {
        private Map<String, Object> rawResult;

        private boolean systemStatus = true;
        @Getter
        private String resultMessage;
        private String resultCode;

        public KpipWhitelistFactory(Map<String, Object> rawResult) {
            this.rawResult = rawResult;
            try {
                resultCode = (String) rawResult.get("resultCode");
                resultMessage = (String) rawResult.get("resultMsg");
            } catch (Exception ex) {
                systemStatus = false;
                resultCode = "INTERNAL_FAIL";
                resultMessage = "resultCode 및 resultMsg 형식 불일치";
            }
        }
    }

    private class KpipIssueContractFactory {
        private Map<String, Object> rawResult;

        private boolean systemStatus = true;

        @Getter
        private String resultMessage;
        private String resultCode;

        public KpipIssueContractFactory(Map<String, Object> rawResult) {
            this.rawResult = rawResult;
            try {
                resultCode = (String) rawResult.get("resultCode");
                resultMessage = (String) rawResult.get("resultMsg");
            } catch (Exception ex) {
                systemStatus = false;
                resultCode = "INTERNAL_FAIL";
                resultMessage = "resultCode 및 resultMsg 형식 불일치";
            }
        }

        public boolean isOK() {
            if (!systemStatus || resultCode == null) {
                return false;
            } else if (resultCode.equals("OK")) {
                return true;
            } else {
                return false;
            }
        }

        public String getContCert() {
            try {
                return (String) rawResult.get("contCert");
            } catch (Exception ex) {
                return null;
            }
        }
    }

    // #endregion

    // #region CONTRACT-REVOKE
    public ServiceResult<String> revokeContract(PncReqBodyRevokeContractCert request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        // 풀링용 아이디.
        // 조건을 보고 DB에서 조회해봐야한다.
        String id = null;
        ServiceResult<ContractMeta> certMetaResult = contractManageService.getContractByEmaId(request.getEmaId());
        if (certMetaResult.getSuccess()) {
            id = certMetaResult.get().getContractId();
        }
        if (id == null) {
            serviceResult.fail(404, "emaId가 일치하는 인증서가 존재하지 않습니다.");
            return serviceResult;
        }
        // 백그라운드로 삭제 넘김.
        CompletableFuture<ServiceResult<ContCertKafkaDTO>> certFuture = getSelf().revokeContractAsync(
                certMetaResult.get(),
                request, trackId);
        // 발급의 종료에 kafka를 구독시킴
        // 비동기 작업 완료 후 처리
        certFuture.thenAccept(certResult -> {
            if (certResult.getSuccess()) {
                ContCertKafkaDTO dto = certResult.get();
                if (dto.isSuccess()) {

                    kafkaService.sendContCertSuccessKafka(dto.getEmaId(),
                            dto.getOemId(),
                            dto.getPcid(),
                            dto.getMemberKey(),
                            dto.getMemberGroupId(),
                            dto.getMemberGroupSeq(),
                            dto.getContCert(),
                            trackId,
                            "DELETED");
                } else {
                    kafkaService.sendContCertFailKafka(dto.getEmaId(),
                            dto.getOemId(),
                            dto.getPcid(),
                            dto.getMemberKey(),
                            dto.getMemberGroupId(),
                            dto.getMemberGroupSeq(),
                            trackId,
                            "FAIL");
                }

            }
        }).exceptionally(ex -> {
            // 예외 처리
            apiLogger.error("TAG:CONTRACT_UNKNOWN_ERROR, TRACK ID: {}, MESSAGE: {}", trackId,
                    ex.getMessage());
            return null;
        });

        // 위 과정이 오래걸리니 일단은 무조건 성공으로 id를 먼저 리턴.
        serviceResult.succeed(id);
        return serviceResult;
    }

    // 재신청시 ContractIdentity에도 표시해야할까? => 어차피 쿼리해서 체크하니까 의미는 없지만, 그래도 지워주자.
    @Async
    public CompletableFuture<ServiceResult<ContCertKafkaDTO>> revokeContractAsync(ContractMeta contractMeta,
            PncReqBodyRevokeContractCert request,
            String trackId) {
        ServiceResult<ContCertKafkaDTO> serviceResult = new ServiceResult<>();
        // null이면 상위에서 끝났을것이므로 여기선 패스
        String emaId = request.getEmaId();

        ContCertKafkaDTO kafkaObject = new ContCertKafkaDTO();
        kafkaObject.setOemId(contractMeta.getOemId());
        kafkaObject.setPcid(contractMeta.getPcid());
        kafkaObject.setMemberKey(contractMeta.getMemberKey());
        kafkaObject.setMemberGroupId(contractMeta.getMemberGroupId());
        kafkaObject.setMemberGroupSeq(contractMeta.getMemberGroupSeq());
        kafkaObject.setEmaId(emaId);

        kafkaObject.setSuccess(false);

        ServiceResult<KpipRevokeContractFactory> kpipIssueResult = kpipRevokeContract(emaId, trackId);
        if (kpipIssueResult.isFail()) {
            // kafkaObject.setMessage("kpip에서의 revoke가 실패하였습니다");
            serviceResult.succeed(kafkaObject);
            return CompletableFuture.completedFuture(serviceResult);
        }

        KpipRevokeContractFactory revokeFactory = kpipIssueResult.get();
        kafkaObject.setSuccess(true);
        // 4. 발급된것을 DB에 저장한다.
        // 메세지같은게 안쓰여서 딱히 세팅할건없음. DB저장을 메인으로
        checkContractRevokeResult(contractMeta.getContractId(), kafkaObject, revokeFactory, trackId);
        // revoke에 실패했는데 화이트리스트에서 제거해도 되는가?
        // 모르겠으니 일단 제거한다.

        ServiceResult<KpipWhitelistFactory> kpipWhitelistResult = kpipUndoWhitelist(emaId, trackId);
        if (kpipWhitelistResult.isFail()) {
            // db에 저장이 안되었는데 발급취소도 못했다.
            // 당장은 할수있는건 없다. 일단 로그에 쌓아만두자.
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG:CONTRACT_REVOKE_ERROR, TRACK ID: {}, MESSAGE: {}, emaId: {}", trackId,
                        "Revoke요청이 온 인증서를 Whitelist에서 제거하지 못했습니다.",
                        emaId);
            }
        }
        checkContractUndoWhitelistResult(contractMeta.getContractId(), kpipWhitelistResult.getSuccess());

        serviceResult.succeed(kafkaObject);
        return CompletableFuture.completedFuture(serviceResult);
    }

    private boolean checkContractUndoWhitelistResult(String contractId, boolean undoWhitelisted) {
        // contract에 whitelist 되었음을 체크한다.
        if (undoWhitelisted) {
            ServiceResult<ContractMeta> whitelistResult = contractManageService.undoWhitelistedContract(contractId);
            if (!whitelistResult.getSuccess()) {
                // contract에 whitelist가 아니지만, 차후에 되어야함을 체크한다?
                // => 배치를 돌릴 때 살아있고 화이트리스트 아닌걸 추가하게 한다고 생각하면, 계속 리트라이를 하게될것이므로 에러를 신경쓰지 않아도 된다.

                return false;
            }
        }

        return true;

    }

    private boolean checkContractRevokeResult(String contractId,
            ContCertKafkaDTO kafkaObject,
            KpipRevokeContractFactory revokeFactory,
            String trackId) {
        if (revokeFactory.isOK()) {

            // DB에 저장
            ServiceResult<ContractMeta> revokeResult = contractManageService.revokeContractById(contractId);

            if (revokeResult.isFail()) {
                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG:CONTRACT_DB_ERROR, TRACK ID: {}, MESSAGE: {}, EMAID: {}", trackId,
                            "Contract가 Revoke됨을 DB에 저장 실패.", kafkaObject.getEmaId());
                }
                return false;
            }

            return true;
        }
        return false;

    }

    private ServiceResult<KpipWhitelistFactory> kpipUndoWhitelist(String emaId, String trackId) {
        ServiceResult<KpipWhitelistFactory> result = new ServiceResult<>();
        KpipReqBodyPushWhitelistItem item = new KpipReqBodyPushWhitelistItem();
        item.setType("Delete");
        item.setEmaid(emaId);
        List<KpipReqBodyPushWhitelistItem> reqList = new ArrayList<>();
        reqList.add(item);
        ServiceResult<Map<String, Object>> kpipResult = kpipApiService.pushWhitelist(reqList, trackId);
        if (kpipResult.isFail()) {
            result.fail(500, "ChargeLink Error : " + kpipResult.getErrorMessage());
            return result;
        }
        KpipWhitelistFactory factory = new KpipWhitelistFactory(kpipResult.get());
        result.succeed(factory);
        return result;
    }

    private ServiceResult<KpipRevokeContractFactory> kpipRevokeContract(String emaId, String trackId) {
        ServiceResult<KpipRevokeContractFactory> result = new ServiceResult<>();
        KpipReqBodyEmaid kpipRequest = new KpipReqBodyEmaid();
        kpipRequest.setEmaid(emaId);

        ServiceResult<Map<String, Object>> kpipResult = kpipApiService.revokeContractCert(kpipRequest, trackId);

        if (kpipResult.isFail()) {
            result.fail(500, "ChargeLink Error : " + kpipResult.getErrorMessage());
            return result;
        }

        KpipRevokeContractFactory factory = new KpipRevokeContractFactory(kpipResult.get());

        result.succeed(factory);
        return result;
    }

    private class KpipRevokeContractFactory {
        private Map<String, Object> rawResult;

        private boolean systemStatus = true;

        @Getter
        private String resultMessage;
        private String resultCode;

        public KpipRevokeContractFactory(Map<String, Object> rawResult) {
            this.rawResult = rawResult;
            try {
                resultCode = (String) rawResult.get("resultCode");
                resultMessage = (String) rawResult.get("resultMsg");
            } catch (Exception ex) {
                systemStatus = false;
                resultCode = "INTERNAL_FAIL";
                resultMessage = "resultCode 및 resultMsg 형식 불일치";
            }
        }

        public boolean isOK() {
            if (!systemStatus || resultCode == null) {
                return false;
            } else if (resultCode.equals("OK")) {
                return true;
            } else {
                return false;
            }
        }

    }

    // #endregion
    // #region CONTRACT-CHECK
    public ServiceResult<ContractStatus> getContractStatus(String emaId, String trackId) {
        ServiceResult<ContractStatus> result = new ServiceResult<>();

        // 직접 DB에서 가져올까 했지만 그래도 역할군을 나눠두는게
        // 이 클래스엔 많이 넣어두고, 만약 제한해야되면 Controller단에서 DTO새로만들어서 쓰라고 하자.
        ServiceResult<ContractStatus> statusService = contractManageService.getContractStatusByContractId(emaId);

        if (statusService.getSuccess()) {
            result.succeed(statusService.get());
        } else {
            result.fail(statusService.getErrorCode(), statusService.getErrorMessage());
        }
        return result;
        //

    }

    // #endregion
    // #region CONTRACT-INFO
    public ServiceResult<ContractInfo> getContractInfo(PncReqBodyContractInfo request, String trackId) {
        ServiceResult<ContractInfo> serviceResult = new ServiceResult<>();
        // 1. 요청바디의 값 확인
        String pcid;
        Long memberKey;
        String oemId;
        try {
            pcid = request.getPcid();
            oemId = request.getOemId();
            memberKey = request.getMemberKey();
        } catch (Exception ex) {
            serviceResult.fail(400, "요청값이 유효하지 않습니다.");
            String bodyJson = "";
            if (request != null) {
                try {
                    bodyJson = objectMapper.writeValueAsString(request);
                } catch (Exception e) {

                }
            }
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG:CONTRACT_GET_ERROR, TRACK ID: {}, MESSAGE: {}, Request: {}", trackId,
                        "요청값 캐스팅실패.",
                        bodyJson);
            }
            return serviceResult;
        }

        // 왜 여기선 oemId가 넘어올까? oemID도 사실 Unique인걸까? 그런 이야기는 없었다.
        ServiceResult<ContractMeta> findResult = contractManageService.findContractByMetaData(memberKey, pcid, oemId);
        if (findResult.isFail()) {
            // DB에 매칭되는 정보가 없음.
            serviceResult.fail(findResult.getErrorCode(), findResult.getErrorMessage());
            return serviceResult;
        }
        String contractId = findResult.get().getContractId();
        // 1-1. emaid
        String emaid = findResult.get().getEmaId();
        // 1-2. 계약 시작/종료 일자 (따로 저장중이라면 저장된 정보 가져오고, 저장중이 아니라면 인증서로부터 추출한다)

        // String변환기준과 사용처가 없어서 일단 null로 나오도록 처리함.
        String contractStartDt = findResult.get().getContractStartDtString();
        String contractEndDt = findResult.get().getContractEndDtString();

        String fullContCert = findResult.get().getFullContCert();

        // 체크
        // 치명적에러
        if (fullContCert == null) {
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG:CONTRACT_GET_ERROR, TRACK ID: {},CONTRACT_ID: , MESSAGE: {}", trackId, contractId,
                        "인증서 전문이 존재하지 않는 계약.");
            }
            serviceResult.fail(500, "데이터가 소실된 계약.");
        }
        ServiceResult<KpipVerifyContractFactory> verifyResult = kpipVerify(fullContCert, trackId);
        if (verifyResult.isFail()) {
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG:CONTRACT_GET_ERROR, TRACK ID: {}, MESSAGE: {}", trackId,
                        "KPIP 요청 실패.");
            }
            serviceResult.fail(500, "KPIP Connection Error");
        } else if (verifyResult.get().isOK()) {
            serviceResult.succeed(findResult.get().buildContractInfo());
        } else {
            serviceResult.fail(400, verifyResult.get().getStatus());
        }

        return serviceResult;
    }

    private ServiceResult<KpipVerifyContractFactory> kpipVerify(String contCert, String trackId) {
        ServiceResult<KpipVerifyContractFactory> result = new ServiceResult<>();

        KpipReqBodyVerifyContractCert verifyRequest = new KpipReqBodyVerifyContractCert();
        verifyRequest.setContCert(contCert);

        ServiceResult<Map<String, Object>> kpipResult = kpipApiService.verifyContractCert(verifyRequest, trackId);

        if (kpipResult.isFail()) {
            result.fail(500, "ChargeLink Error : " + kpipResult.getErrorMessage());
            return result;
        }

        KpipVerifyContractFactory factory = new KpipVerifyContractFactory(kpipResult.get());

        result.succeed(factory);
        return result;

    }

    private class KpipVerifyContractFactory {
        private Map<String, Object> rawResult;

        private boolean systemStatus = true;

        @Getter
        private String resultMessage;
        private String resultCode;
        @Getter
        private String status;

        public KpipVerifyContractFactory(Map<String, Object> rawResult) {
            this.rawResult = rawResult;
            try {
                resultCode = (String) rawResult.get("resultCode");
                resultMessage = (String) rawResult.get("resultMsg");
                status = (String) rawResult.get("ocspStatus");
            } catch (Exception ex) {
                systemStatus = false;
                resultCode = "INTERNAL_FAIL";
                resultMessage = "resultCode 및 resultMsg 형식 불일치";
                status = "EmptyResult";
            }
        }

        public boolean isOK() {
            if (!systemStatus || resultCode == null || status == null) {
                return false;
            } else if (resultCode.equalsIgnoreCase("OK") && status.equalsIgnoreCase("Good")) {
                return true;
            } else {
                return false;
            }
        }

    }
    // #endregion

    // #region CONTRACT-AUTH
    public ServiceResult<String> pncAuthorize(PncReqBodyAuthorize request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();

        String emaId = request.getEmaId();
        // 아래 정보들은 MSP에서 올라오긴 하는데, 실제 계약인증서 검증 시에는 emaId만 가지고 검증하므로 검증 과정에서는 필요 없다.
        long ecKey = request.getEcKey();

        // DB에서 체크
        ServiceResult<ContractMeta> queryResult = contractManageService.getContractByEmaId(emaId);
        if (!queryResult.getSuccess()) {
            // DB에 없음.
            serviceResult.fail(404, "No Contract Found");
            return serviceResult;
        }
        String contractId = queryResult.get().getContractId();

        // 1. KEPCO로 검증요청을 보내본다.
        ServiceResult<KpipAuthFactory> kpipAuthResult = kpipAuth(emaId, trackId);
        if (kpipAuthResult.getSuccess()) {
            // 요청자체가 성공한것이다.
            if (kpipAuthResult.get().isOK()) {
                // 요청결과도 OK다.
                serviceResult.succeed("OK");
            } else {
                // kpip 결과 실패다. 확실한 실패
                serviceResult.fail(500, kpipAuthResult.get().getResultMessage());
            }
        } else {

            // 2. 기타문제로 KEPCO에서 처리를 못해주면, 가지고있는 정보로 유효성을 검증해준다.
            ServiceResult<ContractMeta> internalAuthResult = contractManageService.checkAuth(contractId);
            if (internalAuthResult.getSuccess()) {
                // 내부판정결과 OK
                serviceResult.succeed("OK");
            } else {
                // 내부판정결과 FAIL
                serviceResult.fail(internalAuthResult.getErrorCode(), internalAuthResult.getErrorMessage());
            }
        }

        return serviceResult;

    }

    private ServiceResult<KpipAuthFactory> kpipAuth(String emaId, String trackId) {
        ServiceResult<KpipAuthFactory> result = new ServiceResult<>();

        KpipReqBodyEmaid authRequest = new KpipReqBodyEmaid();
        authRequest.setEmaid(emaId);

        ServiceResult<Map<String, Object>> kpipResult = kpipApiService.authorizePnc(authRequest, trackId);

        if (kpipResult.isFail()) {
            result.fail(500, "ChargeLink Error : " + kpipResult.getErrorMessage());
            return result;
        }

        KpipAuthFactory factory = new KpipAuthFactory(kpipResult.get());

        result.succeed(factory);
        return result;

    }

    private class KpipAuthFactory {
        private Map<String, Object> rawResult;

        private boolean systemStatus = true;

        @Getter
        private String resultMessage;
        private String resultCode;

        public KpipAuthFactory(Map<String, Object> rawResult) {
            this.rawResult = rawResult;
            try {
                resultCode = (String) rawResult.get("resultCode");
                resultMessage = (String) rawResult.get("resultMsg");
            } catch (Exception ex) {
                systemStatus = false;
                resultCode = "INTERNAL_FAIL";
                resultMessage = "resultCode 및 resultMsg 형식 불일치";
            }
        }

        public boolean isOK() {
            if (!systemStatus || resultCode == null) {
                return false;
            } else if (resultCode.equals("OK")) {
                return true;
            } else {
                return false;
            }
        }

    }
    // #endregion

    // #region CONVERT-CERT
    private String convertToPemFromBase64Der(String base64DerCert) {
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN CERTIFICATE-----\n");
        int lineLength = 64;
        for (int i = 0; i < base64DerCert.length(); i += lineLength) {
            int endIndex = Math.min(i + lineLength, base64DerCert.length());
            pem.append(base64DerCert.substring(i, endIndex));
            if (endIndex < base64DerCert.length()) {
                pem.append("\n");
            }
        }
        pem.append("\n-----END CERTIFICATE-----");
        return pem.toString();
    }

    private String mergeTwoPemCertsIntoChain(String leaf, String sub) {
        return leaf + '\n' + sub;
    }

    private String convertAndMergeCerts(String base64DerLeafCert, String base64DerSubCaCert) {
        String pemLeafCert = convertToPemFromBase64Der(base64DerLeafCert);
        String pemSubCaCert = convertToPemFromBase64Der(base64DerSubCaCert);
        return mergeTwoPemCertsIntoChain(pemLeafCert, pemSubCaCert);
    }
    // #endregion

}
