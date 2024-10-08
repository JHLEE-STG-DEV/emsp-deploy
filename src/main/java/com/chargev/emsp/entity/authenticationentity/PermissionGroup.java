package com.chargev.emsp.entity.authenticationentity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "EV_AUTH_PERMISSION_GROUP")
@Entity
@Data

public class PermissionGroup {
    // GROUP ID = SUBJECT_TYPE(3) + CODE(3)
    @Id
    @Column(name = "GROUP_ID", columnDefinition = "CHAR(6)", nullable = false, unique = true)
    private String groupId;

    @Column(name = "GROUP_NAME", columnDefinition = "VARCHAR(32)", nullable = false)
    private String groupName;

    @Column(name = "GROUP_DESCRIPTION", columnDefinition = "VARCHAR(255)")
    private String groupDescription;

    // 관리자 그룹, 사용자 그룹 등을 다시 분리 처리함 
    @Column(name = "GROUP_TYPE", columnDefinition = "INT", nullable = false)
    private int groupType;

    @CreationTimestamp
    @Column(name = "CREATED_DATE", columnDefinition = "DATE", nullable = false)
    private Date createdDate;

    @Column(name = "UPDATED_DATE", columnDefinition = "DATE")
    private Date updatedDate;

    @Column(name = "CREATED_USER", columnDefinition = "CHAR(32)")
    private String createdUser;

    @Column(name = "UPDATED_USER", columnDefinition = "CHAR(32)")
    private String updatedUser;    

    @Column(name = "DELETED", columnDefinition = "INT", nullable = false)
    private Integer deleted;    

}
