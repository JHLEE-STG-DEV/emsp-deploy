package com.chargev.emsp.repository.key;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.chargev.emsp.entity.keyentity.Keys;

public interface KeyRepository extends CrudRepository<Keys, String>, PagingAndSortingRepository<Keys, String> {
    // select all value of Keys where deleted = 0 and clientId = :clientId and clientSecret = :clientSecret
    @Query(value = "SELECT e FROM Keys e WHERE e.deleted = 0 AND e.clientId = :clientId AND e.clientSecret = :clientSecret AND e.keyType = :keyType",  nativeQuery = false)
    Keys findByClientIdAndClientSecret(@Param("clientId") String clientId, @Param("clientSecret") String clientSecret, @Param("keyType") String keyType);
}
