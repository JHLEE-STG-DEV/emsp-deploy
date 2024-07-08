package com.chargev.emsp.service.http;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.chargev.emsp.model.dto.pnc.KpipReqBodyDownloadCrl;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyEmaid;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyGetOcspMessage;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyIssueContractCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyPushWhitelistItem;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyRevokeCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyVerifyContractCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyVerifyOcsp;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.log.CheckpointKind;
import com.chargev.emsp.service.log.CheckpointReference;
import com.chargev.emsp.service.log.KpipLogService;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Service
public class KpipApiService {

    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final KpipLogService kpipLogService;
    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");

    // base url, spid, spkey (custom.properties에서 설정한 값을 불러온다.)
    @Value("${kpip.base.url}")
    private String baseUrl;

    @Value("${kpip.spid}")
    private String spid;

    @Value("${kpip.spkey}")
    private String spkey;



    public KpipApiService(@Value("${kpip.base.url}") String baseUrl, KpipLogService kpipLogService, ObjectMapper objectMapper) {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl(baseUrl)
                .build();
        this.kpipLogService = kpipLogService;
                this.objectMapper = objectMapper;
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
    public ServiceResult<Map<String, Object>> issueCert(KpipReqBodyIssueCert request, String trackId) {
        
        ServiceResult<Map<String, Object>> serviceResult = new ServiceResult<>();

        String url = "/cert/issuance";
        String bodyJson = null;  try {
            bodyJson = objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            apiLogger.error("Kpip Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {  
            apiLogger.info("TAG: KPIP_START, Request URL: {}, Track ID: {}, Body: {}", url, trackId, bodyJson);
        }

        String kpipLogId = kpipLogService.kpipLogStart(url, request, trackId);
        CheckpointReference checkpoint = new CheckpointReference(kpipLogId);
        serviceResult.getCheckpoints().add(checkpoint);

        HttpHeaders headers = createHeaders();
        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                // .onStatus(HttpStatusCode::is4xxClientError, handleWebclientError(kpipLogId))
                // .onStatus(HttpStatusCode::is5xxServerError, handleWebclientError(kpipLogId))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });
        try {
            Map<String, Object> response = responseMono.block();

            if (response != null) {
                kpipLogService.kpipLogFinish(kpipLogId, "200", response);
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_SUCCESS);
                serviceResult.succeed(response);
                if (apiLogger.isInfoEnabled()) {
                    String responseJson = null;  
                    try {
                        responseJson = objectMapper.writeValueAsString(response);
                    } catch (Exception ex) {
                        apiLogger.error("Kpip Body 직렬화 실패.");
                    }
                    apiLogger.info("TAG: KPIP_END, Request URL: {}, Track ID: {}, Body: {}", url, trackId, responseJson);
                }
            } else {
                kpipLogService.kpipLogFail(kpipLogId, String.valueOf(400), "No Response");
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                serviceResult.fail(400, "No Response"); // 실제로 받은 에러코드와 body, 혹은 적당한 에러메세지를 받아야 함.
                if (apiLogger.isWarnEnabled()) {
                    apiLogger.warn("TAG: KPIP_END, Request URL: {}, Track ID: {}, MESSAGE: {}", url, trackId, "No Response");
                }
            }
        } catch (WebClientResponseException e) {
            // 4xx 및 5xx 오류를 여기서 처리
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            serviceResult.fail(statusCode, responseBody);
            kpipLogService.kpipLogFail(kpipLogId, String.valueOf(statusCode), responseBody);
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, statusCode);
            }
        } catch (Exception e) {
            // 기타 예외 처리
            serviceResult.fail(500, "Unknwon Error");
            kpipLogService.kpipLogFail(kpipLogId, "500", "Unknwon Error");
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, "UNKHOWN");
            }
        }
        return serviceResult;
    }

    // 1-2. EVSE/CSMS Revoke Certificate
    public ServiceResult<Map<String, Object>> revokeCert(KpipReqBodyRevokeCert request, String trackId) {
        ServiceResult<Map<String, Object>> serviceResult = new ServiceResult<>();

        String url = "/cert/revocation";
        String bodyJson = null;  try {
            bodyJson = objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            apiLogger.error("Kpip Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {  
            apiLogger.info("TAG: KPIP_START, Request URL: {}, Track ID: {}, Body: {}", url, trackId, bodyJson);
        }

        String kpipLogId = kpipLogService.kpipLogStart(url, request, trackId);
        CheckpointReference checkpoint = new CheckpointReference(kpipLogId);
        serviceResult.getCheckpoints().add(checkpoint);

        HttpHeaders headers = createHeaders();

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        try {
            Map<String, Object> response = responseMono.block();

            if (response != null) {
                kpipLogService.kpipLogFinish(kpipLogId, "200", response);
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_SUCCESS);
                serviceResult.succeed(response);
                if (apiLogger.isInfoEnabled()) {
                    
                    String responseJson = null;  
                    try {
                        responseJson = objectMapper.writeValueAsString(response);
                    } catch (Exception ex) {
                        apiLogger.error("Kpip Body 직렬화 실패.");
                    }
                    apiLogger.info("TAG: KPIP_END, Request URL: {}, Track ID: {}, Body: {}", url, trackId, responseJson);
                }
            } else {
                kpipLogService.kpipLogFail(kpipLogId, String.valueOf(400), "No Response");
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                serviceResult.fail(400, "No Response"); // 실제로 받은 에러코드와 body, 혹은 적당한 에러메세지를 받아야 함.
                if (apiLogger.isWarnEnabled()) {
                    apiLogger.warn("TAG: KPIP_END, Request URL: {}, Track ID: {}, MESSAGE: {}", url, trackId, "No Response");
                }
            }
        } catch (WebClientResponseException e) {
            // 4xx 및 5xx 오류를 여기서 처리
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            serviceResult.fail(statusCode, responseBody);
            kpipLogService.kpipLogFail(kpipLogId, String.valueOf(statusCode), responseBody);
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, statusCode);
            }
        } catch (Exception e) {
            // 기타 예외 처리
            serviceResult.fail(500, "Unknwon Error");
            kpipLogService.kpipLogFail(kpipLogId, "500", "Unknwon Error");
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, "UNKHOWN");
            }
        }
        return serviceResult;

    }

    // 1-3. Verify OCSP
    public ServiceResult<String> verifyOcsp(KpipReqBodyVerifyOcsp request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();

        String url = "/cert/ocsp-verification";
        HttpHeaders headers = createHeaders();

        String bodyJson = null;  try {
            bodyJson = objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            apiLogger.error("Kpip Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {  
            apiLogger.info("TAG: KPIP_START, Request URL: {}, Track ID: {}, Body: {}", url, trackId, bodyJson);
        }
        String kpipLogId = kpipLogService.kpipLogStart(url, request, trackId);
        CheckpointReference checkpoint = new CheckpointReference(kpipLogId);
        serviceResult.getCheckpoints().add(checkpoint);

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        try {

            Map<String, Object> response = responseMono.block();

            if (response != null) {

                String resultCode = (String) response.get("resultCode");
                String resultMsg = (String) response.get("resultMsg");
                String status = (String) response.get("status");

                if (resultCode.equals("OK")) {
                    // 이후 로직 처리
                    serviceResult.succeed(resultMsg);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_SUCCESS);
                } else {
                    // 처리 실패
                    serviceResult.fail(500, resultMsg);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
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
                kpipLogService.kpipLogFinish(kpipLogId, "200", response);
                if (apiLogger.isInfoEnabled()) {
                    
                    String responseJson = null;  
                    try {
                        responseJson = objectMapper.writeValueAsString(response);
                    } catch (Exception ex) {
                        apiLogger.error("Kpip Body 직렬화 실패.");
                    }
                    apiLogger.info("TAG: KPIP_END, Request URL: {}, Track ID: {}, Body: {}", url, trackId, responseJson);
                }

            } else {
                kpipLogService.kpipLogFail(kpipLogId, String.valueOf(400), "No Response");
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                serviceResult.fail(400, "No Response"); // 실제로 받은 에러코드와 body, 혹은 적당한 에러메세지를 받아야 함.
                if (apiLogger.isWarnEnabled()) {
                    apiLogger.warn("TAG: KPIP_END, Request URL: {}, Track ID: {}, MESSAGE: {}", url, trackId, "No Response");
                }
            }
        } catch (WebClientResponseException e) {
            // 4xx 및 5xx 오류를 여기서 처리
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            serviceResult.fail(statusCode, responseBody);
            kpipLogService.kpipLogFail(kpipLogId, String.valueOf(statusCode), responseBody);
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, statusCode);
            }
        } catch (Exception e) {
            // 기타 예외 처리
            serviceResult.fail(500, "Unknwon Error");
            kpipLogService.kpipLogFail(kpipLogId, "500", "Unknwon Error");
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, "UNKHOWN");
            }
        }

        return serviceResult;
    }

    // 1-4. Download CRL
    public ServiceResult<String> downloadCrl(KpipReqBodyDownloadCrl request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();

        String url = "/cert/download-crl";
        HttpHeaders headers = createHeaders();

        String bodyJson = null;  try {
            bodyJson = objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            apiLogger.error("Kpip Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {  
            apiLogger.info("TAG: KPIP_START, Request URL: {}, Track ID: {}, Body: {}", url, trackId, bodyJson);
        }
        String kpipLogId = kpipLogService.kpipLogStart(url, request, trackId);
        CheckpointReference checkpoint = new CheckpointReference(kpipLogId);
        serviceResult.getCheckpoints().add(checkpoint);

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        try {
            Map<String, Object> response = responseMono.block();

            if (response != null) {

                String resultCode = (String) response.get("resultCode");
                String resultMsg = (String) response.get("resultMsg");
                // Base 64 Encoded X.509 format of CRL
                String crl = (String) response.get("crl");
                if (resultCode.equals("OK")) {
                    // KEPCO쪽 계약인증서 삭제 성공
                    // 이후 로직 처리

                    serviceResult.succeed(resultMsg);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_SUCCESS);
                } else {
                    // KEPCO쪽 계약인증서 삭제 실패
                    // resultMsg에 따른 분기 필요할 것으로 보임
                    serviceResult.fail(500, resultMsg);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                }
                kpipLogService.kpipLogFinish(kpipLogId, "200", response);
                if (apiLogger.isInfoEnabled()) {
                    String responseJson = null;  
                    try {
                        responseJson = objectMapper.writeValueAsString(response);
                    } catch (Exception ex) {
                        apiLogger.error("Kpip Body 직렬화 실패.");
                    }
                    apiLogger.info("TAG: KPIP_END, Request URL: {}, Track ID: {}, Body: {}", url, trackId, responseJson);
                }
            } else {
                kpipLogService.kpipLogFail(kpipLogId, String.valueOf(400), "No Response");
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                serviceResult.fail(400, "No Response"); // 실제로 받은 에러코드와 body, 혹은 적당한 에러메세지를 받아야 함.
                if (apiLogger.isWarnEnabled()) {
                    apiLogger.warn("TAG: KPIP_END, Request URL: {}, Track ID: {}, MESSAGE: {}", url, trackId, "No Response");
                }
            }
        } catch (WebClientResponseException e) {
            // 4xx 및 5xx 오류를 여기서 처리
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            serviceResult.fail(statusCode, responseBody);
            kpipLogService.kpipLogFail(kpipLogId, String.valueOf(statusCode), responseBody);
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, statusCode);
            }
        } catch (Exception e) {
            // 기타 예외 처리
            serviceResult.fail(500, "Unknwon Error");
            kpipLogService.kpipLogFail(kpipLogId, "500", "Unknwon Error");
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, "UNKHOWN");
            }
        }

        return serviceResult;
    }

    // 1-5. OCSP Response message
    // OCSP 검증은 1-3으로 이미 수행하는데, 추가로 OCSP 응답 메시지를 받는 기능이 있어서 구현해놓음
    // 도식 상으로 EVSE에서 직접 OCSP를 검증하는 작업을 해야 할 때 사용하는 것으로 보이나
    // 현재 EVSE의 모든 요청은 MSP를 통해 다시 eMSP로 전달되므로 OCSP를 검증하는 것도 1-3만 가지고 수행이 가능할 것으로 예상됨
    public ServiceResult<String> getOcspResponseMessage(KpipReqBodyGetOcspMessage request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();

        String url = "/cert/ocsp-response-msg";
        HttpHeaders headers = createHeaders();

        String bodyJson = null;  try {
            bodyJson = objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            apiLogger.error("Kpip Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {  
            apiLogger.info("TAG: KPIP_START, Request URL: {}, Track ID: {}, Body: {}", url, trackId, bodyJson);
        }
        String kpipLogId = kpipLogService.kpipLogStart(url, request, trackId);
        CheckpointReference checkpoint = new CheckpointReference(kpipLogId);
        serviceResult.getCheckpoints().add(checkpoint);

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        try {
            Map<String, Object> response = responseMono.block();

            if (response != null) {

                String resultCode = (String) response.get("resultCode");
                String resultMsg = (String) response.get("resultMsg");
                String status = (String) response.get("status");

                // EVSE에서 처리가능한 Base 64 Encoded OCSP Response Message
                String ocspRes = (String) response.get("ocspRes");

                if (resultCode.equals("OK")) {
                    // KEPCO쪽 계약인증서 검증 성공
                    // 이후 로직 처리
                    serviceResult.succeed(resultMsg);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_SUCCESS);
                } else {
                    // KEPCO쪽 계약인증서 검증 실패
                    // resultMsg에 따른 분기 필요할 것으로 보임
                    serviceResult.fail(500, resultMsg);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
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
                kpipLogService.kpipLogFinish(kpipLogId, "200", response);
                if (apiLogger.isInfoEnabled()) {
                    String responseJson = null;  
                    try {
                        responseJson = objectMapper.writeValueAsString(response);
                    } catch (Exception ex) {
                        apiLogger.error("Kpip Body 직렬화 실패.");
                    }
                    apiLogger.info("TAG: KPIP_END, Request URL: {}, Track ID: {}, Body: {}", url, trackId, responseJson);
                }

            } else {
                kpipLogService.kpipLogFail(kpipLogId, String.valueOf(400), "No Response");
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                serviceResult.fail(400, "No Response"); // 실제로 받은 에러코드와 body, 혹은 적당한 에러메세지를 받아야 함.
                if (apiLogger.isWarnEnabled()) {
                    apiLogger.warn("TAG: KPIP_END, Request URL: {}, Track ID: {}, MESSAGE: {}", url, trackId, "No Response");
                }
            }
        } catch (WebClientResponseException e) {
            // 4xx 및 5xx 오류를 여기서 처리
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            serviceResult.fail(statusCode, responseBody);
            kpipLogService.kpipLogFail(kpipLogId, String.valueOf(statusCode), responseBody);
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, statusCode);
            }
        } catch (Exception e) {
            // 기타 예외 처리
            serviceResult.fail(500, "Unknwon Error");
            kpipLogService.kpipLogFail(kpipLogId, "500", "Unknwon Error");
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, "UNKHOWN");
            }
        }

        return serviceResult;

    }

    // 2-1. Issue Contract Certificate
    public ServiceResult<Map<String, Object>> issueContractCert(KpipReqBodyIssueContractCert request, String trackId) {
        ServiceResult<Map<String, Object>> serviceResult = new ServiceResult<>();

        String url = "/contract/issue";

        String bodyJson = null;  try {
            bodyJson = objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            apiLogger.error("Kpip Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {  
            apiLogger.info("TAG: KPIP_START, Request URL: {}, Track ID: {}, Body: {}", url, trackId, bodyJson);
        }
        String kpipLogId = kpipLogService.kpipLogStart(url, request, trackId);
        CheckpointReference checkpoint = new CheckpointReference(kpipLogId);
        serviceResult.getCheckpoints().add(checkpoint);

        HttpHeaders headers = createHeaders();

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        try {
            Map<String, Object> response = responseMono.block();

            if (response != null) {
                kpipLogService.kpipLogFinish(kpipLogId, "200", response);
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_SUCCESS);
                serviceResult.succeed(response);
                if (apiLogger.isInfoEnabled()) {
                    String responseJson = null;  
                    try {
                        responseJson = objectMapper.writeValueAsString(response);
                    } catch (Exception ex) {
                        apiLogger.error("Kpip Body 직렬화 실패.");
                    }
                    apiLogger.info("TAG: KPIP_END, Request URL: {}, Track ID: {}, Body: {}", url, trackId, responseJson);
                }
            } else {
                kpipLogService.kpipLogFail(kpipLogId, String.valueOf(400), "No Response");
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                serviceResult.fail(400, "No Response"); // 실제로 받은 에러코드와 body, 혹은 적당한 에러메세지를 받아야 함.
                if (apiLogger.isWarnEnabled()) {
                    apiLogger.warn("TAG: KPIP_END, Request URL: {}, Track ID: {}, MESSAGE: {}", url, trackId, "No Response");
                }
            }
        } catch (WebClientResponseException e) {
            // 4xx 및 5xx 오류를 여기서 처리
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            serviceResult.fail(statusCode, responseBody);
            kpipLogService.kpipLogFail(kpipLogId, String.valueOf(statusCode), responseBody);
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, statusCode);
            }
        } catch (Exception e) {
            // 기타 예외 처리
            serviceResult.fail(500, "Unknwon Error");
            kpipLogService.kpipLogFail(kpipLogId, "500", "Unknwon Error");
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, "UNKHOWN");
            }
        }
        return serviceResult;
    }

    // 2-2. Revoke Contract Certificate
    public ServiceResult<Map<String, Object>> revokeContractCert(KpipReqBodyEmaid request, String trackId) {
        ServiceResult<Map<String, Object>> serviceResult = new ServiceResult<>();

        String url = "/contract/revocation";
        HttpHeaders headers = createHeaders();

        String bodyJson = null;  try {
            bodyJson = objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            apiLogger.error("Kpip Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {  
            apiLogger.info("TAG: KPIP_START, Request URL: {}, Track ID: {}, Body: {}", url, trackId, bodyJson);
        }
        String kpipLogId = kpipLogService.kpipLogStart(url, request, trackId);
        CheckpointReference checkpoint = new CheckpointReference(kpipLogId);
        serviceResult.getCheckpoints().add(checkpoint);

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        try {
            Map<String, Object> response = responseMono.block();

            if (response != null) {

                String resultCode = (String) response.get("resultCode");
                String resultMsg = (String) response.get("resultMsg");

                if (resultCode.equals("OK")) {
                    // KEPCO쪽 계약인증서 삭제 성공
                    // 이후 로직 처리
                    serviceResult.succeed(response);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_SUCCESS);
                } else {
                    // KEPCO쪽 계약인증서 삭제 실패
                    // resultMsg에 따른 분기 필요할 것으로 보임
                    serviceResult.fail(500, resultMsg);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                }
                kpipLogService.kpipLogFinish(kpipLogId, "200", response);
                if (apiLogger.isInfoEnabled()) {
                    String responseJson = null;  
                    try {
                        responseJson = objectMapper.writeValueAsString(response);
                    } catch (Exception ex) {
                        apiLogger.error("Kpip Body 직렬화 실패.");
                    }
                    apiLogger.info("TAG: KPIP_END, Request URL: {}, Track ID: {}, Body: {}", url, trackId, responseJson);
                }
            } else {
                kpipLogService.kpipLogFail(kpipLogId, String.valueOf(400), "No Response");
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                serviceResult.fail(400, "No Response"); // 실제로 받은 에러코드와 body, 혹은 적당한 에러메세지를 받아야 함.
                if (apiLogger.isWarnEnabled()) {
                    apiLogger.warn("TAG: KPIP_END, Request URL: {}, Track ID: {}, MESSAGE: {}", url, trackId, "No Response");
                }
            }
        } catch (WebClientResponseException e) {
            // 4xx 및 5xx 오류를 여기서 처리
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            serviceResult.fail(statusCode, responseBody);
            kpipLogService.kpipLogFail(kpipLogId, String.valueOf(statusCode), responseBody);
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, statusCode);
            }
        } catch (Exception e) {
            // 기타 예외 처리
            serviceResult.fail(500, "Unknwon Error");
            kpipLogService.kpipLogFail(kpipLogId, "500", "Unknwon Error");
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, "UNKHOWN");
            }
        }

        return serviceResult;
    }

    // 2-3. Verify Contract Certificate
    public ServiceResult<Map<String, Object>> verifyContractCert(KpipReqBodyVerifyContractCert request,
            String trackId) {
        ServiceResult<Map<String, Object>> serviceResult = new ServiceResult<>();

        String url = "/pnc-auth/contract-cert-verification";
        HttpHeaders headers = createHeaders();

        String bodyJson = null;  try {
            bodyJson = objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            apiLogger.error("Kpip Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {  
            apiLogger.info("TAG: KPIP_START, Request URL: {}, Track ID: {}, Body: {}", url, trackId, bodyJson);
        }
        String kpipLogId = kpipLogService.kpipLogStart(url, request, trackId);
        CheckpointReference checkpoint = new CheckpointReference(kpipLogId);
        serviceResult.getCheckpoints().add(checkpoint);

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        try {
            Map<String, Object> response = responseMono.block();

            if (response != null) {

                String resultCode = (String) response.get("resultCode");
                String resultMsg = (String) response.get("resultMsg");
                String status = (String) response.get("ocspStatus");

                serviceResult.succeed(response);
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_SUCCESS);
                /*
                 일단 결과자체는 ok이고, 내부검증은 밖에서한다.
                if (resultCode.equals("OK")) {
                    // KEPCO쪽 계약인증서 검증 성공
                    // 이후 로직 처리
                    serviceResult.succeed(response);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_SUCCESS);
                } else {
                    // KEPCO쪽 계약인증서 검증 실패
                    // resultMsg에 따른 분기 필요할 것으로 보임
                    serviceResult.fail(500, resultMsg);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                }
 */
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
                kpipLogService.kpipLogFinish(kpipLogId, "200", response);
                if (apiLogger.isInfoEnabled()) {
                    String responseJson = null;  
                    try {
                        responseJson = objectMapper.writeValueAsString(response);
                    } catch (Exception ex) {
                        apiLogger.error("Kpip Body 직렬화 실패.");
                    }
                    apiLogger.info("TAG: KPIP_END, Request URL: {}, Track ID: {}, Body: {}", url, trackId, responseJson);
                }
            } else {
                kpipLogService.kpipLogFail(kpipLogId, String.valueOf(400), "No Response");
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                serviceResult.fail(400, "No Response"); // 실제로 받은 에러코드와 body, 혹은 적당한 에러메세지를 받아야 함.
                if (apiLogger.isWarnEnabled()) {
                    apiLogger.warn("TAG: KPIP_END, Request URL: {}, Track ID: {}, MESSAGE: {}", url, trackId, "No Response");
                }
            }
        } catch (WebClientResponseException e) {
            // 4xx 및 5xx 오류를 여기서 처리
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            serviceResult.fail(statusCode, responseBody);
            kpipLogService.kpipLogFail(kpipLogId, String.valueOf(statusCode), responseBody);
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, statusCode);
            }
        } catch (Exception e) {
            // 기타 예외 처리
            serviceResult.fail(500, "Unknwon Error");
            kpipLogService.kpipLogFail(kpipLogId, "500", "Unknwon Error");
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, "UNKHOWN");
            }
        }
        return serviceResult;
    }

    // 2-4. Push Whitelist
    public ServiceResult<Map<String, Object>> pushWhitelist(List<KpipReqBodyPushWhitelistItem> request,
            String trackId) {

        ServiceResult<Map<String, Object>> serviceResult = new ServiceResult<>();
        String url = "/pnc-auth/white-list";
        HttpHeaders headers = createHeaders();

        String bodyJson = null;  try {
            bodyJson = objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            apiLogger.error("Kpip Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {  
            apiLogger.info("TAG: KPIP_START, Request URL: {}, Track ID: {}, Body: {}", url, trackId, bodyJson);
        }
        String kpipLogId = kpipLogService.kpipLogStart(url, request, trackId);
        CheckpointReference checkpoint = new CheckpointReference(kpipLogId);
        serviceResult.getCheckpoints().add(checkpoint);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("emaidList", request);

        try {
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            System.out.println("request :" + requestBodyJson);

            Mono<Map<String, Object>> responseMono = webClient.put()
            .uri(url)
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .body(BodyInserters.fromValue(requestBodyJson))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
            });

            Map<String, Object> response = responseMono.block();
            System.out.println("response :" + response);

            if (response != null) {
                String resultCode = (String) response.get("resultCode");
                // String resultMsg = (String) response.get("resultMsg");

                if (resultCode.equals("OK")) {
                    // List 전체 처리 성공
                    // 이후 로직 처리
                    serviceResult.succeed(response);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_SUCCESS);
                    //return serviceResult;
                } else if (resultCode.equals("OK")) {
                    // List 중 일부만 처리 성공
                    // emadList 내부의 각각의 resultMsg 확인 필요
                    serviceResult.succeed(response);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_SUCCESS);
                } else {
                    // 처리 실패
                    serviceResult.fail(500, "KEPCO 처리 실패");
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                }

                // if (response.containsKey("emaidList")) {
                //     List<Map<String, Object>> emaidListMap = (List<Map<String, Object>>) response.get("emaidList");
                //     List<KpipResBodyPushWhitelistItem> emaidList = objectMapper.convertValue(
                //             emaidListMap,
                //             new TypeReference<List<KpipResBodyPushWhitelistItem>>() {}
                //     );
                //     // 처리 결과가 담긴 List
                //     System.out.println("emaidList: " + emaidList);
                // }
                kpipLogService.kpipLogFinish(kpipLogId, "200", response);
                if (apiLogger.isInfoEnabled()) {
                    String responseJson = null;  
                    try {
                        responseJson = objectMapper.writeValueAsString(response);
                    } catch (Exception ex) {
                        apiLogger.error("Kpip Body 직렬화 실패.");
                    }
                    apiLogger.info("TAG: KPIP_END, Request URL: {}, Track ID: {}, Body: {}", url, trackId, responseJson);
                }
            } else {
                kpipLogService.kpipLogFail(kpipLogId, String.valueOf(400), "No Response");
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                serviceResult.fail(400, "No Response"); // 실제로 받은 에러코드와 body, 혹은 적당한 에러메세지를 받아야 함.
                if (apiLogger.isWarnEnabled()) {
                    apiLogger.warn("TAG: KPIP_END, Request URL: {}, Track ID: {}, MESSAGE: {}", url, trackId, "No Response");
                }

            }
        } catch (WebClientResponseException e) {
            // 4xx 및 5xx 오류를 여기서 처리
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            serviceResult.fail(statusCode, responseBody);
            kpipLogService.kpipLogFail(kpipLogId, String.valueOf(statusCode), responseBody);
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, statusCode);
            }

        } catch (Exception e) {
            serviceResult.fail(500, "Unknwon Error");
            kpipLogService.kpipLogFail(kpipLogId, "500", "Unknwon Error");
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, "UNKHOWN");
            }

        }

        return serviceResult;
    }

    // 2-5. Download CRL of MOSubCA
    public ServiceResult<String> downloadContCrl(KpipReqBodyDownloadCrl request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        String url = "/pnc-auth/download-crl-mo";
        HttpHeaders headers = createHeaders();

        String bodyJson = null;  try {
            bodyJson = objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            apiLogger.error("Kpip Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {  
            apiLogger.info("TAG: KPIP_START, Request URL: {}, Track ID: {}, Body: {}", url, trackId, bodyJson);
        }
        String kpipLogId = kpipLogService.kpipLogStart(url, request, trackId);
        CheckpointReference checkpoint = new CheckpointReference(kpipLogId);
        serviceResult.getCheckpoints().add(checkpoint);

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        try {
            Map<String, Object> response = responseMono.block();

            if (response != null) {
                String resultCode = (String) response.get("resultCode");
                String resultMsg = (String) response.get("resultMsg");

                String crl = (String) response.get("crl");

                if (resultCode.equals("OK")) {
                    // List 전체 처리 성공
                    // 이후 로직 처리
                    serviceResult.succeed(resultMsg);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_SUCCESS);
                } else {
                    // 처리 실패
                    serviceResult.fail(500, resultMsg);
                    checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                }
                kpipLogService.kpipLogFinish(kpipLogId, "200", response);
                if (apiLogger.isInfoEnabled()) {  
                    String responseJson = null;  
                    try {
                        responseJson = objectMapper.writeValueAsString(response);
                    } catch (Exception ex) {
                        apiLogger.error("Kpip Body 직렬화 실패.");
                    }
                    apiLogger.info("TAG: KPIP_END, Request URL: {}, Track ID: {}, Body: {}", url, trackId, responseJson);
                }

            } else {
                kpipLogService.kpipLogFail(kpipLogId, String.valueOf(400), "No Response");
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                serviceResult.fail(400, "No Response"); // 실제로 받은 에러코드와 body, 혹은 적당한 에러메세지를 받아야 함.
                if (apiLogger.isWarnEnabled()) {
                    apiLogger.warn("TAG: KPIP_END, Request URL: {}, Track ID: {}, MESSAGE: {}", url, trackId, "No Response");
                }

            }
        } catch (WebClientResponseException e) {
            // 4xx 및 5xx 오류를 여기서 처리
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            serviceResult.fail(statusCode, responseBody);
            kpipLogService.kpipLogFail(kpipLogId, String.valueOf(statusCode), responseBody);
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, statusCode);
            }

        } catch (Exception e) {
            serviceResult.fail(500, "Unknwon Error");
            kpipLogService.kpipLogFail(kpipLogId, "500", "Unknwon Error");
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);

            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, "UNKHOWN");
            }
        }

        return serviceResult;
    }

    // 2-6. Authorize PnC account
    public ServiceResult<Map<String, Object>> authorizePnc(KpipReqBodyEmaid request, String trackId) {
        ServiceResult<Map<String, Object>> serviceResult = new ServiceResult<>();
        String url = "/pnc-auth/authorize-account";
        HttpHeaders headers = createHeaders();

     
        String bodyJson = null;  try {
            bodyJson = objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            apiLogger.error("Kpip Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {  
            apiLogger.info("TAG: KPIP_START, Request URL: {}, Track ID: {}, Body: {}", url, trackId, bodyJson);
        }
        String kpipLogId = kpipLogService.kpipLogStart(url, request, trackId);
        CheckpointReference checkpoint = new CheckpointReference(kpipLogId);
        serviceResult.getCheckpoints().add(checkpoint);

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        try {
            Map<String, Object> response = responseMono.block();

            if (response != null) {
                serviceResult.succeed(response);
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_SUCCESS);
                kpipLogService.kpipLogFinish(kpipLogId, "200", response);
                if (apiLogger.isInfoEnabled()) {
                    String responseJson = null;  
                    try {
                        responseJson = objectMapper.writeValueAsString(response);
                    } catch (Exception ex) {
                        apiLogger.error("Kpip Body 직렬화 실패.");
                    }
                    apiLogger.info("TAG: KPIP_END, Request URL: {}, Track ID: {}, Body: {}", url, trackId, responseJson);
                }

            } else {
                kpipLogService.kpipLogFail(kpipLogId, String.valueOf(400), "No Response");
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                serviceResult.fail(400, "No Response"); // 실제로 받은 에러코드와 body, 혹은 적당한 에러메세지를 받아야 함.
                if (apiLogger.isWarnEnabled()) {
                    apiLogger.warn("TAG: KPIP_END, Request URL: {}, Track ID: {}, MESSAGE: {}", url, trackId, "No Response");
                }

            }
        } catch (WebClientResponseException e) {
            // 4xx 및 5xx 오류를 여기서 처리
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            serviceResult.fail(statusCode, responseBody);
            kpipLogService.kpipLogFail(kpipLogId, String.valueOf(statusCode), responseBody);
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, statusCode);
            }

        } catch (Exception e) {
            serviceResult.fail(500, "Unknwon Error");
            kpipLogService.kpipLogFail(kpipLogId, "500", "Unknwon Error");
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);

            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KPIP_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, "UNKHOWN");
            }
        }

        return serviceResult;
    }
}
