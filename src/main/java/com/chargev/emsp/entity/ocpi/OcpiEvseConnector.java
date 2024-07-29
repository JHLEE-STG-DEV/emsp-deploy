package com.chargev.emsp.entity.ocpi;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "OCPI_EVSE_CONNECTOR")
@Data
public class OcpiEvseConnector {

    @Id
    @Embedded
    private OcpiEvseConnectorId id;

    @Column(name = "UPDATED")
    private Date updated;

    @Column(name = "UID", nullable = false, length = 36)
    private String uid;

    @Column(name = "CONNECTOR_DATA", columnDefinition = "TEXT", nullable = false)
    private String connectorData;

    @Embeddable
    @Data
    public static class OcpiEvseConnectorId  {

        @Column(name = "LOCATION_ID", nullable = false, length = 36)
        private String locationId;

        @Column(name = "OEM_ID", nullable = false, length = 8)
        private String oemId;

        @Column(name = "EVSE_ID", length = 48)
        private String evseId;

        @Column(name = "CONNECTOR_ID", nullable = false, length = 36)
        private String connectorId;
    
    }

}