package com.chargev.emsp.repository.contract;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.contract.ContractIdentity;
import com.chargev.emsp.entity.contract.ContractIdentityUK;

public interface ContractIdentityRepository extends JpaRepository<ContractIdentity, ContractIdentityUK>{
    List<ContractIdentity> findAllByPcid(String pcid);
    
    List<ContractIdentity> findAllByContractId(String contractId);
}
