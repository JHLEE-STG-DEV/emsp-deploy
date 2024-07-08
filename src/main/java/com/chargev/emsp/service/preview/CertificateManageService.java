package com.chargev.emsp.service.preview;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.cert.Certification;
import com.chargev.emsp.entity.cert.SubCertification;
import com.chargev.emsp.model.dto.pnc.CertificateInfo;
import com.chargev.emsp.model.dto.pnc.CertificationMeta;
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
    private final SubCertificationRepository subCertRepository;
    private final CertificateConversionService conversionService;
    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");

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


    public ServiceResult<String> saveNewCert(String certId, Long ecKey, String subCertId, String leafPem, boolean clearAndSave) {
        ServiceResult<String> result = new ServiceResult<>();

        Optional<Certification> certResult = Optional.empty();
        try{
            certResult =certRepository.findById(certId);

        }catch(Exception ex){

        }
        if(certResult.isEmpty()){
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
        try{

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
    
        }catch(Exception ex){
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG:CERT_PARSE_ERROR,  MESSAGE: {}, Request: {}",
                        "인증서 분해 실패.",leafPem);
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

    public ServiceResult<String> revokeCertByCertId(String certId){
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
        if(cert.isEmpty()){
            
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
    public ServiceResult<CertificationMeta> findCertByPEM(Long ecKey, String pem){
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
        if(cert.getFullCert() == null)
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
}
