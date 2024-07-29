package com.chargev.emsp.service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TopicNames {
    @Value("${kafka.topic.topicname.${version}}")
    private String topicName;
    public String getTopicName() {
        return topicName;
    }
}
