package com.chargev.emsp.repository.oem;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.oem.EmspAccountEntity;
import com.chargev.emsp.entity.oem.EmspContractEntity;

public interface EmspContractRepository extends JpaRepository<EmspContractEntity, String> {
    // Optional<EmspContractEntity> findByVin(String vin);
    Optional<EmspContractEntity> findByVinAndContractStatus(String vin, int contractStatus);
    // List<EmspContractEntity> findByAccount(EmspAccountEntity account);
    List<EmspContractEntity> findByAccountAndContractStatus(EmspAccountEntity account, int contractStatus);
    List<EmspContractEntity> findByAccountAndContractStatusIn(EmspAccountEntity account, List<Integer> statuses);
    Optional<EmspContractEntity> findByContractId(String contractId);
    Optional<EmspContractEntity> findByContractIdAndContractStatus(String contractId, int contractStatus);
    Optional<EmspContractEntity> findByContractIdAndContractStatusIn(String contractId, List<Integer> statuses);
    Optional<EmspContractEntity> findByAccountAndContractIdAndContractStatusIn(EmspAccountEntity account, String contractId, List<Integer> statuses);
    Optional<EmspContractEntity> findByRfidIdAndRfidStatus(String RfidId, int RfidStatus);
    Optional<EmspContractEntity> findByRfidNumAndRfidStatusAndContractStatus(String rfNum, int rfidStatus, int contractStatus);
    Optional<EmspContractEntity> findByContractIdAndContractStatusAndRfidStatus(String contractId, int contractStatus, int rfidStatus);
}

