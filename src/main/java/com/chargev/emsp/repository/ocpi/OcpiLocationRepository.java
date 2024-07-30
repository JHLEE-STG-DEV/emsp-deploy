package com.chargev.emsp.repository.ocpi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.chargev.emsp.entity.ocpi.OcpiLocation;
import com.chargev.emsp.entity.ocpi.OcpiLocation.OcpiLocationId;
import java.util.List;

@Repository
public interface OcpiLocationRepository extends JpaRepository<OcpiLocation, OcpiLocationId> {
    @Query(value = "SELECT * FROM OCPI_LOCATION l WHERE l.LASTUPDATED BETWEEN :startDate AND :endDate AND l.OEM_ID = :oemId ORDER BY l.LASTUPDATED LIMIT :size OFFSET :offset", nativeQuery = true)
    List<OcpiLocation> findByDate(@Param("startDate") String startDate,  @Param("endDate") String endDate, @Param("oemId") String oemId, @Param("size") int size, @Param("offset") int offset);
}