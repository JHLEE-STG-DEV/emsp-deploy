package com.chargev.emsp.repository.authentication;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.chargev.emsp.entity.authenticationentity.PermissionBase;
import com.chargev.emsp.entity.authenticationentity.PermissionGroup;

public interface PermissionGroupRepository extends CrudRepository<PermissionGroup, String>, PagingAndSortingRepository<PermissionGroup, String> {
    @Query(value = "SELECT e FROM PermissionBase e JOIN Permission p ON e.permissionId = p.permissionId WHERE p.groupId = :groupId AND e.deleted = 0 AND p.deleted = 0", nativeQuery = false)
    Iterable<PermissionBase> getPermissionByGroup(@Param("groupId") String groupId);
}
