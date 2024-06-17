package com.chargev.emsp.service.cryptography;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class ECDSASignatureService {

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public PrivateKey loadPrivateKey(String privateKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePrivate(spec);
    }

    public PublicKey loadPublicKey(String publicKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePublic(spec);
    }

    public String signValue(String value, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(value.getBytes());
        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    public boolean verifySignature(String value, String signatureStr, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initVerify(publicKey);
        signature.update(value.getBytes());
        byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);
        return signature.verify(signatureBytes);
    }
}