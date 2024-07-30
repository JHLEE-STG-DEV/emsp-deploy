package com.chargev.emsp.service.ocpi;

import java.util.List;

import com.chargev.emsp.entity.ocpi.OcpiEvse;
import com.chargev.emsp.entity.ocpi.OcpiEvseConnector;
import com.chargev.emsp.entity.ocpi.OcpiLocation;
import com.chargev.emsp.entity.poi.PoiMaster;
import com.chargev.emsp.model.dto.ocpi.Location;

public interface LocationService {
    List<PoiMaster> getPoiMaster(String lastUpdated);
    List<Location> updateLastPoi(String startUpdated, String lastUpdated, String oemId);
    List<OcpiLocation> getLocationByDate(String startUpdated, String lastUpdated, String oemId, int size, int offset);
    OcpiLocation getLocationById(OcpiLocation.OcpiLocationId id);
    OcpiEvse getEvseByLocationAndId(String locationId, String uid, String oemId);
    OcpiEvseConnector getEvseConnectorByLocationAndId(String locationId, String uid, String connectorId, String oemId);
}
