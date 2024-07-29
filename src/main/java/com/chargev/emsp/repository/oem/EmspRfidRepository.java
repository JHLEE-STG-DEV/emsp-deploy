package com.chargev.emsp.repository.oem;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.oem.EmspRfidEntity;

public interface EmspRfidRepository extends JpaRepository<EmspRfidEntity, String> {
    Optional<EmspRfidEntity> findFirstByStatus(int status);
    // Optional<RFID> findByOemContractIdAndStatus(String contractId, int status);
    // List<EmspRfidEntity> findByContractIdAndStatus(String contractId, int status);
    Optional<EmspRfidEntity> findByContractIdAndRfNum(String contractId, String cardNum);
    Optional<EmspRfidEntity> findByIdAndStatus(String rfidId, int status);
    boolean existsByContractIdAndStatus(String contractId, int rfidStatus);
    Optional<EmspRfidEntity> findByContractIdAndStatusIn(String contractId, List<Integer> statuses);
    Optional<EmspRfidEntity> findByRfNum(String rfNum);
}


