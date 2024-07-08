package com.chargev.emsp.controller;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.entity.authenticationentity.AuthSubject;
import com.chargev.emsp.entity.authenticationentity.Permission;
import com.chargev.emsp.entity.authenticationentity.PermissionBase;
import com.chargev.emsp.entity.authenticationentity.PermissionGroup;
import com.chargev.emsp.entity.authenticationentity.TokenIssueHistory;
import com.chargev.emsp.entity.authenticationentity.TokenRequest;
import com.chargev.emsp.entity.keyentity.Keys;
import com.chargev.emsp.model.dto.response.ApiResponseString;
import com.chargev.emsp.model.dto.response.OcpiResponseStatusCode;
import com.chargev.emsp.service.authentication.AuthService;
import com.chargev.emsp.service.cryptography.AESService;
import com.chargev.emsp.service.cryptography.ECDSASignatureService;
import com.chargev.emsp.service.cryptography.ECKeyPairService;
import com.chargev.emsp.service.cryptography.JwtTokenService;
import com.chargev.emsp.service.cryptography.KeyService;
import com.chargev.emsp.service.cryptography.SHAService;
import com.chargev.emsp.service.formatter.DateTimeFormatterService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@Slf4j
@RequestMapping("{version}/auth")
@Validated
@RequiredArgsConstructor

public class AuthController {
    private final DateTimeFormatterService dateTimeFormatterService;

    private final ECKeyPairService ecKeyPairService;
    private final ECDSASignatureService ecdsaSignatureService;
    private final JwtTokenService jwtTokenService;
    private final AuthService authService;
    private final SHAService shaService;
    private final AESService aesService;
    private final KeyService keyService;
    private static final String SALT_STRING = "1577d9c941ad49008f4161ad02728dd2";

    private static final String CLIENT_ID_FOR_OEM = "082f72270a6640b7adff7595e9e7819b";
    private static final String CLIENT_SECRET_FOR_OEM = "25a92f0abea546e79cd77a4b00e68abc";

    private static final String AUTH_PASSWORD = "password";
    private static final String AUTH_CODE = "authorization_code";
    private static final String AUTH_IMPLICIT = "implicit";
    private static final String AUTH_CLIENT_CREDENTIALS = "client_credentials";
    private static final String AUTH_REFRESH_TOKEN = "refresh_token";


