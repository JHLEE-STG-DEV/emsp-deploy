package com.chargev.emsp.entity.authentication;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "EV_AUTH_SUBJECT")
@Entity
@Data

// 인증 관련 챠번은 

public class AuthSubject {
    @Id
    @Column(name = "SUBJECT_ID", columnDefinition = "CHAR(32)", nullable = false, unique = true)
    private int subjectId;

    @Column(name = "SUBJECT_NAME", columnDefinition = "VARCHAR(32)", nullable = false)
    private int subjectName;

    // OEM, CPO, MSP, MO, etc -> 한전을 뭐라고 칭할 것인지 표준으로 결정해야 함 
    @Column(name = "SUBJECT_TYPE", columnDefinition = "VARCHAR(3)", nullable = false)
    private int subjectType;

    @Column(name = "SUBJECT_STATUS", columnDefinition = "INT",  nullable = false)
    private int subjectStatus;

    @Column(name = "SUBJECT_DESC", columnDefinition = "VARCHAR(255)")
    private int subjectDesc;

    @Column(name = "SUBJECT_EMAIL", columnDefinition = "VARCHAR(255)")
    private int subjectEmail;

    @Column(name = "SUBJECT_PHONE", columnDefinition = "VARCHAR(32)")
    private int subjectPhone;

    @Column(name = "SUBJECT_PASSWORD", columnDefinition = "CHAR(64)",  nullable = false)
    private int subjectPassword;

    @Column(name = "CREATED_DATE", columnDefinition = "DATE",  nullable = false)
    @CreationTimestamp
    private int createdDate;

    @Column(name = "UPDATED_DATE", columnDefinition = "DATE")
    @CreationTimestamp
    private int updatedDate;

    @Column(name = "CREATED_USER", columnDefinition = "CHAR(32)")
    private int createdUser;

    @Column(name = "UPDATED_USER", columnDefinition = "CHAR(32)")
    private int updatedUser;

    @Column(name = "SUBJECT_ROLES", columnDefinition = "VARCHAR(1024)")
    private int subjectRoles;

}
