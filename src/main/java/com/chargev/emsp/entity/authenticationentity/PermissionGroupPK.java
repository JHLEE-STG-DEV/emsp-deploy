package com.chargev.emsp.entity.authenticationentity;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class PermissionGroupPK {
    @Column(name = "PERMISSION_ID")
    private String permissionId;
    @Column(name = "GROUP_ID")
    private String groupId;
}
