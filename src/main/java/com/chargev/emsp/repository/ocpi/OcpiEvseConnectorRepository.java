package com.chargev.emsp.repository.ocpi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chargev.emsp.entity.ocpi.OcpiEvseConnector;
import com.chargev.emsp.entity.ocpi.OcpiEvseConnector.OcpiEvseConnectorId;

@Repository
public interface OcpiEvseConnectorRepository extends JpaRepository<OcpiEvseConnector, OcpiEvseConnectorId> {
}