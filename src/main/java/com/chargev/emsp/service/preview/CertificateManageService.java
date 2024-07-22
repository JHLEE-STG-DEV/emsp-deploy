package com.chargev.emsp.service.preview;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.chargev.emsp.entity.cert.CertIdentity;
import com.chargev.emsp.entity.cert.Certification;
import com.chargev.emsp.entity.cert.SubCertification;
import com.chargev.emsp.model.dto.pnc.CertificateInfo;
import com.chargev.emsp.model.dto.pnc.CertificationMeta;
import com.chargev.emsp.repository.cert.CertIdentityRepository;
import com.chargev.emsp.repository.cert.CertificationRepository;
import com.chargev.emsp.repository.cert.SubCertificationRepository;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.cryptography.CertificateConversionService;
import com.chargev.emsp.service.cryptography.SHAService;
import com.chargev.utils.IdHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificateManageService {
    private final SHAService shaService;
    private final CertificationRepository certRepository;
    private final CertIdentityRepository certIdRepository;
    private final SubCertificationRepository subCertRepository;
    private final CertificateConversionService conversionService;
    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");

    public ServiceResult<CheckCertIssueCondition> checkCertCondition(Long ecKey, String trackId,
    boolean requestNew) {
        ServiceResult<CheckCertIssueCondition> serviceResult = new ServiceResult<>();
        if (ecKey == null) {
            serviceResult.fail(404, "Bad Request");
            apiLogger.warn("Message: {}, Track ID: {}", "입력값이 Null입니다.", trackId);
            return serviceResult;
        }

        // 우선 사실상id가 존재하면 가져온다.
        CertReqType reqType = CertReqType.FAIL;
        Optional<CertIdentity> certIdentity = Optional.empty();
        try {
            if (!certIdRepository.existsById(ecKey)) {
                CertIdentity newCertIdentity = new CertIdentity();
                newCertIdentity.setCreatedDate(new Date());
                newCertIdentity.setEcKey(ecKey);
                certIdRepository.saveAndFlush(newCertIdentity);
            }
        } catch (Exception ex) {
            // 이미 존재할 수 있음. 여기선 개의치않음.
            ex.printStackTrace();
        }
        try {
            certIdentity = certIdRepository.findById(ecKey);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (certIdentity.isEmpty()) {
            // 생성 자체에 실패했다.
            serviceResult.fail(500, "인증정보 생성에 실패하였습니다.");
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: ISSUE_CERT, MESSAGE: {} ,ecKey: {}, Track ID: {}",
                        "인증정보 조회에 실패하였습니다.", ecKey, trackId);
            }
        }
        // 풀링을 하진 않아서 좀더 여유롭긴하다.
        if (certIdentity.get().getWorking() > 0 && certIdentity.get().getWorked() == 0) {
            // 이미 발급절차중이다.
            // 그러면 그냥 동작중인 키를 주자. (working이면 거기에 대응되는 id 존재한다고 가정)
            CheckCertIssueCondition issueCondition = new CheckCertIssueCondition();
            issueCondition.setCertId(certIdentity.get().getCertId());
            issueCondition.setReqType(CertReqType.WORKING);
            serviceResult.succeed(issueCondition);
            return serviceResult;
        }

        // 연동되어있는것
        String certId = certIdentity.get().getCertId();

        // work하고있지 않다. contract를 발급해둔다.
        Certification cert;
        if (certId == null || !StringUtils.hasText(certId)) {
            certId = IdHelper.genLowerUUID32();
            cert = new Certification();
            cert.setCertId(certId);
            cert.setCreatedDate(new Date());
            cert.setEcKey(ecKey);
            // DB에 저장
            try {
                cert = certRepository.saveAndFlush(cert);

            } catch (Exception ex) {
                ex.printStackTrace();
                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG: ISSUE_CERT, MESSAGE: {} ,ecKey: {}, Track ID: {}",
                            "신규 CERT 정보 테이블 저장 실패", ecKey, trackId);
                }
                serviceResult.fail(500, "Failed to INSERT DB");
            }

            // 일단 db에 저장을 쳐서 생성중절차를 밟게함.
            certIdentity.get().setCertId(certId);
            certIdentity.get().setWorking(1);
            certIdentity.get().setWorked(0);
            try {
                certIdRepository.saveAndFlush(certIdentity.get());
            } catch (Exception ex) {
                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG: ISSUE_CERT, MESSAGE: {} ,ecKey: {}, Track ID: {}",
                            "CERT_IDENTITY 정보 WORKING플래그 작성 실패", ecKey, trackId);
                }
            }
            reqType = CertReqType.NEW;
        } else {
            // 기존에 cert가
            reqType = CertReqType.UPDATE;
        }

        try {
            cert = certRepository.findById(certId)
                    .orElseThrow(() -> new RuntimeException("CERT not found"));

        } catch (Exception ex) {
            serviceResult.fail(400, "Failed to Query Cert");
            return serviceResult;
        }
        // 관련 cert 만들든 찾아오든 했다.
        // 이제 requestNew와 관련하여 조절한다.
        // 이제 존재여부에 따라 Update할지 New할지 체크한다. (무조건 request는 업데이트방향으로)
        if(requestNew){
            if (reqType.equals(CertReqType.UPDATE)) {
                // 파일이 존재한다. 1. 살아있으면 그냥 업데이트로 보낸다. 2. 죽어있으면 New로 갈아끼운다.
                if (isCertActive(cert)) {
                    // 살아있다. 이건 중복이다.
                    serviceResult.fail(409, "중복 가입");
                    return serviceResult;
    
                } else {
                    // 죽어있다. 이놈은 없애고, New인것처럼 행동해야한다.
                    // Flag상 죽어있으니까, KEPKO에는 안쏴도되는거로 가정
                    certId = IdHelper.genLowerUUID32();
                    Certification replacedCert = new Certification();
                    replacedCert.setCertId(certId);
                    replacedCert.setCreatedDate(new Date());
                    replacedCert.setEcKey(ecKey);
                    try {
                        replacedCert = certRepository.saveAndFlush(replacedCert);
    
                    } catch (Exception ex) {
    
                        ex.printStackTrace();
                        if (apiLogger.isErrorEnabled()) {
                            apiLogger.error("TAG: ISSUE_CERT, MESSAGE: {} ,ecKey: {}, Track ID: {}",
                                    "CERT 정보 테이블 저장 실패 (REPLACE)", ecKey, trackId);
                        }
                        serviceResult.fail(500, "Failed to INSERT DB");
                    }
                    certIdentity.get().setCertId(certId);
                    certIdentity.get().setWorking(1);
                    certIdentity.get().setWorked(0);
                    try {
                        certIdRepository.saveAndFlush(certIdentity.get());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        if (apiLogger.isErrorEnabled()) {
                            apiLogger.error("TAG: ISSUE_CERT, MESSAGE: {} ,ecKey: {}, Track ID: {}",
                                    "CERT_IDENTITY 정보 WORKING플래그 작성 실패", ecKey, trackId);
                        }
                    }
                    reqType = CertReqType.NEW;
                }
            }
        } else if (reqType.equals(CertReqType.NEW)) {
            // Cert 가 없는데 UPDATE로 요청한 것이다.
            serviceResult.fail(404, "업데이트할 인증서 정보가 존재하지 않습니다.");
            return serviceResult;
        }

       

        CheckCertIssueCondition issueCondition = new CheckCertIssueCondition();
        issueCondition.setCertId(certId);
        issueCondition.setReqType(reqType);
        serviceResult.succeed(issueCondition);
        return serviceResult;
    }

    private boolean isCertActive(Certification cert) {
        if (cert == null)
            return false;

        if (cert.getStatus() != 0 || cert.getFullCert() == null) {
            // 플래그까지 했으면 맞음.
            return false;
        }
        return true;

    }
 public ServiceResult<String> finishIssueCertIdentity(Long ecKey,  String trackId,
            boolean success, int errorCode, String message) {
        ServiceResult<String> result = new ServiceResult<>();
        Optional<CertIdentity> certIdentity = Optional.empty();

        try {
            certIdentity = certIdRepository.findById(ecKey);
        } catch (Exception ex) {

        }
        if (certIdentity.isEmpty()) {
            apiLogger.error("TAG:CERTT_FINISH_FAIL, ecKey: {},  MESSAGE: {}, TRACKID: {}", ecKey,
                     "UNLOCK할 CertId조회 불가", trackId);
        }
        CertIdentity unlockedIdentity = certIdentity.get();
        unlockedIdentity.setWorked(1);
        unlockedIdentity.setWorking(0);

        // Contract자체의 메세지도 여기서 남겨주자.

        // 근데 중복이면 건드리면안되고 그냥 공중분해시킨다.(저쪽도 중복여부는 추적하지않음.)
        if (errorCode == 409) {
            result.succeed("OK");
            return result;

        }

        Optional<Certification> realtedContract = Optional.empty();
        try {
            realtedContract = certRepository.findById(unlockedIdentity.getCertId());
        } catch (Exception ex) {

        }
        if (realtedContract.isPresent()) {
            Certification relatedContractEntity = realtedContract.get();
            if (success) {
                relatedContractEntity.setStatus(0);
                relatedContractEntity.setStatusMessage("OK");
            } else {
                relatedContractEntity.setStatus(-1);
                relatedContractEntity.setStatusMessage(message);
            }
            try {
                certRepository.save(relatedContractEntity);
            } catch (Exception ex) {
                apiLogger.error(
                        "TAG:CERT_FINISH_FAIL, CertId: {}, STATUS: {}, STATUS_MESSAGE: {}, MESSAGE: {}, TRACKID: {}",
                        unlockedIdentity.getCertId(), relatedContractEntity.getStatus(),
                        relatedContractEntity.getStatusMessage(), "성공여부를 저장 실패", trackId);
            }
        }
        try {
            certIdRepository.save(unlockedIdentity);
        } catch (Exception ex) {
            apiLogger.error("TAG:CERT_FINISH_FAIL, ECKEY: {}, MESSAGE: {}, TRACKID: {}",
                    ecKey,  "UNLOCK 플래그 저장 실패", trackId);
        }

        result.succeed("OK");
        return result;
    }
    // 발급요청까지도 id로 가져오는식으로 변경.
    // 요청바디를 전부기록해야할까? 일단 csr만 해보고 이후에 알아서
    public ServiceResult<String> initNewCert(String id, String csr) {
        ServiceResult<String> result = new ServiceResult<>();

        Certification cert = new Certification();
        cert.setCertId(id);
        cert.setRequestDate(new Date());

        try {
            certRepository.saveAndFlush(cert);
            result.succeed(id);
        } catch (Exception ex) {
            result.fail(500, "Failed to Insert DB");
        }
        return result;
    }

    public ServiceResult<String> saveSubCert(Long ecKey, String subDER) {
        ServiceResult<String> result = new ServiceResult<>();
        String subCertId = IdHelper.genLowerUUID32();

        // 중복확인을 위한 해시
        String hashedCert = shaService.sha256Hash(subDER, "");
        if (hashedCert == null) {
            result.fail(500, "Failed to Hash DER");
            return result;
        }

        Optional<SubCertification> duplicated = Optional.empty();
        try {
            duplicated = subCertRepository.findFirstByHashedCert(hashedCert);
        } catch (Exception ex) {
            result.fail(500, "Failed to Query DB");
            return result;
        }

        if (duplicated.isPresent() && (duplicated.get().getEcKey().equals(ecKey))) {
            // 똑같은게 있음. 그대로 씀.
            subCertId = duplicated.get().getCertId();
        } else {
            // 없으니 새롭게 저장함.
            SubCertification newCert = new SubCertification();
            newCert.setCertId(subCertId);
            newCert.setEcKey(ecKey);
            newCert.setHashedCert(hashedCert);
            newCert.setFullCert(subDER);

            // DB에 넣으니까 로컬에 저장하지 않음. 밖에서 로깅하기도하니까.
            // DB에 저장
            try {
                subCertRepository.save(newCert);
            } catch (Exception ex) {
                result.fail(500, "Failed to INSERT DB");
                return result;
            }
        }

        result.succeed(subCertId);
        return result;
    }

    public ServiceResult<String> saveNewCert(String certId, Long ecKey, String subCertId, String leafPem,
            boolean clearAndSave) {
        ServiceResult<String> result = new ServiceResult<>();

        Optional<Certification> certResult = Optional.empty();
        try {
            certResult = certRepository.findById(certId);

        } catch (Exception ex) {

        }
        if (certResult.isEmpty()) {
            result.fail(404, "Cert정보를 찾을 수 없음.");
            return result;

        }

        // 만약 Update면 이전것을 전부 revoke시킨다.
        List<String> deletedIds = new LinkedList<>();
        if (clearAndSave) {
            ServiceResult<List<String>> revokeResult = revokeAllEcCerts(ecKey);
            if (!revokeResult.getSuccess()) {
                result.fail(revokeResult.getErrorCode(), revokeResult.getErrorMessage());
                return result;
            }
            deletedIds = revokeResult.get();
        }

        Certification cert = certResult.get();
        // id, ecKey, sub설정
        cert.setCertId(certId);
        cert.setEcKey(ecKey);
        cert.setSubCertId(subCertId);
        cert.setFullCert(leafPem);

        // 생성 타임스탬프
        cert.setCreatedDate(new Date());

        // 내용을 파싱해서 필요한것을 넣음.
        try {

            CertificateInfo certInfo = conversionService.getCertInfoFromPEM(leafPem);

            String issuerName = certInfo.getIssuerCN();
            // issuerKey가 이게 맞는지 확인
            String issuerKey = certInfo.getPublicKey();
            String serialNumber = certInfo.getFormattedSerialNumber();
            serialNumber = serialNumber.replace("0x", "");

            Date expireDate = certInfo.getExpirationDate();

            cert.setIssuerName(issuerName);
            cert.setIssuerKey(issuerKey);
            cert.setSerialNumber(serialNumber);
            cert.setExpireDate(expireDate);

        } catch (Exception ex) {
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG:CERT_PARSE_ERROR,  MESSAGE: {}, Request: {}",
                        "인증서 분해 실패.", leafPem);
                ex.printStackTrace();
            }
            ex.printStackTrace();
            result.fail(500, "Failed to parse PEM");
            return result;
        }
        // DB에 저장
        try {
            certRepository.saveAndFlush(cert);
            result.succeed(certId);
        } catch (Exception ex) {
            result.fail(500, "Failed to INSERT DB");
        }

        // 실패했어도 revoke는 이미 되었다. 원복할 이유가 없다.

        return result;
    }

    private ServiceResult<List<String>> revokeAllEcCerts(Long ecKey) {
        ServiceResult<List<String>> result = new ServiceResult<>();

        List<String> deletedIds = new LinkedList<>();

        // ecKey로 찾아옴.
        List<Certification> activeCerts;
        try {
            activeCerts = certRepository.findByEcKeyAndStatus(ecKey, 0);
        } catch (Exception ex) {
            result.fail(500, "Failed to query Certs");
            return result;
        }

        // 없음
        if (activeCerts.isEmpty()) {
            result.succeed(deletedIds);
            return result;
        }

        // 하나씩 delete처리함.
        for (Certification cert : activeCerts) {
            try {
                revokeCert(cert);
                deletedIds.add(cert.getCertId());
            } catch (Exception ex) {
                // 실패해도 내가 뭘할수있지?
            }
        }

        result.succeed(deletedIds);
        return result;
    }

    private void revokeCert(Certification cert) {
        cert.setRevokedDate(new Date());
        cert.setStatus(2);
        certRepository.saveAndFlush(cert);
    }

    public ServiceResult<String> revokeCertByCertId(String certId) {
        ServiceResult<String> result = new ServiceResult<>();
        if (certId == null) {
            result.fail(400, "Bad Request");
            return result;
        }
        Optional<Certification> cert = Optional.empty();
        try {
            cert = certRepository.findById(certId);
        } catch (Exception ex) {
            result.fail(404, "No Certs Found");
            return result;
        }
        if (cert.isEmpty()) {

            result.fail(404, "No Certs Found");
            return result;
        }

        try {
            revokeCert(cert.get());
            result.succeed(certId);
        } catch (Exception ex) {
            result.fail(500, "Failed to update DB");
        }
        return result;
    }

    public ServiceResult<CertificationMeta> findCertByPEM(Long ecKey, String pem) {
        ServiceResult<CertificationMeta> result = new ServiceResult<>();
        if (ecKey == null || pem == null) {
            result.fail(400, "Bad Request");
            return result;
        }

        // hashed로 바로 쿼리하는것이 best지만, ecKey라는 1차가 있으니 그럴 위험을 감수할 이유가 없다. (1대1이 보장되는것이
        // 기본로직이라고 가정하면 경제적이기도 하다.)
        // ecKey로 찾아옴.
        List<Certification> activeCerts;
        try {
            activeCerts = certRepository.findByEcKeyAndStatus(ecKey, 0);
        } catch (Exception ex) {
            result.fail(404, "No Certs Found");
            return result;
        }

        if (activeCerts.isEmpty()) {
            // 없다? 실패로 쳐야할까? 일단 404로하자.
            result.fail(404, "No Certs Found");
            return result;
        }
        Certification matchedCert = null;
        for (Certification cert : activeCerts) {
            // 여러개면? 유일이라고 가정한다. 내용확인은 보내놓았다.
            if (isSame(cert, pem)) {
                matchedCert = cert;
            }

        }
        if (matchedCert == null) {

            result.fail(404, "No Certs Found");

        } else {
            result.succeed(buildMeta(matchedCert));
        }
        return result;
    }

    private boolean isSame(Certification cert, String pem) {
        if (cert.getFullCert() == null)
            return false;
        return cert.getFullCert().equals(pem);

    }

    private CertificationMeta buildMeta(Certification cert) {
        if (cert == null)
            return null;
        CertificationMeta meta = new CertificationMeta();
        meta.setCertId(cert.getCertId());
        meta.setFullCert(cert.getFullCert());

        return meta;
    }

    public ServiceResult<CertificationMeta> findCertByEcKey(Long ecKey){
        ServiceResult<CertificationMeta> result = new ServiceResult<>();

        Optional<CertIdentity> targetId = Optional.empty();
        try{
            targetId =  certIdRepository.findById(ecKey);
        }catch(Exception ex){
            ex.printStackTrace();
            result.fail(500, "Failed to query DB");

        }
        if(targetId.isEmpty() || targetId.get().getCertId() == null){
            result.fail(404, "관련 인증서를 찾을 수 없습니다.");
            return result;
        }

        // 여기에 연동된게 목표다.
        Optional<Certification> target = Optional.empty();

        
        try {
            target = certRepository.findById(targetId.get().getCertId());
        } catch (Exception ex) {
            result.fail(500, "Failed to query DB");
            return result;
        }
        if (target.isEmpty()) {
            result.fail(404, "No Contract Found");
        } else {
            result.succeed(buildMeta(target.get()));
        }
        return result;

    }
}
