package com.chargev.emsp.service.cert;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.cert.Certification;
import com.chargev.emsp.entity.cert.SubCertification;
import com.chargev.emsp.model.dto.pnc.CertificateHashData;
import com.chargev.emsp.model.dto.pnc.CertificationMeta;
import com.chargev.emsp.model.dto.pnc.HashAlgorithm;
import com.chargev.emsp.repository.cert.CertificationRepository;
import com.chargev.emsp.repository.cert.SubCertificationRepository;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.cryptography.SHAService;
import com.chargev.utils.IdHelper;
import com.chargev.utils.LocalFileManager;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

// 내부DB에 별도로 저장해서 관리함.
@Service
@RequiredArgsConstructor
public class CertManageServiceImpl implements CertManageService {
    private final CertificationRepository certRepository;
    private final SubCertificationRepository subCertRepository;
    private static final Logger logger = LoggerFactory.getLogger(CertManageServiceImpl.class);
    private final SHAService shaService;

    private boolean objectLogActive = true;
    private Path leafCertDirectoryPath;
    private Path subCertDirectoryPath;

    @PostConstruct
    public void init() {
        try {
            Path certDirectoryPath = Paths.get("/var/log/certs");
            LocalFileManager.ensureDirectory(certDirectoryPath);
            leafCertDirectoryPath = certDirectoryPath.resolve("leaf");
            subCertDirectoryPath = certDirectoryPath.resolve("sub");
            LocalFileManager.ensureDirectory(leafCertDirectoryPath);
            LocalFileManager.ensureDirectory(subCertDirectoryPath);

        } catch (Exception e) {
            objectLogActive = false;
            logger.error(e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    // Sub의 내용이 외부에서 필요없으면 따놓을 이유가 없다. Base64를 기준으로 저장하자.
    @Override
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
            duplicated = subCertRepository.findByHashedCert(hashedCert);
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

            // 쌩으로 로컬에 저장.
            if (objectLogActive) {
                try {
                    LocalFileManager.writeToFile(subDER, subCertDirectoryPath.resolve(subDER + "_der"));
                } catch (Exception ex) {

                    // 실패
                    ex.printStackTrace();
                    logger.error("leafPem Object 저장 실패.");
                }
            }

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

    @Override
    public ServiceResult<String> createNewCert(Long ecKey, String subCertId, String leafPem, boolean clearAndSave) {
        ServiceResult<String> result = new ServiceResult<>();

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

        Certification cert = new Certification();
        // id, ecKey, sub설정
        String certId = IdHelper.genLowerUUID32();
        cert.setCertId(certId);
        cert.setEcKey(ecKey);
        cert.setSubCertId(subCertId);

        // 생성 타임스탬프
        cert.setCreatedDate(new Date());

        // 내용을 파싱해서 필요한것을 넣음.

        String issuerName = "";
        String issuerKey = "";
        String serialNumber = "";

        Date expireDate = new Date();

        cert.setIssuerName(issuerName);
        cert.setIssuerKey(issuerKey);
        cert.setSerialNumber(serialNumber);
        cert.setExpireDate(expireDate);

        // 쌩으로 로컬에 저장.
        if (objectLogActive) {
            try {
                LocalFileManager.writeToFile(leafPem, leafCertDirectoryPath.resolve(certId + "_pem"));
            } catch (Exception ex) {

                // 실패
                ex.printStackTrace();
                logger.error("leafPem Object 저장 실패.");
            }
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
                logger.error("Failed to revoke. : " + cert.getCertId());
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

    @Override
    public ServiceResult<CertificationMeta> findCertByHashData(Long ecKey, CertificateHashData hashed) {
        ServiceResult<CertificationMeta> result = new ServiceResult<>();
        if (ecKey == null || hashed == null) {
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
            if (isSame(cert, hashed)) {
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

    @Override
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
            logger.error("Failed to revoke. : " + certId);
            result.fail(500, "Failed to update DB");
        }
        return result;
        
    }

    private boolean isSame(Certification cert, CertificateHashData hashed) {
        HashAlgorithm algorithm = hashed.getHashAlgorithm();
        // serialNumber 비교
        if (!cert.getSerialNumber().equals(hashed.getSerialNumber())) {
            return false;
        }
        // issuerName 비교
        if (!hashed.getIssuerNameHash().equals(shaService.hash(algorithm, cert.getIssuerName()))) {
            return false;
        }
        // issuerKey 비교
        if (!hashed.getIssuerKeyHash().equals(shaService.hash(algorithm, cert.getIssuerKey()))) {
            return false;
        }
        return true;
    }

    private CertificationMeta buildMeta(Certification cert) {
        if (cert == null)
            return null;
        CertificationMeta meta = new CertificationMeta();
        meta.setCertId(cert.getCertId());

        return meta;
    }
}
