package com.chargev.emsp.entity.authentication;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "EV_AUTH_TOKEN_ISSUE_HISTORY", indexes = @Index(name = "IDX_EV_AUTH_TOKEN_ISSUE_HISTORY", columnList = "SUBJECT_ID, ISSUE_SERIAL", unique = true))
@Entity
@Data
// 이 테이블은 토큰 발급시에 해당 사용자의 토큰을 발급한 이력을 저장하는 테이블로, 일련번호를 채번하기 위한 용도로 사용된다. 
// 일련번호의 중복을 막기 위해 일단 일련번호 채번 -> 발급 -> 이전 번호 무효화 형태로 진행한다. 
public class TokenIssueHistory {
    @Id
    @Column(name = "ISSUE_ID", columnDefinition = "CHAR(32)", nullable = false, unique = true)
    private int issueId;

    @Column(name = "SUBJECT_ID", columnDefinition = "CHAR(32)", nullable = false)
    private String subjectId;

    @Column(name = "ISSUE_SERIAL", columnDefinition = "INT", nullable = false)
    private int issueSerial;

    @Column(name = "ISSUE_DATE", columnDefinition = "DATE", nullable = false)
    @CreationTimestamp
    private int issueDate;

    @Column(name = "ISSUE_STATUS", columnDefinition = "INT", nullable = false)
    private int issueStatus;

    @Column(name = "TOKEN", columnDefinition = "VARCHAR(2048)")
    private String token;

    // 발급 요청자가 발급한 토큰인지, 시스템이 발급한 토큰인지, 관리자가 발급한 토큰인지 발급자를 명시한다. 
    @Column(name = "TOKEN_TYPE", columnDefinition = "INT", nullable = false)
    private int tokenType;

    // 발급자 정보, 발급 요청자가 발급을 요청했다면, 해당 발급자의 PK를 저장한다. 시스템 발급이면 시스템 ID, 관리자 발급이면 관리자 ID를 저장한다. 
    @Column(name = "CREATED_USER", columnDefinition = "CHAR(32)", nullable = false)
    private String createdUser;

    //발급 요청한 곳의 IP 주소를 저장한다. 
    @Column(name = "IP_ADDRESS", columnDefinition = "VARCHAR(32)", nullable = false)
    private String ipAddress;

    // 최종 업데이트 시각을 기록한다.
    @Column(name = "UPDATED_DATE", columnDefinition = "DATE")
    @CreationTimestamp
    private int updatedDate;

}
