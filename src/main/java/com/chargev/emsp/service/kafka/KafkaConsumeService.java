package com.chargev.emsp.service.kafka;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.chargev.emsp.config.OCPICondition;
import com.chargev.utils.IdHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Conditional(OCPICondition.class)
public class KafkaConsumeService {
    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");
    private final ObjectMapper objectMapper;
    private final CouchbaseService couchbaseService;
    private final TopicNames topicNames;

    @KafkaListener(topics = "#{topicNames.getTopicName()}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(Map<String, Object> message, Acknowledgment acknowledgment) {
        String trackId = IdHelper.genLowerUUID32();
        System.out.println("Received message: " + message);
        try {
            System.out.println("================================");
            if (message != null) {
                for (var entry : message.entrySet()) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }
            }

            if (message.containsKey("payload")) {
                Object payload = message.get("payload");
                if (payload instanceof String) {
                    couchbaseService.saveEvcpStatusToCouchbase(payload.toString(), trackId);
                    // try {
                    //     // parse된 것이 필요하면
                    //     Map<String, Object> parsedPayload = objectMapper.readValue((String) payload, Map.class);

                    // } catch (Exception e) {
                    //     System.err.println("Failed to parse the payload field: " + e.getMessage());
                    // }
                }
            }


            // 메시지가 성공적으로 처리된 후 오프셋 커밋
            acknowledgment.acknowledge();

        } catch (Exception ex) {
            System.err.println("Error processing message: " + ex.getMessage());
        }
    }

    // 필요할지 몰라서 일단 여기 작성
    public enum ChargeStatus {
        AVAILABLE,
        BLOCKED,
        CHARGING,
        INOPERATIVE,
        OUTOFORDER,
        PLANNED,
        REMOVED,
        RESERVED,
        UNKNOWN
    }
}
