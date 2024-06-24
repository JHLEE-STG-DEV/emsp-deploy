package com.chargev.emsp.service.cryptography;

import org.springframework.stereotype.Service;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

import javax.security.auth.x500.X500Principal;


@Service
@Slf4j
public class ECKeyPairService {
    // 전체 PKI 이중 키 알고리듬은 ECDSA, 커브는 Prime256v1 으로 통일한다. 
    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public KeyPair generateECKeyPair() {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("EC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1"); // Prime256v1
        try {
            keyPairGenerator.initialize(ecSpec, new SecureRandom());
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
        }
        return keyPairGenerator.generateKeyPair();
    }

    public String getPublicKeyAsBase64(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    public String getPrivateKeyAsBase64(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    public String generateCSR(KeyPair keyPair, String subject)  {
        X500Principal subjectName = new X500Principal(subject);

        try {
            ContentSigner signGen = new JcaContentSignerBuilder("SHA256withECDSA").build(keyPair.getPrivate());
            JcaPKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subjectName, keyPair.getPublic());
            PKCS10CertificationRequest csr = builder.build(signGen);

            return Base64.getEncoder().encodeToString(csr.getEncoded());
        }
        catch (Exception e) {
        }
        return null;
    }
}