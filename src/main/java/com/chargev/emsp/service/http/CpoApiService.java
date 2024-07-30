package com.chargev.emsp.service.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.chargev.emsp.model.dto.ocpi.CpoReqBodyStartSession;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.log.CheckpointKind;
import com.chargev.emsp.service.log.CheckpointReference;
import com.chargev.emsp.service.log.CpoLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;


@Service
public class CpoApiService {

    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final CpoLogService cpoLogService;
    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");

    @Value("${cpo.token}")
    private String token;

    @Value("${cpo.base.url}")
    private String baseUrl;

    public CpoApiService(@Value("${cpo.base.url}") String baseUrl, @Value("${cpo.token}") String token, CpoLogService cpoLogService, ObjectMapper objectMapper) {
        // 디버깅 로그 추가
        apiLogger.info("Initializing WebClient with baseUrl: {}", baseUrl);
        
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                // JWT 토큰인데 Bearer를 쓰지 않는다. (추후 변경 가능성 있을 것 같음.)
                .defaultHeader(HttpHeaders.AUTHORIZATION, token)
                .baseUrl(baseUrl)
                .filter(logRequest()) // 요청 로깅 필터 추가
                .filter(logResponse()) // 응답 로깅 필터 추가
                .build();
        this.cpoLogService = cpoLogService;
        this.objectMapper = objectMapper;
    }

        // 요청을 로깅하는 필터
        private ExchangeFilterFunction logRequest() {
            return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                apiLogger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
                return Mono.just(clientRequest);
            });
        }
    
        // 응답을 로깅하는 필터
        private ExchangeFilterFunction logResponse() {
            return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                apiLogger.info("Response Status: {}", clientResponse.statusCode());
                return Mono.just(clientResponse);
            });
        }

    // 1-1. START_SESSION (to CPO)
    public ServiceResult<Map<String, Object>> startSessionToCpo(CpoReqBodyStartSession request, String ecKey, String trackId) {
        
        ServiceResult<Map<String, Object>> serviceResult = new ServiceResult<>();

        String url = "";

        try {
            url = "/emsp/charger/" + ecKey + "/start";
        } catch (Exception e) {
            apiLogger.error("ecKey 오류.");
        }
        
        String bodyJson = null;
        
        try {
            bodyJson = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException ex) {
            apiLogger.error("CPO Body 직렬화 실패.", ex);
        } catch (Exception ex) {
            apiLogger.error("CPO Body 처리 중 알 수 없는 오류로 실패.");
        }
        
        if (apiLogger.isInfoEnabled()) {  
            apiLogger.info("TAG: CPO_START, Request URL: {}, Track ID: {}, Body: {}", url, trackId, bodyJson);
        }

        String cpoLogId = cpoLogService.cpoLogStart(url, request, trackId);
        CheckpointReference checkpoint = new CheckpointReference(cpoLogId);
        serviceResult.getCheckpoints().add(checkpoint);

        Mono<Map<String, Object>> responseMono = webClient.post()
                .uri(url)
                .body(BodyInserters.fromValue(request))
                .retrieve().bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });
        try {
            Map<String, Object> response = responseMono.block();

            if (response != null) {
                cpoLogService.cpoLogFinish(cpoLogId, "200", response);
                checkpoint.setCheckpointKind(CheckpointKind.CPO_SEND_SUCCESS);
                serviceResult.succeed(response);
                if (apiLogger.isInfoEnabled()) {
                    String responseJson = null;  
                    try {
                        responseJson = objectMapper.writeValueAsString(response);
                    } catch (JsonProcessingException ex) {
                        apiLogger.error("CPO Body 직렬화 실패.", ex);
                    } catch (Exception ex) {
                        apiLogger.error("CPO Body 처리 중 알 수 없는 오류로 실패.");
                    }
                    apiLogger.info("TAG: CPO_END, Request URL: {}, Track ID: {}, Body: {}", url, trackId, responseJson);
                }
            } else {
                cpoLogService.cpoLogFail(cpoLogId, String.valueOf(400), "No Response");
                checkpoint.setCheckpointKind(CheckpointKind.CPO_SEND_FAIL);
                serviceResult.fail(400, "No Response"); // 실제로 받은 에러코드와 body, 혹은 적당한 에러메세지를 받아야 함.
                if (apiLogger.isWarnEnabled()) {
                    apiLogger.warn("TAG: CPO_END, Request URL: {}, Track ID: {}, MESSAGE: {}", url, trackId, "No Response");
                }
            }
        } catch (WebClientResponseException e) {
            // 4xx 및 5xx 오류를 여기서 처리
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            serviceResult.fail(statusCode, responseBody);
            cpoLogService.cpoLogFail(cpoLogId, String.valueOf(statusCode), responseBody);
            checkpoint.setCheckpointKind(CheckpointKind.CPO_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: CPO_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, statusCode);
            }
        } catch (Exception e) {
            // 기타 예외 처리
            serviceResult.fail(500, "Unknwon Error");
            cpoLogService.cpoLogFail(cpoLogId, "500", "Unknwon Error");
            checkpoint.setCheckpointKind(CheckpointKind.CPO_SEND_FAIL);
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: CPO_END, Request URL: {}, Track ID: {}, STATUS: {}", url, trackId, "UNKHOWN");
            }
        }
        return serviceResult;
    }

}