    // TODO : 테스트가 끝나면 이 함수 필수로 삭제해야 함 
    @GetMapping("/insertInitialData")
    public AuthSubject insertInitialData() {
        Keys key = new Keys();
        
        key.setKeyId("b44de2aa7b544645bdfa98a8ec5d5807");
        key.setKeyName("TESTKEY");
        key.setKeyType("EC");
        key.setKeyStatus(1);
        key.setKeyDesc("TEST KEY FOR OEM");
        key.setPrivateKey("MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCAJz8IesWrRR1+0RDkjnQrB99QXa+TMUK9c/m4Z8WCZNg==");
        key.setPublicKey("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEicyEdsjOCk0qwzLQW6iedldkkWfarNZ041F21pTq4hT/ak46TVrBiSxeqrerQx2mm9YtzLASoJ75zhUOn7WgMQ==");
        key.setCreatedDate(new Date());
        key.setUpdatedDate(new Date());
        key.setCreatedUser("00000000000000000000000000000000");
        key.setUpdatedUser("00000000000000000000000000000000");
        key.setClientId(CLIENT_ID_FOR_OEM);
        key.setClientSecret(CLIENT_SECRET_FOR_OEM);
        key.setDeleted(0);

        try {
        keyService.saveKeys(key);
        }
        catch (Exception e) {
            log.error("Error : {}", e.getMessage());
        }

        key.setKeyId("de920d8c5f52406a872d97ea26725068");
        key.setKeyName("TESTKEY_AES");
        key.setKeyType("AES");
        key.setKeyStatus(1);
        key.setKeyDesc("TEST KEY FOR OEM");
        key.setPrivateKey("c9afaba19a1145d79f4f9b9525159f07d1daa4690e2f4d6ab117906473334f2b");
        key.setPublicKey("");
        key.setCreatedDate(new Date());
        key.setUpdatedDate(new Date());
        key.setCreatedUser("00000000000000000000000000000000");
        key.setUpdatedUser("00000000000000000000000000000000");
        key.setClientId(CLIENT_ID_FOR_OEM);
        key.setClientSecret(CLIENT_SECRET_FOR_OEM);
        key.setDeleted(0);

        try {
            keyService.saveKeys(key);
        }
        catch (Exception e) {
            log.error("Error : {}", e.getMessage());
        }


        AuthSubject authSubject = new AuthSubject();
        authSubject.setSubjectId("e4f0b26742014c60a7fce2a9f7efdf25");
        authSubject.setSubjectName("Mercedes-Benz");
        authSubject.setSubjectEmail("test@test.com");
        authSubject.setSubjectPhone("010-1234-5678");
        authSubject.setSubjectPassword(shaService.sha256Hash("2d92271fa4fc45f39643e8d26ec61af7", SALT_STRING));
        authSubject.setCreatedDate(new Date());
        authSubject.setUpdatedDate(new Date());
        authSubject.setCreatedUser("00000000000000000000000000000000");
        authSubject.setUpdatedUser("00000000000000000000000000000000");
        authSubject.setDeleted(0);
        authSubject.setGroupId("OEM001");
        authSubject.setSubjectType("OEM");
        authSubject.setSubjectStatus(1); // 1: Active, 2: Inactive, 3: Locked
        authSubject.setSubjectDesc("Mercedes-Benz OEM Server User");
        authSubject.setTokenGenPermission(1); // 1:시간 자유, 기타는 범위 관련 처리함 
        authService.saveAuthSubject(authSubject);

        authSubject = new AuthSubject();
        authSubject.setSubjectId("102ab2f035d54d9c9760cbac5afce4aa");
        authSubject.setSubjectName("CPO Test");
        authSubject.setSubjectEmail("test@test.com");
        authSubject.setSubjectPhone("010-1234-5678");
        authSubject.setSubjectPassword(shaService.sha256Hash("81dbde083e46426b94453aa8d8803e5e", SALT_STRING));
        authSubject.setCreatedDate(new Date());
        authSubject.setUpdatedDate(new Date());
        authSubject.setCreatedUser("00000000000000000000000000000000");
        authSubject.setUpdatedUser("00000000000000000000000000000000");
        authSubject.setDeleted(0);
        authSubject.setGroupId("CPO001");
        authSubject.setSubjectType("CPO");
        authSubject.setSubjectStatus(1); // 1: Active, 2: Inactive, 3: Locked
        authSubject.setSubjectDesc("CPO");
        authSubject.setTokenGenPermission(1); // 1:시간 자유, 기타는 범위 관련 처리함 
        authService.saveAuthSubject(authSubject);

        authSubject = new AuthSubject();
        authSubject.setSubjectId("bc25f58dcb1a40db86fabda7d2f9c06b");
        authSubject.setSubjectName("GSChageV");
        authSubject.setSubjectEmail("test@test.com");
        authSubject.setSubjectPhone("010-1234-5678");
        authSubject.setSubjectPassword(shaService.sha256Hash("204d69e91ed04672bb18813f3a2cd933", SALT_STRING));
        authSubject.setCreatedDate(new Date());
        authSubject.setUpdatedDate(new Date());
        authSubject.setCreatedUser("00000000000000000000000000000000");
        authSubject.setUpdatedUser("00000000000000000000000000000000");
        authSubject.setDeleted(0);
        authSubject.setGroupId("PNC001");
        authSubject.setSubjectType("PNC");
        authSubject.setSubjectStatus(1); // 1: Active, 2: Inactive, 3: Locked
        authSubject.setSubjectDesc("GSChargeV PNC Server User");
        authSubject.setTokenGenPermission(1); // 1:시간 자유, 기타는 범위 관련 처리함 
        authService.saveAuthSubject(authSubject);

        authSubject = new AuthSubject();
        authSubject.setSubjectId("a1dca34e1f3b478884fafdffbf66ef48");
        authSubject.setSubjectName("OCPITestUser");
        authSubject.setSubjectEmail("test@test.com");
        authSubject.setSubjectPhone("010-1234-5678");
        authSubject.setSubjectPassword(shaService.sha256Hash("c0aeb5d2328d43ba87a24e9233d185b6", SALT_STRING));
        authSubject.setCreatedDate(new Date());
        authSubject.setUpdatedDate(new Date());
        authSubject.setCreatedUser("00000000000000000000000000000000");
        authSubject.setUpdatedUser("00000000000000000000000000000000");
        authSubject.setDeleted(0);
        authSubject.setGroupId("OCP001");
        authSubject.setSubjectType("OCP");
        authSubject.setSubjectStatus(1); // 1: Active, 2: Inactive, 3: Locked
        authSubject.setSubjectDesc("GSChargeV OCPI Server User");
        authSubject.setTokenGenPermission(1); // 1:시간 자유, 기타는 범위 관련 처리함 

        PermissionBase permissionBase = new PermissionBase();
        permissionBase.setPermissionId("4c4ab7f112bc4da0a69ffa5759c5a862");
        permissionBase.setPermissionName("READ");
        permissionBase.setPermissionScope("OEMAPI_GENERAL");
        permissionBase.setPermissionDesc("Read Permission");
        permissionBase.setCreatedDate(new Date());
        permissionBase.setUpdatedDate(new Date());
        permissionBase.setCreatedUser("00000000000000000000000000000000");
        permissionBase.setUpdatedUser("00000000000000000000000000000000");
        permissionBase.setDeleted(0);
        

        authService.savePermissionBase(permissionBase);

        permissionBase.setPermissionId("b154cb1370314264ad164cf982bd99f5");
        permissionBase.setPermissionName("WRITE");
        permissionBase.setPermissionScope("OEMAPI_GENERAL");
        permissionBase.setPermissionDesc("Read Permission");
        permissionBase.setCreatedDate(new Date());
        permissionBase.setUpdatedDate(new Date());
        permissionBase.setCreatedUser("00000000000000000000000000000000");
        permissionBase.setUpdatedUser("00000000000000000000000000000000");
        permissionBase.setDeleted(0);

        authService.savePermissionBase(permissionBase);

        permissionBase = new PermissionBase();
        permissionBase.setPermissionId("ad05ab75b4b845d1961facf19d595f55");
        permissionBase.setPermissionName("READ");
        permissionBase.setPermissionScope("PNCAPI_GENERAL");
        permissionBase.setPermissionDesc("Read Permission");
        permissionBase.setCreatedDate(new Date());
        permissionBase.setUpdatedDate(new Date());
        permissionBase.setCreatedUser("00000000000000000000000000000000");
        permissionBase.setUpdatedUser("00000000000000000000000000000000");
        permissionBase.setDeleted(0);

        authService.savePermissionBase(permissionBase);

        permissionBase.setPermissionId("c760b41adbc2499a8278724fd7e9f808");
        permissionBase.setPermissionName("WRITE");
        permissionBase.setPermissionScope("PNCAPI_GENERAL");
        permissionBase.setPermissionDesc("Read Permission");
        permissionBase.setCreatedDate(new Date());
        permissionBase.setUpdatedDate(new Date());
        permissionBase.setCreatedUser("00000000000000000000000000000000");
        permissionBase.setUpdatedUser("00000000000000000000000000000000");
        permissionBase.setDeleted(0);

        authService.savePermissionBase(permissionBase);

        permissionBase = new PermissionBase();
        permissionBase.setPermissionId("956a12f5165e465f96af3df3fd3164c8");
        permissionBase.setPermissionName("READ");
        permissionBase.setPermissionScope("OCPAPI_GENERAL");
        permissionBase.setPermissionDesc("Read Permission");
        permissionBase.setCreatedDate(new Date());
        permissionBase.setUpdatedDate(new Date());
        permissionBase.setCreatedUser("00000000000000000000000000000000");
        permissionBase.setUpdatedUser("00000000000000000000000000000000");
        permissionBase.setDeleted(0);

        authService.savePermissionBase(permissionBase);

        permissionBase.setPermissionId("9d741d9b0d0246d6964f1b8fc869a280");
        permissionBase.setPermissionName("WRITE");
        permissionBase.setPermissionScope("OCPAPI_GENERAL");
        permissionBase.setPermissionDesc("Read Permission");
        permissionBase.setCreatedDate(new Date());
        permissionBase.setUpdatedDate(new Date());
        permissionBase.setCreatedUser("00000000000000000000000000000000");
        permissionBase.setUpdatedUser("00000000000000000000000000000000");
        permissionBase.setDeleted(0);

        authService.savePermissionBase(permissionBase);


        PermissionGroup permissionGroup = new PermissionGroup();
        permissionGroup.setGroupId("OEM001");
        permissionGroup.setGroupName("OEM Group");
        permissionGroup.setGroupDescription("OEM Group");
        permissionGroup.setCreatedDate(new Date());
        permissionGroup.setUpdatedDate(new Date());
        permissionGroup.setCreatedUser("00000000000000000000000000000000");
        permissionGroup.setUpdatedUser("00000000000000000000000000000000");
        permissionGroup.setDeleted(0);

        authService.savePermissionGroup(permissionGroup);

        permissionGroup = new PermissionGroup();
        permissionGroup.setGroupId("PNC001");
        permissionGroup.setGroupName("PNC Group");
        permissionGroup.setGroupDescription("PNC Group");
        permissionGroup.setCreatedDate(new Date());
        permissionGroup.setUpdatedDate(new Date());
        permissionGroup.setCreatedUser("00000000000000000000000000000000");
        permissionGroup.setUpdatedUser("00000000000000000000000000000000");
        permissionGroup.setDeleted(0);

        authService.savePermissionGroup(permissionGroup);

        permissionGroup = new PermissionGroup();
        permissionGroup.setGroupId("OCP001");
        permissionGroup.setGroupName("OCPI Group");
        permissionGroup.setGroupDescription("OCPI Group");
        permissionGroup.setCreatedDate(new Date());
        permissionGroup.setUpdatedDate(new Date());
        permissionGroup.setCreatedUser("00000000000000000000000000000000");
        permissionGroup.setUpdatedUser("00000000000000000000000000000000");
        permissionGroup.setDeleted(0);

        authService.savePermissionGroup(permissionGroup);

        Permission permission = new Permission();
        permission.setPermissionId("4c4ab7f112bc4da0a69ffa5759c5a862");
        permission.setGroupId("OEM001");
        permission.setCreatedDate(new Date());
        permission.setUpdatedDate(new Date());
        permission.setCreatedUser("00000000000000000000000000000000");
        permission.setUpdatedUser("00000000000000000000000000000000");
        permission.setDeleted(0);

        authService.savePermission(permission);

        permission = new Permission();
        permission.setPermissionId("b154cb1370314264ad164cf982bd99f5");
        permission.setGroupId("OEM001");
        permission.setCreatedDate(new Date());
        permission.setUpdatedDate(new Date());
        permission.setCreatedUser("00000000000000000000000000000000");
        permission.setUpdatedUser("00000000000000000000000000000000");
        permission.setDeleted(0);

        authService.savePermission(permission);

        permission = new Permission();
        permission.setPermissionId("c760b41adbc2499a8278724fd7e9f808");
        permission.setGroupId("PNC001");
        permission.setCreatedDate(new Date());
        permission.setUpdatedDate(new Date());
        permission.setCreatedUser("00000000000000000000000000000000");
        permission.setUpdatedUser("00000000000000000000000000000000");
        permission.setDeleted(0);

        authService.savePermission(permission);

        permission = new Permission();
        permission.setPermissionId("ad05ab75b4b845d1961facf19d595f55");
        permission.setGroupId("PNC001");
        permission.setCreatedDate(new Date());
        permission.setUpdatedDate(new Date());
        permission.setCreatedUser("00000000000000000000000000000000");
        permission.setUpdatedUser("00000000000000000000000000000000");
        permission.setDeleted(0);

        authService.savePermission(permission);

        permission = new Permission();
        permission.setPermissionId("956a12f5165e465f96af3df3fd3164c8");
        permission.setGroupId("OCP001");
        permission.setCreatedDate(new Date());
        permission.setUpdatedDate(new Date());
        permission.setCreatedUser("00000000000000000000000000000000");
        permission.setUpdatedUser("00000000000000000000000000000000");
        permission.setDeleted(0);

        authService.savePermission(permission);

        permission = new Permission();
        permission.setPermissionId("9d741d9b0d0246d6964f1b8fc869a280");
        permission.setGroupId("OCP001");
        permission.setCreatedDate(new Date());
        permission.setUpdatedDate(new Date());
        permission.setCreatedUser("00000000000000000000000000000000");
        permission.setUpdatedUser("00000000000000000000000000000000");
        permission.setDeleted(0);

        authService.savePermission(permission);
        

        return authService.saveAuthSubject(authSubject);

    }


