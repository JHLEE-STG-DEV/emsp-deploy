package com.chargev.emsp.repository.log;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.log.PncCheckpointLog;

public interface PncCheckpointLogRepository extends JpaRepository<PncCheckpointLog, String> {
    
}
