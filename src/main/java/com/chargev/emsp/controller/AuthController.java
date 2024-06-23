package com.chargev.emsp.controller;

import com.chargev.emsp.entity.authenticationentity.AuthSubject;
import com.chargev.emsp.entity.authenticationentity.TokenIssueHistory;
import com.chargev.emsp.entity.authenticationentity.TokenRequest;
import com.chargev.emsp.model.dto.response.ApiResponseString;
import com.chargev.emsp.model.dto.response.OcpiResponseStatusCode;
import com.chargev.emsp.service.authentication.AuthService;
import com.chargev.emsp.service.cryptography.AESService;
import com.chargev.emsp.service.cryptography.ECDSASignatureService;
import com.chargev.emsp.service.cryptography.ECKeyPairService;
import com.chargev.emsp.service.cryptography.JwtTokenService;
import com.chargev.emsp.service.cryptography.SHAService;

import ch.qos.logback.core.subst.Token;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.shaded.io.opentelemetry.proto.trace.v1.Status.StatusCode;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



@RestController
@Slf4j
@RequestMapping("{version}/auth")
@Validated
@RequiredArgsConstructor

public class AuthController {
    private final ECKeyPairService ecKeyPairService;
    private final ECDSASignatureService ecdsaSignatureService;
    private final JwtTokenService jwtTokenService;
    private final AuthService authService;
    private final SHAService shaService;
    private final AESService aesService;
    private static final String SALT_STRING = "1577d9c941ad49008f4161ad02728dd2";

    // TODO : 테스트가 끝나면 이 함수 필수로 삭제해야 함 
    @GetMapping("/insertInitialData")
    public AuthSubject insertInitialData() {
        AuthSubject authSubject = new AuthSubject();
        authSubject.setSubjectId("e4f0b26742014c60a7fce2a9f7efdf25");
        authSubject.setSubjectName("Mercedes-Benz");
        authSubject.setSubjectPassword("1234");
        authSubject.setSubjectEmail("test@test.com");
        authSubject.setSubjectPhone("010-1234-5678");
        authSubject.setSubjectPassword(shaService.sha256Hash("2d92271fa4fc45f39643e8d26ec61af7", SALT_STRING));
        authSubject.setCreatedDate(new Date());
        authSubject.setUpdatedDate(new Date());
        authSubject.setCreatedUser("00000000000000000000000000000000");
        authSubject.setUpdatedUser("00000000000000000000000000000000");
        authSubject.setDeleted(0);
        authSubject.setSubjectRoles("OEM:READ;GROUP:0001");
        authSubject.setSubjectType("OEM");
        authSubject.setSubjectStatus(1); // 1: Active, 2: Inactive, 3: Locked
        authSubject.setSubjectDesc("Mercedes-Benz OEM Server User");
        return authService.saveAuthSubject(authSubject);
    }


    @PostMapping("/token")
    public ApiResponseString generateToken(@RequestBody TokenRequest entity) {
        // 토큰을 발행할 영역을 지정 후, 토큰을 발행한다.
        // 토큰 사용 영역 
        // 1. 관리자 페이지 영역 (권한 요청 필수)
        // 2. CPO, OCPI, OEM, PNC 각 영역별 발행 (권한 요청 옵션, 초기 인증 처리 방법)
        // 3. 외부 플랫폼의 사용자 처리 등.
        ApiResponseString response = new ApiResponseString();
        aesService.setKey("c9afaba19a1145d79f4f9b9525159f07d1daa4690e2f4d6ab117906473334f2b");
        String str =  aesService.encrypt("안뇽하세요");
        response.setData(str);
        response.setStatusCode(OcpiResponseStatusCode.SUCCESS);

        String original = aesService.decrypt(str);
        response.setStatusMessage(original);
        return response;
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
