package com.chargev.emsp.repository.ocpi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chargev.emsp.entity.ocpi.OcpiLocation;
import com.chargev.emsp.entity.ocpi.OcpiLocation.OcpiLocationId;

@Repository
public interface OcpiLocationRepository extends JpaRepository<OcpiLocation, OcpiLocationId> {
}