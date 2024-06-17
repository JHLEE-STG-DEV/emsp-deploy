package com.chargev.emsp.service.cryptography;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
public class JwtTokenService {
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

    public String generateToken(String subject, Map<String, Object> claims, long expirationTime, String privateKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PrivateKey privateKey = loadPrivateKey(privateKeyStr);

        long realExpirationTime = System.currentTimeMillis() + expirationTime;
        if(expirationTime == -1) {
            realExpirationTime = -1;
        }

        return Jwts.builder()
        .subject(subject)
        .claims(claims)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(realExpirationTime == -1 ? null : new Date(realExpirationTime))
        .signWith(privateKey)
        .compact();
    }

    public boolean validateToken(String token, String publicKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PublicKey publicKey = loadPublicKey(publicKeyStr);
        try {
             Jws<Claims> claims = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);
             if(claims != null)
                return true;
             else 
                return false;
        } catch (Exception e) {
             return false;
        }
    }
}