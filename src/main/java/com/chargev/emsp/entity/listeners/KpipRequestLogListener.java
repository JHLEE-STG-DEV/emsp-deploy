package com.chargev.emsp.entity.listeners;

import com.chargev.emsp.entity.log.KpipRequestLog;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class KpipRequestLogListener {
    @PrePersist
    @PreUpdate
    public void validateCertification(KpipRequestLog log) {
        truncateFields(log);
    }

    private void truncateFields(KpipRequestLog cert) {
        cert.setEndpoint(Truncater.truncateIfOver(cert.getEndpoint(), 2048));
        cert.setErrorMessage(Truncater.truncateIfOver(cert.getErrorMessage(), 2048));
    }
    
}
