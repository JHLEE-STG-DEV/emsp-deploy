package com.chargev.emsp.service.ocpi;

import java.util.List;

import com.chargev.emsp.entity.poi.PoiMaster;
import com.chargev.emsp.model.dto.ocpi.Location;

public interface LocationService {
    List<PoiMaster> getPoiMaster(String lastUpdated);
    List<Location> updateLastPoi(String startUpdated, String lastUpdated, String oemId);
}
