package com.chargev.emsp.repository.log;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.log.KafkaRequestLog;

public interface KafkaRequestLogRepository extends JpaRepository<KafkaRequestLog, String>{
    
}
