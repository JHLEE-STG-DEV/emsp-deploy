package com.chargev.emsp.service.cryptography;

import java.io.ByteArrayInputStream;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class CertificateService {

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
    }

    // Base64 인코딩된 DER 형식의 인증서에서 만료 날짜를 추출합니다.
    public Date getExpiredDate(String base64Cert)  {
        X509Certificate certificate = null;
        try {
            certificate = getCertificateFromBase64(base64Cert);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if(certificate == null) {
            return null;
        }

        return certificate.getNotAfter();
    }

    // Base64 인코딩된 DER 형식의 인증서에서 발급 날짜를 추출합니다.
    public Date getIssuedDate(String base64Cert) {
        X509Certificate certificate = null;
        try {
            certificate = getCertificateFromBase64(base64Cert);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if(certificate == null) {
            return null;
        }

        return certificate.getNotBefore();
    }

    // Base64 인코딩된 DER 형식의 인증서를 PEM 형식의 X.509 인증서로 변환합니다.
    public String convertToPEM(String base64Cert) {
        byte[] decodedCert = Base64.decode(base64Cert); // Base64 디코딩
        return convertToPEMFromDER(decodedCert);
    }

    // PEM 형식의 X.509 인증서를 Base64 인코딩된 DER 형식으로 변환합니다.
    public String convertToBase64DER(String pemCert) {
        if(pemCert == null || pemCert.isEmpty()) {
            return null;
        }
        return pemCert.replace("-----BEGIN CERTIFICATE-----", "")
                                   .replace("-----END CERTIFICATE-----", "")
                                   .replaceAll("\\s", "");
    }

    private X509Certificate getCertificateFromBase64(String base64Cert)  {
        byte[] decodedCert = Base64.decode(base64Cert); // Base64 디코딩
        CertificateFactory certFactory = null;
        try {
            certFactory = CertificateFactory.getInstance("X.509", "BC");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        X509Certificate certificate = null;
        try {
            certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(decodedCert));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return certificate;
    }

    private String convertToPEMFromDER(byte[] derCert) {
        String base64Cert = Base64.toBase64String(derCert);
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN CERTIFICATE-----\n");
        pem.append(base64Cert);
        pem.append("\n-----END CERTIFICATE-----");
        return pem.toString();
    }
}
