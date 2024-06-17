package com.chargev.emsp.service;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.Roles;
import com.chargev.emsp.mapper.RolesMapper;
import com.chargev.emsp.repository.RolesRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogoService {

    private final RolesMapper rolesMapper;
    private final RolesRepository repository;


    public Optional<Roles> getRoleWithMapper(String roleId){
        return Optional.of(rolesMapper.selectRoles(roleId));
    }
    public Optional<Roles> getRoleWithRepo(String roleId){
        return repository.findById(roleId);
    }

    public void saveRoleWithMapper(Roles role){
        
        boolean insert = false;
        if(role.getId() == null){
            insert = true;
        }

        Optional<Roles> targetRole = getRoleWithMapper(role.getId());
        if(targetRole.isEmpty()){
            insert = true;
        }

        if(insert){
            rolesMapper.insertRoles(role);
        }else{
            rolesMapper.updateRoles(targetRole.get());
        }

    }
    public void saveRoleWithRepo(Roles role){
        Optional<Roles> targetRole = getRoleWithRepo(role.getId());
        if(targetRole.isPresent()){
            BeanUtils.copyProperties(role, targetRole.get(), "id");
            repository.save(targetRole.get());
        }else{
            repository.save(role);
        }
    }
}