    @PostMapping("/token")
    public ApiResponseString generateToken(@RequestBody TokenRequest entity, HttpServletRequest request) {
        // 토큰을 발행할 영역을 지정 후, 토큰을 발행한다.
        // 토큰 사용 영역 
        // 1. 관리자 페이지 영역 (권한 요청 필수)
        // 2. CPO, OCPI, OEM, PNC 각 영역별 발행 (권한 요청 옵션, 초기 인증 처리 방법)
        // 3. 외부 플랫폼의 사용자 처리 등.

        String ip = request.getHeader("X-Forwarded-For");
    
        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        ApiResponseString response = new ApiResponseString();
        response.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        // 어떤 영역에서 사용할 것인지 프라이빗 키를 불러온다. 
        if(entity == null) {
            response.setStatusCode(OcpiResponseStatusCode.INVALID_PARAMETER);
            response.setStatusMessage(OcpiResponseStatusCode.INVALID_PARAMETER.toString());
            return response;
        }

        if(entity.getGrantType() == null) {
            response.setStatusCode(OcpiResponseStatusCode.INVALID_PARAMETER);
            response.setStatusMessage(OcpiResponseStatusCode.INVALID_PARAMETER.toString());
            return response;
        }

        if(!entity.getGrantType().equals(AUTH_PASSWORD)) {
            response.setStatusCode(OcpiResponseStatusCode.INVALID_PARAMETER);
            response.setStatusMessage(OcpiResponseStatusCode.INVALID_PARAMETER.toString());
            return response;
        }

        AuthSubject authSubject = authService.loginWithPassword(entity.getUsername(), entity.getPassword());
        if(authSubject == null) {
            response.setStatusCode(OcpiResponseStatusCode.INVALID_PARAMETER);
            response.setStatusMessage(OcpiResponseStatusCode.INVALID_PARAMETER.toString());
            return response;
        }

        Keys asymKeys = null;
        // 현재는 비동기 키를 전부 동일하게 처리함 
        if(authSubject.getSubjectType().equals("OEM")) {
            asymKeys = keyService.getKeys(CLIENT_ID_FOR_OEM, CLIENT_SECRET_FOR_OEM, "EC");
        }
        else if(authSubject.getSubjectType().equals("PNC")) {
            asymKeys = keyService.getKeys(CLIENT_ID_FOR_OEM, CLIENT_SECRET_FOR_OEM, "EC");
        }
        else if(authSubject.getSubjectType().equals("OCP")) {
            asymKeys = keyService.getKeys(CLIENT_ID_FOR_OEM, CLIENT_SECRET_FOR_OEM, "EC");
        }        
        else if(authSubject.getSubjectType().equals("CPO")) {
            asymKeys = keyService.getKeys(CLIENT_ID_FOR_OEM, CLIENT_SECRET_FOR_OEM, "EC");
        }        
        else {
            response.setStatusCode(OcpiResponseStatusCode.INVALID_PARAMETER);
            response.setStatusMessage(OcpiResponseStatusCode.INVALID_PARAMETER.toString());
            return response;
        }
        if(asymKeys == null) {
            response.setStatusCode(OcpiResponseStatusCode.SERVER_ERROR);
            response.setStatusMessage(OcpiResponseStatusCode.SERVER_ERROR.toString());
            return response;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "GSChargeV");
        claims.put("sub", authSubject.getSubjectId());
        claims.put("reg", "N");

        // 토큰 발급 히스토리 
        TokenIssueHistory history = new TokenIssueHistory();
        history.setSubjectId(authSubject.getSubjectId());
        history.setIssueDate(new Date());
        history.setIssueSerial(0);
        history.setIpAddress(ip);
        history.setToken("");
        history.setTokenType(1);
        history.setCreatedUser("00000000000000000000000000000000");
        history.setUpdatedDate(new Date());
        history.setIssueStatus(0);

        TokenIssueHistory his = authService.saveTokenIssueHistory(history);
        if(his.getIssueSerial() == 0) {
            response.setStatusCode(OcpiResponseStatusCode.SERVER_ERROR);
            response.setStatusMessage(OcpiResponseStatusCode.SERVER_ERROR.toString());
            return response;
        }

        List<PermissionBase> permissions = authService.getPermissionByGroup(authSubject.getGroupId());

        if(permissions == null) {
            response.setStatusCode(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION);
            response.setStatusMessage(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION.toString());
            return response;
        }

        if(permissions.isEmpty()) {
            response.setStatusCode(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION);
            response.setStatusMessage(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION.toString());
            return response;
        }

        claims.put("sn", his.getIssueSerial());

        StringBuilder roles = new StringBuilder();
        for(PermissionBase permission : permissions) {
            roles.append(permission.getPermissionName()).append(":").append(permission.getPermissionScope()).append(";");
        }

        claims.put("roles", roles.toString());

        Long expirationTime = 3600000L * 24; // 24 Hr

        if(authSubject.getSubjectType().equals("PNC") || authSubject.getSubjectType().equals("CPO")) {
            expirationTime = -1L; // 1 Hr
        }

        if((authSubject.getTokenGenPermission() & 1) != 0 && entity.getExpiration() != null) {
            try {
                expirationTime = Long.parseLong(entity.getExpiration());
            }
            catch (Exception ex) {
            }
        }
        
   
        String jwtToken = jwtTokenService.generateToken(authSubject.getSubjectId(), claims, expirationTime, asymKeys.getPrivateKey());
        if(jwtToken == null) {

            his.setToken(jwtToken);
            his.setIssueStatus(1);
            his.setIssueDate(new Date());
            authService.saveTokenIssueHistory(his);
            
            response.setStatusCode(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION);
            response.setStatusMessage(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION.toString());
            return response;
        }

        response.setData(jwtToken);
        response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        return response;
    }
    

