package com.chargev.emsp.repository.authentication;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.chargev.emsp.entity.authenticationentity.PermissionBase;

public interface PermissionBaseRepository extends CrudRepository<PermissionBase, String>, PagingAndSortingRepository<PermissionBase, String> {
    
}
