package com.chargev.emsp.entity.listeners;

import com.chargev.emsp.entity.cert.Certification;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
public class CertificationListener {
      
    @PrePersist
    @PreUpdate
    public void validateCertification(Certification cert) {
        truncateFields(cert);
    }

    private void truncateFields(Certification cert) {
        cert.setStatusMessage(Truncater.truncateIfOver(cert.getStatusMessage(), 1024));
    }

}
