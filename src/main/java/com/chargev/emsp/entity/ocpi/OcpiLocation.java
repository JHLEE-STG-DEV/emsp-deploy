package com.chargev.emsp.entity.ocpi;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "OCPI_LOCATION")
@Data
public class OcpiLocation {

    @EmbeddedId
    private OcpiLocationId id;

    @Column(name = "OCPIDATA", columnDefinition = "TEXT", nullable = false)
    private String ocpiData;

    @Column(name = "LASTUPDATED", length = 14, nullable = false)
    private String lastUpdated;

    @Column(name = "UPDATEDDATE", nullable = false)
    private Date updatedDate;

    @Column(name = "STATION_NAME", length = 100, nullable = false)
    private String stationName;

    @Column(name = "ZIP_CODE", length = 5)
    private String zipCode;

    @Embeddable
    @Data
    public static class OcpiLocationId  {

        @Column(name = "LOCATION_ID", nullable = false, length = 36)
        private String locationId;

        @Column(name = "OEM_ID", nullable = false, length = 8)
        private String oemId;
    }
}