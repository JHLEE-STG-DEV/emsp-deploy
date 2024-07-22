package com.chargev.emsp.service.cryptography;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Service;

import com.chargev.emsp.model.dto.pnc.CertificateInfo;

@Service
public class CertificateConversionService {


    // @PostConstruct
    // public void init() {
    //     Security.addProvider(new BouncyCastleProvider());
    // }
    static {
        // Bouncy Castle 프로바이더를 추가
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
        if (pemCert == null || pemCert.isEmpty()) {
            return null;
        }
        // Remove PEM header, footer, and all whitespace characters
        return pemCert.replaceAll("-----BEGIN [^-]+-----", "")
                      .replaceAll("-----END [^-]+-----", "")
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

        int lineLength = 64;
        for (int i = 0; i < base64Cert.length(); i += lineLength) {
            int endIndex = Math.min(i + lineLength, base64Cert.length());
            pem.append(base64Cert.substring(i, endIndex));
            if (endIndex < base64Cert.length()) {
                pem.append("\n");
            }
        }
        
        pem.append("\n-----END CERTIFICATE-----");
        return pem.toString();
    }

    // PEM 형식의 X.509 인증서에서 인증서 정보를 추출한다.
    public CertificateInfo getCertInfoFromPEM(String pemCert) {

        CertificateInfo result = new CertificateInfo();

        // PEM 문자열을 바이트 배열로 변환
        byte[] certBytes = pemCert.getBytes();
        System.out.println("Converted to DER bytes: " + certBytes.length);

        // 바이트 배열을 X.509 인증서로 변환
        X509Certificate cert = getX509CertificateFromBytes(certBytes);
        if (cert == null) {
            return result; // 예외 처리를 추가할 수 있음
        }

        // 필요한 정보 추출
        String crlDistributionPoints = getCRLDistributionPoints(cert); // CRL 배포 주소
        String ocspUrl = getOCSPUrl(cert);
        Date expirationDate = cert.getNotAfter(); // 인증서 만료일
        Date issueDate = cert.getNotBefore(); // 인증서 발급일
        String issuerDN = cert.getIssuerX500Principal().getName(); // Issuer DN
        String issuerCN = extractCNFromDN(issuerDN); // Issuer CN
        String serialNumber = cert.getSerialNumber().toString(); // Serial Number
        java.math.BigInteger sn = cert.getSerialNumber();
        String formattedSerialNumber = getFormattedSerialNumber(sn);
        String publicKey = cert.getPublicKey().toString(); // Issuer's Public Key
        String signatureAlgorithm = cert.getSigAlgName(); // Signature Algorithm
        String subjectDN = cert.getSubjectX500Principal().getName(); // Subject DN
        String subjectCN = extractCNFromDN(subjectDN);

        result.setCrlDistributionPoints(crlDistributionPoints);
        result.setOcspUrl(ocspUrl);
        result.setExpirationDate(expirationDate);
        result.setIssueDate(issueDate);
        result.setIssuerDN(issuerDN);
        result.setIssuerCN(issuerCN);
        result.setSerialNumber(serialNumber);
        result.setFormattedSerialNumber(formattedSerialNumber);
        result.setPublicKey(publicKey);
        result.setSignatureAlgorithm(signatureAlgorithm);
        result.setSubjectDN(subjectDN);
        result.setSubjectCN(subjectCN);

        return result;
    }

