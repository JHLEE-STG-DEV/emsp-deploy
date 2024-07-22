package com.chargev.emsp.entity.listeners;

import com.chargev.emsp.entity.log.KafkaRequestLog;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class KafkaRequestLogListener {
    @PrePersist
    @PreUpdate
    public void validateCertification(KafkaRequestLog log) {
        truncateFields(log);
    }

    private void truncateFields(KafkaRequestLog cert) {
        cert.setErrorMessage(Truncater.truncateIfOver(cert.getErrorMessage(), 2048));
    }
    
}
