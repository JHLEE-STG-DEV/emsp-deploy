package com.chargev.emsp.service.http;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

import com.chargev.emsp.model.dto.pnc.KpipReqBodyIssueCert;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.log.CheckpointKind;
import com.chargev.emsp.service.log.CheckpointReference;
import com.chargev.emsp.service.log.KpipLogService;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Service
public class MBApiService {

    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final KpipLogService kpipLogService;
    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");

    // base url, spid, spkey (custom.properties에서 설정한 값을 불러온다.)
    @Value("${mb.base.url}")
    private String baseUrl;




    public MBApiService(@Value("${kpip.base.url}") String baseUrl, KpipLogService kpipLogService, ObjectMapper objectMapper) {
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
            apiLogger.info("TAG: MB_START, Request URL: {}, Track ID: {}, Body: {}", url, trackId, bodyJson);
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
                    apiLogger.info("TAG: MB_END, Request URL: {}, Track ID: {}, Body: {}", url, trackId, responseJson);
                }
            } else {
                kpipLogService.kpipLogFail(kpipLogId, String.valueOf(400), "No Response");
                checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
                serviceResult.fail(400, "No Response"); // 실제로 받은 에러코드와 body, 혹은 적당한 에러메세지를 받아야 함.
                if (apiLogger.isWarnEnabled()) {
                    apiLogger.warn("TAG: MB_END, Request URL: {}, Track ID: {}, MESSAGE: {}", url, trackId, "No Response");
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
                apiLogger.error("TAG: MB_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, statusCode);
            }
        } catch (Exception e) {
            // 기타 예외 처리
            serviceResult.fail(500, "Unknwon Error");
            kpipLogService.kpipLogFail(kpipLogId, "500", "Unknwon Error");
            checkpoint.setCheckpointKind(CheckpointKind.KPIP_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: MB_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, "UNKHOWN");
            }
        }
        return serviceResult;
    }
}
