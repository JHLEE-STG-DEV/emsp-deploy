package com.chargev.emsp.repository.authentication;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.chargev.emsp.entity.authenticationentity.Permission;
import com.chargev.emsp.entity.authenticationentity.PermissionGroupPK;

public interface PermissionRepository extends CrudRepository<Permission, PermissionGroupPK>, PagingAndSortingRepository<Permission, PermissionGroupPK>{
    
}
