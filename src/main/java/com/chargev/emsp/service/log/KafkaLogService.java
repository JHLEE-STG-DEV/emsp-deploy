package com.chargev.emsp.service.log;

public interface KafkaLogService {
    // Kafka
    
    public String kafkaLogStart(String topic, Object data, String trackId);
    
    public String kafkaFinish(String logId, Long offset);
    
    public String kafakaFail(String logId, String errorCode, String errorMessage);
}
