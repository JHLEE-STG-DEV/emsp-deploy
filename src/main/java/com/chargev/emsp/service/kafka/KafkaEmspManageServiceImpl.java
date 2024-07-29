package com.chargev.emsp.service.kafka;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.chargev.emsp.model.dto.oem.EmspKafkaRfid;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaEmspManageServiceImpl implements KafkaEmspManageService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");

    private final ObjectMapper objectMapper;

    @Override
    public void sendRfidModify(EmspKafkaRfid kafkaObject, String trackId) {
        String topic = "MSG-EMSP-MEMBER-CARD";
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info("TAG: KAFKA_TRY, TOPIC: {}, Track ID: {}", topic, trackId);
        }

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, kafkaObject);

        String bodyJson = null;
        try {
            bodyJson = objectMapper.writeValueAsString(kafkaObject);
        } catch (JsonProcessingException ex) {
            apiLogger.error("kafka Body 직렬화 실패.", ex);
        }
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info("TAG: KAFKA_SEND, TOPIC: {}, Track ID: {}, Object: {}", topic, trackId, bodyJson);
        }
        future.whenComplete((resultSend, ex) -> {
            if (ex == null) {

                if (apiLogger.isInfoEnabled()) {
                    apiLogger.info("TAG: KAFKA_SENT, TOPIC: {}, Track ID: {}", topic, trackId);
                }

            } else {

                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG: KAFKA_SENT, TOPIC: {}, Track ID: {}, Message: {}", topic, trackId,
                            ex.getMessage());
                }
            }
        });

    }

}
