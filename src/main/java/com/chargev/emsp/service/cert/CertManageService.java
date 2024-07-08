package com.chargev.emsp.service.cert;

import com.chargev.emsp.model.dto.pnc.CertificationMeta;
import com.chargev.emsp.service.ServiceResult;

public interface CertManageService {

    public ServiceResult<String> saveSubCert(Long ecKey, String subDER);
    public ServiceResult<String> createNewCert(Long ecKey, String subCertId, String leafPem, boolean clearAndSave);

    public ServiceResult<CertificationMeta> findCertByPEM(Long ecKey, String pem);
    public ServiceResult<String> revokeCertByCertId(String certId);
}
