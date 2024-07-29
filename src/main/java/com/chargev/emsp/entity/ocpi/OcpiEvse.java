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
@Table(name = "OCPI_EVSE")
@Data
public class OcpiEvse {

    @Id
    @Embedded
    private OcpiEvseId id;

    @Column(name = "UID", nullable = false, length = 36)
    private String uid;

    @Column(name = "EVSE_DATA", columnDefinition = "TEXT", nullable = false)
    private String evseData;

    @Column(name = "UPDATED")
    private Date updated;

    @Embeddable
    @Data
    public static class OcpiEvseId  {

        @Column(name = "LOCATION_ID", nullable = false, length = 36)
        private String locationId;

        @Column(name = "OEM_ID", nullable = false, length = 8)
        private String oemId;

        @Column(name = "EVSE_ID", nullable = false, length = 48)
        private String evseId;
    }

}