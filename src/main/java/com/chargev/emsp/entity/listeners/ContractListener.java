package com.chargev.emsp.entity.listeners;

import com.chargev.emsp.entity.contract.Contract;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
public class ContractListener {
    
    @PrePersist
    @PreUpdate
    public void validateContract(Contract contract) {
        truncateFields(contract);
    }

    private void truncateFields(Contract contract) {
        contract.setStatusMessage(Truncater.truncateIfOver(contract.getStatusMessage(), 1024));
    }
}