    // 바이트 배열을 X.509 인증서로 변환한다.
    private X509Certificate getX509CertificateFromBytes(byte[] certBytes) {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
            System.out.println("Successfully parsed X509Certificate: " + certificate);
            return certificate;
        } catch (CertificateException e) {
            System.err.println("CertificateException: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }
        return null;
    }

    // X.509 인증서 객체에서 CRL 배포 포인트를 얻는다.
    private String getCRLDistributionPoints(X509Certificate cert) {
        try {
            byte[] crldpExt = cert.getExtensionValue("2.5.29.31");
            if (crldpExt == null) {
                return "No CRL Distribution Points found in the certificate.";
            }

            ASN1InputStream oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(crldpExt));
            ASN1Primitive derObjCrlDP = oAsnInStream.readObject();
            DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;
            byte[] crldpExtOctets = dosCrlDP.getOctets();
            ASN1InputStream oAsnInStream2 = new ASN1InputStream(new ByteArrayInputStream(crldpExtOctets));
            ASN1Primitive derObj2 = oAsnInStream2.readObject();
            CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);
            StringBuilder crlUrls = new StringBuilder();

            for (DistributionPoint dp : distPoint.getDistributionPoints()) {
                DistributionPointName dpn = dp.getDistributionPoint();
                if (dpn != null) {
                    if (dpn.getType() == DistributionPointName.FULL_NAME) {
                        GeneralNames gns = (GeneralNames) dpn.getName();
                        GeneralName[] names = gns.getNames();
                        for (GeneralName name : names) {
                            if (name.getTagNo() == GeneralName.uniformResourceIdentifier) {
                                String url = name.getName().toString();
                                crlUrls.append(url).append("\n");
                            }
                        }
                    }
                }
            }

            return crlUrls.toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing CRL Distribution Points.";
        }
    }
    // DN 문자열에서 CN을 추출하는 메서드
    private String extractCNFromDN(String dn) {
        try {
            LdapName ldapDN = new LdapName(dn);
            for (Rdn rdn : ldapDN.getRdns()) {
                if ("CN".equalsIgnoreCase(rdn.getType())) {
                    return rdn.getValue().toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // X.509 인증서 객체에서 ocsp url을 얻는다.
    private String getOCSPUrl(X509Certificate cert) {
        try {
            byte[] aiaExtensionValue = cert.getExtensionValue("1.3.6.1.5.5.7.1.1");
            if (aiaExtensionValue == null) {
                return "No OCSP URL found in the certificate.";
            }

            ASN1InputStream oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(aiaExtensionValue));
            ASN1Primitive derObjAIA = oAsnInStream.readObject();
            DEROctetString dosAIA = (DEROctetString) derObjAIA;
            byte[] aiaExtOctets = dosAIA.getOctets();
            ASN1InputStream oAsnInStream2 = new ASN1InputStream(new ByteArrayInputStream(aiaExtOctets));
            ASN1Primitive derObj2 = oAsnInStream2.readObject();
            AuthorityInformationAccess authorityInformationAccess = AuthorityInformationAccess.getInstance(derObj2);
            StringBuilder ocspUrls = new StringBuilder();

            AccessDescription[] accessDescriptions = authorityInformationAccess.getAccessDescriptions();
            for (AccessDescription ad : accessDescriptions) {
                if (ad.getAccessMethod().equals(AccessDescription.id_ad_ocsp)) {
                    GeneralName gn = ad.getAccessLocation();
                    if (gn.getTagNo() == GeneralName.uniformResourceIdentifier) {
                        String url = gn.getName().toString();
                        ocspUrls.append(url).append("\n");
                    }
                }
            }

            return ocspUrls.toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing OCSP URLs.";
        }
    }

    // 시리얼 넘버를 16진수 형식으로 반환하는 메서드
    private String getFormattedSerialNumber(java.math.BigInteger serialNumber) {
        return "0x" + serialNumber.toString(16).toUpperCase();
    }

    // CRL 디코딩
    public void decodeCRL(String base64CRL) {
        try {
            // Base64 디코딩 (KEPCO에서 두 번 인코딩하여 보내므로 두 번 디코딩한다)

            byte[] decodedCRL = java.util.Base64.getDecoder().decode(base64CRL);

            // PEM 데이터를 ByteArrayInputStream으로 변환
            ByteArrayInputStream bais = new ByteArrayInputStream(decodedCRL);

            // CRL 객체로 변환
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            bais.reset();
            X509CRL crl = (X509CRL) certFactory.generateCRL(bais);
            // CRL 정보 출력
            System.out.println("Issuer: " + crl.getIssuerX500Principal());
            System.out.println("This Update: " + crl.getThisUpdate());
            System.out.println("Next Update: " + crl.getNextUpdate());
            System.out.println("Revoked Certificates: " + crl.getRevokedCertificates());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String decodeBase64(String base64Something){
        
        byte[] decodedBytes = java.util.Base64.getMimeDecoder().decode(base64Something);

        // Convert byte array to String using the appropriate Charset
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

}
