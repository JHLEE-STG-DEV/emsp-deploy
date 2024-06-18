package com.chargev.emsp.repository.authentication;

import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.chargev.emsp.entity.authenticationentity.TokenIssueHistory;

public interface TokenIssueHistoryRepository extends CrudRepository<TokenIssueHistory, String>, PagingAndSortingRepository<TokenIssueHistory, String> {
    @Query(value = "SELECT COALESCE(MAX(e.issueSerial), 0) FROM TokenIssueHistory e WHERE e.subjectId = :subjectId", nativeQuery = false)
    Integer getNextSerial(@Param("subjectId") String subjectId);
}