    @GetMapping("/generate")
    public Map<String, String> generateECKeyPair() {
        KeyPair keyPair = ecKeyPairService.generateECKeyPair();
        Map<String, String> response = new HashMap<>();
        response.put("publicKey", ecKeyPairService.getPublicKeyAsBase64(keyPair));
        response.put("privateKey", ecKeyPairService.getPrivateKeyAsBase64(keyPair));
        return response;
    }

    @GetMapping("/csr/{subject}")
    public Map<String, String> generateCSR(@PathVariable String subject)  {
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
    

    // @GetMapping("/tokenGenTest")
    // public Map<String, String> tokenGenTest() {
    //     Map<String, Object> claims = new HashMap<>();
    //     claims.put("iss", "GSC");
    //     claims.put("sub", "MB");
    //     claims.put("reg", "N");
    //     claims.put("sn", "1234");
    //     claims.put("misc", "");

    //     String str = "";
    //     try {
    //         str= jwtTokenService.generateToken("ThisisToken", claims, -1, "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCB66CiE68HEop/bseevB+haiYi4nnUBtE9uTA01phbklA==");
    //     }
    //     catch (Exception e) {
    //     }

    //     Map<String, String> response = new HashMap<>();
    //     response.put("result", str);
    //     return response;
    // }
    
    @GetMapping("/validateToken")
    public boolean validateToken() {
        boolean result = false;
        try {
            result = jwtTokenService.validateToken("eyJhbGciOiJFUzI1NiJ9.eyJzdWIiOiJlNGYwYjI2NzQyMDE0YzYwYTdmY2UyYTlmN2VmZGYyNSIsInJlZyI6Ik4iLCJyb2xlcyI6IlJFQUQ6T0VNQVBJX0dFTkVSQUw7V1JJVEU6T0VNQVBJX0dFTkVSQUw7IiwiaXNzIjoiR1NDaGFyZ2VWIiwic24iOjQsImlhdCI6MTcxOTE0NDk0NCwiZXhwIjoxNzE5MTQ4NTQ0fQ.r5hjWh-qkgUvYgQAaBnABEoO866nTD5qKNpNZZ4x5FoklzIEnCu8xYlisw_YLFygu0xBnDuOEyaiAahzyNXFaw", "OEM", "READ:OEMAPI_GENERAL;WRITE:OEMAPI_GENERAL");
        }
        catch (Exception ex) {
        }
        return result;
        
    }
    

}
