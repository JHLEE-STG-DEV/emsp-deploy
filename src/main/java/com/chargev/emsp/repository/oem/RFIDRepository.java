package com.chargev.emsp.repository.oem;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.oem.RFID;

public interface RFIDRepository extends JpaRepository<RFID, String> {
    Optional<RFID> findFirstByStatus(int status);
    // Optional<RFID> findByOemContractIdAndStatus(String contractId, int status);
    List<RFID> findByOemContractContractIdAndStatus(String contractId, int status);
}
