package com.chargev.emsp.entity.authenticationentity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "EV_AUTH_PERMISSION_BASE")
@Entity
@Data
public class PermissionBase {
    @Id
    @Column(name = "PERMISSION_ID", columnDefinition = "CHAR(32)", nullable = false, unique = true)
    private String permissionId;

    @Column(name = "PERMISSION_NAME", columnDefinition = "VARCHAR(32)", nullable = false)
    private String permissionName;

    @Column(name = "PERMISSION_DESC", columnDefinition = "VARCHAR(255)")
    private String permissionDesc;

    @Column(name = "PERMISSION_TYPE", columnDefinition = "INT", nullable = false)
    private int permissionType;

    @CreationTimestamp
    @Column(name = "CREATED_DATE", columnDefinition = "DATE", nullable = false)
    private Date createdDate;

    @Column(name = "UPDATED_DATE", columnDefinition = "DATE")
    private Date updatedDate;

    @Column(name = "CREATED_USER", columnDefinition = "CHAR(32)")
    private String createdUser;

    @Column(name = "UPDATED_USER", columnDefinition = "CHAR(32)")
    private String updatedUser;

}
