package com.chargev.emsp.controller;

import com.chargev.emsp.entity.authenticationentity.AuthSubject;
import com.chargev.emsp.service.cryptography.ECDSASignatureService;
import com.chargev.emsp.service.cryptography.ECKeyPairService;
import com.chargev.emsp.service.cryptography.JwtTokenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@Slf4j
@RequestMapping("{version}/auth")
@Validated
@RequiredArgsConstructor

public class AuthController {
    private final ECKeyPairService ecKeyPairService;
    private final ECDSASignatureService ecdsaSignatureService;
    private final JwtTokenService jwtTokenService;

    // TODO : 테스트가 끝나면 이 함수 필수로 삭제해야 함 
    @GetMapping("/insertInitialData")
    public String insertInitialData() {
        
        return "";
    }

    @PostMapping("/token")
    public String postMethodName(@RequestBody String entity) {
        // 토큰을 발행할 영역을 지정 후, 토큰을 발행한다.
        // 토큰 사용 영역 
        // 1. 관리자 페이지 영역 (권한 요청 필수)
        // 2. CPO, OCPI, OEM, PNC 각 영역별 발행 (권한 요청 옵션, 초기 인증 처리 방법)
        // 3. 외부 플랫폼의 사용자 처리 등.
        return entity;
    }

    @GetMapping("/generate")
    public Map<String, String> generateECKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPair keyPair = ecKeyPairService.generateECKeyPair();
        Map<String, String> response = new HashMap<>();
        response.put("publicKey", ecKeyPairService.getPublicKeyAsBase64(keyPair));
        response.put("privateKey", ecKeyPairService.getPrivateKeyAsBase64(keyPair));
        return response;
    }

    @GetMapping("/csr")
    public Map<String, String> generateCSR(@RequestParam String subject) throws Exception {
        KeyPair keyPair = ecKeyPairService.generateECKeyPair();
        String csr = ecKeyPairService.generateCSR(keyPair, subject);
        Map<String, String> response = new HashMap<>();
        response.put("csr", csr);
        response.put("publicKey", ecKeyPairService.getPublicKeyAsBase64(keyPair));
        response.put("privateKey", ecKeyPairService.getPrivateKeyAsBase64(keyPair));
        return response;
    }

    @PostMapping("/sign")
    public Map<String, String> signValue(@RequestParam String value, @RequestParam String privateKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = ecdsaSignatureService.loadPrivateKey(privateKeyStr);
        String signature = ecdsaSignatureService.signValue(value, privateKey);
        Map<String, String> response = new HashMap<>();
        response.put("signature", signature);
        return response;
    }

    @PostMapping("/verify")
    public Map<String, Boolean> verifySignature(@RequestParam String value, @RequestParam String signature, @RequestParam String publicKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        PublicKey publicKey = ecdsaSignatureService.loadPublicKey(publicKeyStr);
        boolean isValid = ecdsaSignatureService.verifySignature(value, signature, publicKey);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isValid", isValid);
        return response;
    }
    
    @PostMapping("/generateToken")
    public String GenerateToken(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    

    @GetMapping("/tokenGenTest")
    public Map<String, String> tokenGenTest() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "GSC");
        claims.put("sub", "MB");
        claims.put("reg", "N");
        claims.put("sn", "1234");
        claims.put("misc", "");

        String str = "";
        try {
            str= jwtTokenService.generateToken("ThisisToken", claims, -1, "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCB66CiE68HEop/bseevB+haiYi4nnUBtE9uTA01phbklA==");
        }
        catch (Exception e) {
        }

        Map<String, String> response = new HashMap<>();
        response.put("result", str);
        return response;
    }
    
    @GetMapping("/validateToken")
    public boolean validateToken() {
        boolean result = false;
        try {
            result = jwtTokenService.validateToken("eyJhbGciOiJFUzI1NiJ9.eyJzdWIiOiJUaGlzaXNUb2tlbiIsImlhdCI6MTcxNzE4MDcwMywiZXhwIjoxNzE3MTg0MzAzfQ.O33FY0auAvgE1vqwmkcdo-XP7A-yosLYpq9KHVsOsMLqQY-NbGw0NTKGstNx6w4E8-zKypU5GbTriswtey-NZA", "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENyw4zK2UedijFnajo5VZT7S/unHN8GW8BCtFJn6T4ErSIHYlCfasG2Znd5aMJA8n32xJsnb7b0gDSDtKPvmQFw==");
        }
        catch (Exception ex) {
        }
        return result;
        
    }
    

}
