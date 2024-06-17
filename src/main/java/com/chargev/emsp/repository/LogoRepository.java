package com.chargev.emsp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.Logo;

public interface LogoRepository extends JpaRepository<Logo, String>{
    
}
