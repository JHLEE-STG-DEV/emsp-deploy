package com.chargev.emsp.service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.stereotype.Service;

import com.chargev.emsp.service.ServiceResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouchbaseService {
    private final ObjectMapper objectMapper;
    private final CouchbaseTemplate couchbaseTemplate;
    private static final Logger couchbaseLogger = LoggerFactory.getLogger("COUCHBASE_LOGGER");

    public ServiceResult<String> saveEvcpStatusToCouchbase(String message, String trackId) {
        // Message를 Couchbase에 저장하는 로직
        couchbaseLogger.info("TAG:COUCHBASE_SEND, TRACK ID: {},  Request: {}", trackId,    message);
        ServiceResult<String> result = new ServiceResult<>();
        try {
            EvcpStatus evcpStatus = objectMapper.readValue(message, EvcpStatus.class);

            couchbaseTemplate.save(evcpStatus);
            result.succeed("OK");
            couchbaseLogger.info("TAG:COUCHBASE_SUCCEEDD, TRACK ID: {}", trackId);
        } catch (Exception ex) {
            ex.printStackTrace();
            result.fail(500, "Failed to parse");
            couchbaseLogger.info("TAG:COUCHBASE_FAIL, TRACK ID: {}, MESSAGE: {}", trackId, ex.getMessage());
        }
        return result;

    }

    // 어디에서 사용하는건지 몰라서 일단 내부에 구현.
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Document
    public static class EvcpStatus {
        @Id
        private String ecKey;
        private String status;
        private String operationStatus;
        private String chargerStatus;
        private String requestDate;
    }
}
