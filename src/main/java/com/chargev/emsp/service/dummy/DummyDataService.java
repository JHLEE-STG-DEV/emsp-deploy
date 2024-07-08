package com.chargev.emsp.service.dummy;

import java.util.List;

import com.chargev.emsp.model.dto.ocpi.Connector;
import com.chargev.emsp.model.dto.ocpi.EVSE;
import com.chargev.emsp.model.dto.ocpi.Location;
import com.chargev.emsp.model.dto.ocpi.Tariff;

public interface DummyDataService {
    public List<Location> makeLocationList();
    public Location makeLocation();
    public EVSE makeEvse();
    public Connector makeConnector();
    public List<Tariff> makeTariffs();
    public Tariff makeTariff();
}
