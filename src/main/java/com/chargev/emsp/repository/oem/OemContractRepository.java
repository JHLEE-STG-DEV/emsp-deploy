package com.chargev.emsp.repository.oem;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.oem.Account;
import com.chargev.emsp.entity.oem.OemContract;

public interface OemContractRepository extends JpaRepository<OemContract, String> {
    Optional<OemContract> findByVin(String vin);
    Optional<OemContract> findByVinAndContractStatus(String vin, int contractStatus);
    List<OemContract> findByAccount(Account account);
    List<OemContract> findByAccountAndContractStatus(Account account, int contractStatus);
    Optional<OemContract> findByContractId(String contractId);
    Optional<OemContract> findByContractIdAndContractStatus(String contractId, int contractStatus);
}
