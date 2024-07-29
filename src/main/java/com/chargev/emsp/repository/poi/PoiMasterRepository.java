package com.chargev.emsp.repository.poi;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.*;

import com.chargev.emsp.entity.poi.PoiMaster;


@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface PoiMasterRepository extends JpaRepository <PoiMaster, PoiMaster.PoiMasterId> {
    @Query(value = "SELECT e FROM PoiMaster e WHERE e.benzYn = 'Y' and e.lastUpdated >= :startUpdated and e.lastUpdated <= :lastUpdated order by lastUpdated", nativeQuery = false)
    List<PoiMaster> getLastData(@Param("startUpdated") String startUpdated, @Param("lastUpdated") String lastUpdated);
}
