package com.chargev.emsp.service.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.chargev.emsp.model.dto.pnc.*;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.List;

@Service
public class KpipApiService {

    // base url, spid, spkey (custom.properties에서 설정한 값을 불러온다.)
    @Value("${kpip.base.url}")
    private String baseUrl;

    @Value("${kpip.spid}")
    private String spid;

    @Value("${kpip.spkey}")
    private String spkey;

    private final WebClient webClient;

    public KpipApiService(@Value("${kpip.base.url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl(baseUrl)
                .build();
    }

    // 공통 헤더 (KPIP)
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("spid", spid);
        headers.add("spkey", spkey);
        headers.add("trace-id", UUID.randomUUID().toString());
        headers.add("timestamp", generateTimestamp());
        return headers;
    }

    // Timestamp 생성
    private String generateTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        ZonedDateTime utcTime = ZonedDateTime.now(ZoneId.of("UTC"));
        String timestamp = utcTime.format(formatter);
        return timestamp;
    }

    // 1-1. EVSE/CSMS Issue Certificate
    public Map<String, Object> issueCert(KpipReqBodyIssueCert request) {
        String url = "/cert/issuance";
        HttpHeaders headers = createHeaders();

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> response = responseMono.block();

        return response;
    }

    // 1-2. EVSE/CSMS Revoke Certificate
    public Map<String, Object> revokeCert(KpipReqBodyRevokeCert request) {
        String url = "/cert/revocation";
        HttpHeaders headers = createHeaders();

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> response = responseMono.block();

        return response;
    }

    // 1-3. Verify OCSP
    public String verifyOcsp(KpipReqBodyVerifyOcsp request) {
        String url = "/cert/ocsp-verification";
        HttpHeaders headers = createHeaders();

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> response = responseMono.block();

        String resultCode = (String) response.get("resultCode");
        String resultMsg = (String) response.get("resultMsg");
        String status = (String) response.get("status");

        if (resultCode.equals("OK")) {
            // 이후 로직 처리
        } else {
            // 처리 실패
        }

        switch (status) {
            case "Good":
                // 검증 성공
                break;
            case "Revoked":
                // 검증 되었으나 만료된 인증서임
                break;
            case "Unkhons":
                // 검증 실패. 알 수 없는 인증서임.
                break;
            default:
                // 기타 경우.
                break;
        }

        return resultMsg;
    }

    // 1-4. Download CRL
    public String downloadCrl(KpipReqBodyDownloadCrl request) {
        String url = "/cert/download-crl";
        HttpHeaders headers = createHeaders();

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> response = responseMono.block();

        String resultCode = (String) response.get("resultCode");
        String resultMsg = (String) response.get("resultMsg");

        // Base 64 Encoded X.509 format of CRL
        String crl = (String) response.get("crl");

        if (resultCode.equals("OK")) {
            // 이후 로직 처리
        } else {
            // 처리 실패
        }

        return resultMsg;
    }

    // 1-5. OCSP Response message
    // OCSP 검증은 1-3으로 이미 수행하는데, 추가로 OCSP 응답 메시지를 받는 기능이 있어서 구현해놓음
    // 도식 상으로 EVSE에서 직접 OCSP를 검증하는 작업을 해야 할 때 사용하는 것으로 보이나
    // 현재 EVSE의 모든 요청은 MSP를 통해 다시 eMSP로 전달되므로 OCSP를 검증하는 것도 1-3만 가지고 수행이 가능할 것으로 예상됨
    public String getOcspResponseMessage(KpipReqBodyGetOcspMessage request) {
        String url = "/cert/ocsp-response-msg";
        HttpHeaders headers = createHeaders();

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> response = responseMono.block();

        String resultCode = (String) response.get("resultCode");
        String resultMsg = (String) response.get("resultMsg");
        String status = (String) response.get("status");

        // EVSE에서 처리가능한 Base 64 Encoded OCSP Response Message
        String ocspRes = (String) response.get("ocspRes");

        if (resultCode.equals("OK")) {
            // 이후 로직 처리
        } else {
            // 처리 실패
        }

        switch (status) {
            case "Good":
                // 검증 성공
                break;
            case "Revoked":
                // 검증 되었으나 만료된 인증서임
                break;
            case "Unkhons":
                // 검증 실패. 알 수 없는 인증서임.
                break;
            default:
                // 기타 경우.
                break;
        }

        return resultMsg;
    }

    // 2-1. Issue Contract Certificate
    public Map<String, Object> issueContractCert(KpipReqBodyIssueContractCert request) {
        String url = "/contract/issue";
        HttpHeaders headers = createHeaders();

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> response = responseMono.block();

        return response;
    }

    // 2-2. Revoke Contract Certificate
    public String revokeContractCert(KpipReqBodyEmaid request) {
        String url = "/contract/revocation";
        HttpHeaders headers = createHeaders();

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> response = responseMono.block();

        String resultCode = (String) response.get("resultCode");
        String resultMsg = (String) response.get("resultMsg");

        if (resultCode.equals("OK")) {
            // KEPCO쪽 계약인증서 삭제 성공
            // 이후 로직 처리
        } else {
            // KEPCO쪽 계약인증서 삭제 실패
            // resultMsg에 따른 분기 필요할 것으로 보임
        }

        return resultMsg;
    }

    // 2-3. Verify Contract Certificate
    public String verifyContractCert(KpipReqBodyVerifyContractCert request) {
        String url = "/pnc-auth/contract-cert-verification";
        HttpHeaders headers = createHeaders();

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> response = responseMono.block();

        String resultCode = (String) response.get("resultCode");
        String resultMsg = (String) response.get("resultMsg");
        String status = (String) response.get("status");

        if (resultCode.equals("OK")) {
            // KEPCO쪽 계약인증서 검증 성공
            // 이후 로직 처리
        } else {
            // KEPCO쪽 계약인증서 검증 실패
            // resultMsg에 따른 분기 필요할 것으로 보임
        }

        switch (status) {
            case "Good":
                // 검증 성공
                break;
            case "Revoked":
                // 검증 되었으나 만료된 인증서임
                break;
            case "Unkhons":
                // 검증 실패. 알 수 없는 인증서임.
                break;
            default:
                // 기타 경우.
                break;
        }

        return resultMsg;
    }

    // 2-4. Push Whitelist
    public String pushWhitelist(List<KpipReqBodyPushWhitelistItem> request) {
        String url = "/pnc-auth/white-list";
        HttpHeaders headers = createHeaders();

        Mono<Map<String, Object>> responseMono = webClient.put()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> response = responseMono.block();

        String resultCode = (String) response.get("resultCode");
        String resultMsg = (String) response.get("resultMsg");

        if (resultCode.equals("OK")) {
            // List 전체 처리 성공
            // 이후 로직 처리
        } else if (resultCode.equals("OK")) {
            // List 중 일부만 처리 성공
            // emadList 내부의 각각의 resultMsg 확인 필요
        } else {
            // 처리 실패
        }

        if (response.containsKey("emaidList")) {
            List<KpipResBodyPushWhitelistItem> emaidList = (List<KpipResBodyPushWhitelistItem>) response.get("emaidList");
            // 처리 결과가 담긴 List
        }

        return resultMsg;
    }

    // 2-5. Authorize PnC account
    public String authorizePnc(KpipReqBodyEmaid request) {
        String url = "/pnc-auth/authorize-account";
        HttpHeaders headers = createHeaders();

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> response = responseMono.block();

        String resultCode = (String) response.get("resultCode");
        String resultMsg = (String) response.get("resultMsg");

        if (resultCode.equals("OK")) {
            // 이후 로직 처리
        } else {
            // 처리 실패
        }

        return resultMsg;
    }
}
