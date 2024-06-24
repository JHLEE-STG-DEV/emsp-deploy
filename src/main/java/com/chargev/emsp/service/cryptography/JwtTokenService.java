package com.chargev.emsp.service.cryptography;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.authenticationentity.AuthSubject;
import com.chargev.emsp.entity.authenticationentity.TokenRequest;
import com.chargev.emsp.repository.authentication.AuthSubjectRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Slf4j

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private static final String AUTH_PASSWORD = "password";
    private static final String AUTH_CODE = "authorization_code";
    private static final String AUTH_IMPLICIT = "implicit";
    private static final String AUTH_CLIENT_CREDENTIALS = "client_credentials";
    private static final String AUTH_REFRESH_TOKEN = "refresh_token";
    // 퍼블릭 키에 대해서 상태 유지를 할 수 있는 방법을 고민해야 함 
    private static String oemPublicKey = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEicyEdsjOCk0qwzLQW6iedldkkWfarNZ041F21pTq4hT/ak46TVrBiSxeqrerQx2mm9YtzLASoJ75zhUOn7WgMQ==";

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
    }

    private boolean canIssueTokeByThatMethods(TokenRequest request) {
        // 토큰 발급 가능한 방법을 체크한다.
        // TODO 실제 구현 필요
        return true; 
    }

    public PrivateKey loadPrivateKey(String privateKeyStr)  {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        try {
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePrivate(spec);
        }
        catch (Exception e) {
            return null;
        }
    }

    public PublicKey loadPublicKey(String type)  {
        byte[] keyBytes = Base64.getDecoder().decode(oemPublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePublic(spec);
        }
        catch (Exception e) {
            return null;
        }
    }

    public String generateToken(String subject, Map<String, Object> claims, long expirationTime, String privateKeyStr)  {
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

    public boolean validateToken(String token, String type, String permissionStr)  {
        PublicKey publicKey = loadPublicKey(type);
        try {
            Jws<Claims> claims = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);
            String permission = claims.getPayload().get("roles", String.class);
            if (claims.getPayload() == null) {
                return false;
            }
            if (permissionStr == null || permissionStr.equals("*")) {
                return true;
            }
            String[] permissionList = permissionStr.split(";");
            String[] permissionItems = permission.split(";");
            for (String item : permissionList) {
                if (!checkPermission(permissionItems, item)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.info("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    
    private boolean checkPermission(String[] permissionMap, String permission) {
        for (String item : permissionMap) {
            if (item.equals(permission)) {
                return true;
            }
        }
        return false;
    }
}