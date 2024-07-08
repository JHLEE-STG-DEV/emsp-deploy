package com.chargev.emsp.repository.log;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.log.CheckpointLog;

public interface CheckpointLogRepository extends JpaRepository<CheckpointLog, String>{
    
}
