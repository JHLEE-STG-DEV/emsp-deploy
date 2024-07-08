package com.chargev.emsp.entity.authenticationentity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Table(name = "EV_AUTH_SUBJECT")
@Entity
@Data

// 인증 관련 

public class AuthSubject {
    @Id
    @Column(name = "SUBJECT_ID", columnDefinition = "CHAR(32)", nullable = false, unique = true)
    private String subjectId;

    @Column(name = "SUBJECT_NAME", columnDefinition = "VARCHAR(32)", nullable = false)
    private String subjectName;

    // OEM, CPO, MSP, MO, etc -> 한전을 뭐라고 칭할 것인지 표준으로 결정해야 함 
    @Column(name = "SUBJECT_TYPE", columnDefinition = "VARCHAR(3)", nullable = false)
    private String subjectType;

    @Column(name = "SUBJECT_STATUS", columnDefinition = "INT",  nullable = false)
    private int subjectStatus;

    @Column(name = "SUBJECT_DESC", columnDefinition = "VARCHAR(255)")
    private String subjectDesc;

    @Column(name = "SUBJECT_EMAIL", columnDefinition = "VARCHAR(255)")
    private String subjectEmail;

    @Column(name = "SUBJECT_PHONE", columnDefinition = "VARCHAR(32)")
    private String subjectPhone;

    @Column(name = "SUBJECT_PASSWORD", columnDefinition = "CHAR(64)",  nullable = false)
    private String subjectPassword;

    @Column(name = "CREATED_DATE", columnDefinition = "DATE",  nullable = false)
    @CreationTimestamp
    private Date createdDate;

    @Column(name = "UPDATED_DATE", columnDefinition = "DATE")
    @CreationTimestamp
    private Date updatedDate;

    @Column(name = "CREATED_USER", columnDefinition = "CHAR(32)")
    private String createdUser;

    @Column(name = "UPDATED_USER", columnDefinition = "CHAR(32)")
    private String updatedUser;

    @Column(name = "GROUP_ID", columnDefinition = "CHAR(6)")
    private String groupId; 

    @Column(name = "TOKEN_GEN_PERMISSION", columnDefinition = "INT")
    private int tokenGenPermission; 

    @Transient
    private List<PermissionBase> permissionList;

    @ColumnDefault("0")
    @Column(name = "DELETED", columnDefinition = "INT", nullable = false)
    private int deleted;

}
