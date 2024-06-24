package com.chargev.emsp.entity.authenticationentity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "EV_AUTH_PERMISSION")
@Entity
@Data

@IdClass(PermissionGroupPK.class)
public class Permission {
    @Id
//	@ManyToOne 
//    @JoinTable(name = "EV_AUTH_PERMISSION_BASE", joinColumns = @JoinColumn(name = "PERMISSION_ID"), inverseJoinColumns = @JoinColumn(name = "PERMISSION_ID"))
    @Column(name = "PERMISSION_ID", columnDefinition = "VARCHAR(32)", nullable = false)
    private String permissionId;

    @Id
//   @ManyToOne(optional = false)
//    @JoinTable(name = "EV_AUTH_PERMISSION_GROUP", joinColumns = @JoinColumn(name = "GROUP_ID"), inverseJoinColumns = @JoinColumn(name = "GROUP_ID"))
    @Column(name = "GROUP_ID", columnDefinition = "CHAR(6)", nullable = false)
    private String groupId;

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

