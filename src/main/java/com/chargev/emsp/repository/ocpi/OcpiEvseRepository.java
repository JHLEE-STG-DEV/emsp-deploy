package com.chargev.emsp.repository.ocpi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chargev.emsp.entity.ocpi.OcpiEvse;
import com.chargev.emsp.entity.ocpi.OcpiEvse.OcpiEvseId;

@Repository
public interface OcpiEvseRepository extends JpaRepository<OcpiEvse, OcpiEvseId> {
}