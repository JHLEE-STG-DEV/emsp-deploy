package com.chargev.emsp.repository.contract;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chargev.emsp.entity.contract.Contract;

public interface ContractRepository  extends JpaRepository<Contract, String>{
    // 메서드 이름을 기반으로 쿼리
    
  
    @Query(value = "SELECT * FROM EV_CONTRACT  WHERE EMA_ID = :emaId LIMIT 1", nativeQuery = true)
    Optional<Contract> findByEmaId(@Param("emaId") String emaId);
    //Optional<Contract> findByPcid(String pcid);
    
    @Query(value = "SELECT * FROM EV_CONTRACT  WHERE PCID = :pcid", nativeQuery = true)
    List<Contract> findAllByPcid(@Param("pcid") String pcid);
    //List<Contract> findAllByPcid(String pcid);
    Optional<Contract> findByMemberKeyAndPcidAndOemId(Long memberKey, String pcid, String oemId);
    
    @Query(value = "SELECT * FROM EV_CONTRACT  WHERE MEMBER_KEY = :memberKey AND PCID = :pcid AND OEM_ID = :oemId AND STATUS = 1 LIMIT 1", nativeQuery = true)
    Optional<Contract> findActiveByMemberKeyAndPcidAndOemId(@Param("memberKey") Long memberKey,
    @Param("pcid") String pcid,
    @Param("oemId") String oemId);
    
    @Query(value = "SELECT * FROM EV_CONTRACT  WHERE MEMBER_KEY = :memberKey AND PCID = :pcid", nativeQuery = true)
    List<Contract> findAllByMemberKeyAndPcid(@Param("memberKey") Long memberKey,
    @Param("pcid") String pcid);
}
