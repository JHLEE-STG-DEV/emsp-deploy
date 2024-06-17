package com.chargev.emsp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.chargev.emsp.entity.Roles;

@Mapper
public interface RolesMapper {

    Roles selectRoles(@Param("id") String id);

    void insertRoles(Roles roles);
    void updateRoles(Roles roles);
    void deleteRoles(@Param("id") Long id);
}
