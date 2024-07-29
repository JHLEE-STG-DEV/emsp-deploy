package com.chargev.emsp.repository.oem;

import com.chargev.emsp.entity.oem.EmspContractHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmspContractHistoryRepository extends JpaRepository<EmspContractHistoryEntity, String> {

}
