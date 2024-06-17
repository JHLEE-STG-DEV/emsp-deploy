package com.chargev.emsp.service.http;

import java.util.Map;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.chargev.emsp.model.dto.oem.MpayAsset;
import com.chargev.emsp.model.dto.oem.MpayError;
import reactor.core.publisher.Mono;

@Service
public class MPayApiService {

    // M-Pay API base URL
    private final String baseUrl = "https://api.uat.mercedes-pay.io";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public MPayApiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String getSingleAsset(String merchantId, String referenceId, String assetId) {
        // assetId => contract나 contract.payment의 asset_id와 매칭되는 값으로
        // 아래 상황에서 등록된 결제 수단이 유효한지 확인하는데 사용된다.
        // 1. eMSP Contract 추가 시, payment.asset_id 유효한지 확인
        // 2. eMSP에서 결제 요청 전 contract.asset_id 유효한지 확인
        String url = "/vaults/" + referenceId + "/assets/" + assetId;


        Mono<ClientResponse> responseMono = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("merchantId", merchantId)
                        .build())
                .exchangeToMono(clientResponse -> Mono.just(clientResponse));

        ClientResponse clientResponse = responseMono.block();

        if (clientResponse.statusCode().is2xxSuccessful()) {
            // 성공 응답 처리 로직
            Mono<Map<String, Object>> bodyMono = clientResponse.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> response = bodyMono.block();
            return handleSuccessResponse(response);
        } else {
            // 에러 응답 처리 로직
            Mono<Map<String, Object>> bodyMono = clientResponse.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> response = bodyMono.block();
            return handleErrorResponse(response);
        }

    }

    private String handleErrorResponse(Map<String, Object> response) {
        // 에러 응답 처리 로직
        List<MpayError> errors = objectMapper.convertValue(response.get("errors"), new TypeReference<List<MpayError>>() {});
        // errors 리스트가 비어 있지 않다면 첫 번째 에러의 detail 필드를 가져옵니다.
        if (errors != null && !errors.isEmpty()) {
            String errorDetail = errors.get(0).getError().getDetail();
            // 추출한 에러 메시지 (detail) 반환
            return "Error response: " + errorDetail;
        }
        return "Error response: Unknown error";
    }

    private String handleSuccessResponse(Map<String, Object> response) {
        // 성공 응답 처리 로직
        MpayAsset mpayAsset = objectMapper.convertValue(response, MpayAsset.class);
        // mpayAsset의 invalid 항목을 보면 될 것으로 추정 (boolean)
        return "Success response: " + mpayAsset.toString();
    }

}
