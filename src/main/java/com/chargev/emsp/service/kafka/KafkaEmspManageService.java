package com.chargev.emsp.service.kafka;

import com.chargev.emsp.model.dto.oem.EmspKafkaRfid;

public interface KafkaEmspManageService {
    public void sendRfidModify(EmspKafkaRfid kafkaObject, String trackId);
}
