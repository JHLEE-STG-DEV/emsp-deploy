package com.chargev.emsp.service.cryptography;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.authenticationentity.TokenRequest;

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

    private static final String AUTH_PASSWORD = "password";
    private static final String AUTH_CODE = "authorization_code";
    private static final String AUTH_IMPLICIT = "implicit";
    private static final String AUTH_CLIENT_CREDENTIALS = "client_credentials";
    private static final String AUTH_REFRESH_TOKEN = "refresh_token";


    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
    }

    private boolean canIssueTokeByThatMethods(TokenRequest request) {
        // 토큰 발급 가능한 방법을 체크한다.
        // TODO 실제 구현 필요
        return true; 
    }

    private boolean loginWithCrediential(String clientId, String clientSecret) {
        // 클라이언트 인증을 처리한다.
        // TODO 실제 구현 필요
        return true;
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

    public String generateToken(TokenRequest request, long expirationTime, String privateKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // 토큰 생성 실패 오류는 명백한 오류 메시지를 주면 안 된다. (보안 문제) 해커 입장에서는 다양하게 수집된 토큰이 어떤 형태로 공격이 가능한지 파악할 수 있기 때문이다.
        PrivateKey privateKey = loadPrivateKey(privateKeyStr);

        long realExpirationTime = System.currentTimeMillis() + expirationTime;
        if(expirationTime == -1) {
            realExpirationTime = -1;
        }

        String grantType = request.getGrantType();
        if(grantType == null) {
            grantType = AUTH_IMPLICIT;
        }
        else {
            grantType = grantType.toLowerCase();
        
            if(!(grantType.equals(AUTH_CLIENT_CREDENTIALS) || grantType.equals(AUTH_PASSWORD) || grantType.equals(AUTH_REFRESH_TOKEN) || grantType.equals(AUTH_CODE))) {                 
                return null; // 인증 방법 오류 
            }
        }

        if(!canIssueTokeByThatMethods(request)) {
            return null; // 발행 방법 오류 
        }

        if(grantType.equals(AUTH_IMPLICIT)) {
            // 묵시적 토큰 발행 
            // 주어진 데이터를 기반으로 서명을 처리한다. 
        }
        else if(grantType.equals(AUTH_CLIENT_CREDENTIALS)) {
            // 클라이언트 인증 토큰 발행 
            if(!loginWithCrediential(request.getClientId(), request.getClientSecret())) {
                return null;
            }
        }
        else if(grantType.equals(AUTH_PASSWORD)) {
            // 사용자 인증 토큰 발행 
            // 사용자가 ID 비번 방식으로 인증을 처리하도록 한다
        }
        else if(grantType.equals(AUTH_REFRESH_TOKEN)) {
            // 토큰 갱신 토큰 발행 
            // 해당 토큰이 갱신 가능한 권한을 갖고 있는지 해석 후 확인한다. 
        }
        else if(grantType.equals(AUTH_CODE)) {
            // 코드 인증 토큰 발행 
        
        }

        return Jwts.builder()
        .subject(request.getClientId())
        //.claims(claims)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(realExpirationTime == -1 ? null : new Date(realExpirationTime))
        .signWith(privateKey)
        .compact();
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