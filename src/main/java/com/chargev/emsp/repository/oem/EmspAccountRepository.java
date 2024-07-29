package com.chargev.emsp.repository.oem;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.oem.EmspAccountEntity    ;

public interface EmspAccountRepository extends JpaRepository<EmspAccountEntity, String> {
    boolean existsByCiamId(String ciamId);
    Optional<EmspAccountEntity> findByCiamId(String ciamId);
    Optional<EmspAccountEntity> findByEmspAccountKeyAndAccountStatus(String emspAccountKey, Integer status);
    Optional<EmspAccountEntity> findByEmspAccountKeyAndAccountStatusIn(String emspAccountKey, List<Integer> statuses);
}