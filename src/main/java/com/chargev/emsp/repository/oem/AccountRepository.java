package com.chargev.emsp.repository.oem;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.oem.Account;

public interface AccountRepository extends JpaRepository<Account, String> {
    boolean existsByCiamId(String ciamId);
    Optional<Account> findByCiamId(String ciamId);
    Optional<Account> findByEmspAccountKeyAndAccountStatus(String emspAccountKey, Integer status);
}
