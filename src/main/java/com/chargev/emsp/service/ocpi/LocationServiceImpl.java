package com.chargev.emsp.service.ocpi;

import java.util.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.apache.catalina.startup.HostRuleSet;
import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.ocpi.OcpiEvse;
import com.chargev.emsp.entity.ocpi.OcpiEvseConnector;
import com.chargev.emsp.entity.ocpi.OcpiLocation;
import com.chargev.emsp.entity.poi.ConnectorStr;
import com.chargev.emsp.entity.poi.ConnectorTypeStr;
import com.chargev.emsp.entity.poi.Directions;
import com.chargev.emsp.entity.poi.EvseStr;
import com.chargev.emsp.entity.poi.Operator;
import com.chargev.emsp.entity.poi.PoiMaster;
import com.chargev.emsp.model.dto.ocpi.Access;
import com.chargev.emsp.model.dto.ocpi.AdditionalGeoLocation;
import com.chargev.emsp.model.dto.ocpi.BusinessDetails;
import com.chargev.emsp.model.dto.ocpi.Capability;
import com.chargev.emsp.model.dto.ocpi.Connector;
import com.chargev.emsp.model.dto.ocpi.ConnectorFormat;
import com.chargev.emsp.model.dto.ocpi.ConnectorType;
import com.chargev.emsp.model.dto.ocpi.ConnectorTypeCount;
import com.chargev.emsp.model.dto.ocpi.DisplayText;
import com.chargev.emsp.model.dto.ocpi.EVSE;
import com.chargev.emsp.model.dto.ocpi.GeoLocation;
import com.chargev.emsp.model.dto.ocpi.Hotline;
import com.chargev.emsp.model.dto.ocpi.Hours;
import com.chargev.emsp.model.dto.ocpi.Location;
import com.chargev.emsp.model.dto.ocpi.ParkingType;
import com.chargev.emsp.model.dto.ocpi.PowerType;
import com.chargev.emsp.model.dto.ocpi.RegularHours;
import com.chargev.emsp.model.dto.ocpi.Status;
import com.chargev.emsp.repository.ocpi.OcpiEvseConnectorRepository;
import com.chargev.emsp.repository.ocpi.OcpiEvseRepository;
import com.chargev.emsp.repository.ocpi.OcpiLocationRepository;
import com.chargev.emsp.repository.poi.PoiMasterRepository;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final PoiMasterRepository poiMasterRepository;
    private final OcpiLocationRepository ocpiLocationRepository;
    private final OcpiEvseRepository ocpiEvseRepository;
    private final OcpiEvseConnectorRepository ocpiEvseConnectorRepository;
    

    @Override
    public List<PoiMaster> getPoiMaster(String lastUpdated) {
        //return poiMasterRepository.getLastData("20240701");
        return null;
    }

    private List<Capability> getCapabilities(String companyId) {
        List<Capability> capabilities = new ArrayList<>();
        switch (companyId) {
            case "PI", "GN" -> {
                capabilities.add(Capability.RFID_READER);
                capabilities.add(Capability.REMOTE_START_STOP_CAPABLE);
            }
            case "ME", "KP" -> {
                capabilities.add(Capability.RFID_READER);
                capabilities.add(Capability.CREDIT_CARD_PAYABLE);
            }
            case "MG", "CH" -> capabilities.add(Capability.REMOTE_START_STOP_CAPABLE);
            case "KT" -> {
                capabilities.add(Capability.RFID_READER);
                capabilities.add(Capability.T_MONEY);
            }
            default -> capabilities.add(Capability.RFID_READER);
        }
        return capabilities;
    }

    private ConnectorType getConnectorType(String connectorCode) {
        if(connectorCode.equals("AC")) {
            return ConnectorType.IEC_62196_T1;
        }
        else if(connectorCode.equals("AC3")) {
            return ConnectorType.IEC_62196_T2;
        }
        else if(connectorCode.equals("DC")) {
            return ConnectorType.IEC_62196_T1_COMBO;
        }
        else if(connectorCode.equals("CHADEMO")) {
            return ConnectorType.CHADEMO;
        }
        return ConnectorType.UNKNOWN;
    }

    private PowerType getPowerType(String connectorCode) {
        if(connectorCode.equals("AC")) {
            return PowerType.AC_1_PHASE;
        }
        else if(connectorCode.equals("AC3")) {
            return PowerType.AC_3_PHASE;
        }
        else if(connectorCode.equals("DC")) {
            return PowerType.DC;
        }
        else if(connectorCode.equals("CHADEMO")) {
            return PowerType.DC;
        }
        return PowerType.UNKNOWN;
    }

    private int getVoltage(ConnectorStr connector) {
        int hourlyPowerChargeFilling = 0;
        if(connector.getHourlyPowerChargeFilling() != null && !connector.getHourlyPowerChargeFilling().isEmpty() && connector.getHourlyPowerChargeFilling().chars().allMatch(Character::isDigit)) {
            hourlyPowerChargeFilling = Integer.parseInt(connector.getHourlyPowerChargeFilling());
        }

        if( connector.getVoltage() != null && !connector.getVoltage().isEmpty() && connector.getVoltage().chars().allMatch(Character::isDigit)){
            return Integer.parseInt(connector.getVoltage());
        } else {
            if(hourlyPowerChargeFilling >= 3 && hourlyPowerChargeFilling <= 17 ){
                return 220;
            }else if( hourlyPowerChargeFilling == 30){
                return 1000;
            }else if( hourlyPowerChargeFilling >= 50 && hourlyPowerChargeFilling <= 100 ){
                return 500;
            }else if( hourlyPowerChargeFilling >= 110 && hourlyPowerChargeFilling <= 400 ){
                return 1000;
            }else{
                return 0;
            }
        }
    }

    private int getAmperage(ConnectorStr connector) {
        int hourlyPowerChargeFilling = 0;
        if(connector.getHourlyPowerChargeFilling() != null && !connector.getHourlyPowerChargeFilling().isEmpty() && connector.getHourlyPowerChargeFilling().chars().allMatch(Character::isDigit)) {
            hourlyPowerChargeFilling = Integer.parseInt(connector.getHourlyPowerChargeFilling());
        }

        if( connector.getAmperage() != null && !connector.getAmperage().isEmpty() && connector.getAmperage().chars().allMatch(Character::isDigit)){
            return Integer.parseInt(connector.getAmperage());
        } 
        else{
            if(hourlyPowerChargeFilling == 3 ){
                return 16;
            }
            else if(hourlyPowerChargeFilling == 7 ) {
                return 32;
            }
            else if(hourlyPowerChargeFilling == 10){
                return 32;
            }
            else if(hourlyPowerChargeFilling == 11){
                return 45;
            }
            else if(hourlyPowerChargeFilling == 14){
                return 64;
            }
            else if(hourlyPowerChargeFilling == 15){
                return 68;
            }
            else if(hourlyPowerChargeFilling == 17){
                return 77;
            }
            else if(hourlyPowerChargeFilling == 30){
                return 100;
            }
            else if(hourlyPowerChargeFilling == 50){
                return 125;
            }
            else if(hourlyPowerChargeFilling == 60 ){
                return 125;
            }
            else if(hourlyPowerChargeFilling >= 90) {
                return 200;
            }
            else{
                return 0;
            }
        }
    }


    public static Hours convertOpeningTime(String operationTimeType, String openTime, String closeTime) {

        Hours hours = new Hours();
        if (operationTimeType == null || operationTimeType.isEmpty() || operationTimeType.equalsIgnoreCase("S08007")) {
            hours.setTwentyfourseven(false);
            return hours;
        }

        
        int dayOff = 0;
        try {
            dayOff = Integer.parseInt(operationTimeType.substring(4));
        }
        catch (Exception e) {
            dayOff = 0;
        }

        if (operationTimeType.equalsIgnoreCase("S08001")) {
            hours.setTwentyfourseven(true);
            return hours;
        }

        if (operationTimeType.equalsIgnoreCase("S08002")) {
            return createRegularHours(0, openTime, closeTime);
        }

        if (operationTimeType.startsWith("S09") && operationTimeType.length() == 6) {
            if (operationTimeType.equals("S09008")) {
                return createRegularHoursCloseWeekend(openTime, closeTime);
            }
            return createRegularHours(dayOff, openTime, closeTime);
        }

        return null;
    }

    private static Hours createRegularHoursCloseWeekend(String openTime, String closeTime) {

        List<RegularHours> regularHours = new ArrayList<>();
        LocalTime open = null;
        LocalTime close = null;
        try {
            open = LocalTime.parse(openTime);
            close = parseCloseTime(closeTime);
        } catch (Exception e) {
            open = LocalTime.parse("09:00");
            close = parseCloseTime("18:00");
        }

        for (int i = 1; i <= 5; i++) {
            if (open.isAfter(close) && !close.equals(LocalTime.parse("00:00"))) {
                regularHours.add(buildRegularHours(i, openTime, "24:00"));
                regularHours.add(buildRegularHours(i, "00:00", closeTime));
            } else {
                regularHours.add(buildRegularHours(i, openTime, closeTime));
            }
        }
        
        Hours hours = new Hours();
        hours.setTwentyfourseven(false);
        hours.setRegularHours(regularHours);
        return hours;
    }

    private static Hours createRegularHours(int dayOff, String openTime, String closeTime) {

        List<RegularHours> regularHours = new ArrayList<>();
        LocalTime open = null;
        LocalTime close = null;
        try {
            open = LocalTime.parse(openTime);
            close = parseCloseTime(closeTime);
        } catch (Exception e) {
            open = LocalTime.parse("09:00");
            close = parseCloseTime("18:00");
        }


        for (int i = 1; i <= 7; i++) {
            if (i != dayOff) {
                if (open.isAfter(close) && !close.equals(LocalTime.parse("00:00"))) {
                    regularHours.add(buildRegularHours(i, openTime, "24:00"));
                    regularHours.add(buildRegularHours(i, "00:00", closeTime));
                } else {
                    regularHours.add(buildRegularHours(i, openTime, closeTime));
                }
            }
        }

        Hours hours = new Hours();
        hours.setTwentyfourseven(false);
        hours.setRegularHours(regularHours);
        return hours;
    }

    private static LocalTime parseCloseTime(String closeTime) {
        return closeTime.equals("24:00") ? LocalTime.MIDNIGHT : LocalTime.parse(closeTime);
    }

    private static RegularHours buildRegularHours(int weekday, String openTime, String closeTime) {
        RegularHours regularHours = new RegularHours();
        regularHours.setWeekday(weekday);
        regularHours.setPeriodBegin(openTime);
        regularHours.setPeriodEnd(closeTime);
        return regularHours;
    }

    @Override 
    public List<Location> updateLastPoi(String startUpdated, String lastUpdated, String oemId) {
        List<PoiMaster> poiMasters = poiMasterRepository.getLastData(startUpdated, lastUpdated);
        List<Location> locations = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));
        //foreach poiMasters 
        for (PoiMaster poiMaster : poiMasters) {
            Location loc = new Location();

            loc.setCountryCode("KR");
            loc.setPartyId(poiMaster.getId().getLocationId().substring(0, 2)); 
            loc.setId(poiMaster.getId().getLocationId().substring(0, 2) + "-" + poiMaster.getId().getLocationId().substring(2, poiMaster.getId().getLocationId().length()));
            loc.setPublish(true);
            loc.setName(poiMaster.getStationName());
            loc.setAddress(poiMaster.getRoadAddress());
            loc.setCity(poiMaster.getAreaCode());
            loc.setPostalCode(poiMaster.getZipCode());
            loc.setCountry("KOR");
            
            GeoLocation coordinates = new GeoLocation();
            if(poiMaster.getGpsLat() != null && poiMaster.getGpsLng() != null) {
                coordinates.setLatitude(poiMaster.getGpsLat().toString());
                coordinates.setLongitude(poiMaster.getGpsLng().toString());
            }
            loc.setCoordinates(coordinates);
            
            List<EVSE> evses = new ArrayList<>();
            for(EvseStr evseStr : poiMaster.getEvseStr()) {
                EVSE evse = new EVSE();
                evse.setUid(loc.getId() + "-" + evseStr.getChargerNumber());
                evse.setEvseId(loc.getId() + "-" + evseStr.getChargerNumber());
                Status status = Status.fromValue(evseStr.getChargerStatus());
                if(status == null) {
                    evse.setStatus(Status.UNKNOWN);
                } else {
                    evse.setStatus(status);
                }
                evse.setCapabilities(getCapabilities(loc.getPartyId()));
                List<Connector> connectors = new ArrayList<>();
                for(ConnectorStr connectorStr : evseStr.getConnectorStr()) {
                    Connector connector = new Connector();
                    connector.setId(""+connectorStr.getConnectorId());
                    connector.setStandard(getConnectorType(connectorStr.getConnectorCode()));
                    connector.setFormat(ConnectorFormat.CABLE); // 현재 케이블 고정값인데, 소켓 고정값이 존재하는지 체크 필요 
                    connector.setPowerType(getPowerType(connectorStr.getConnectorCode()));
                    connector.setMaxVoltage(getVoltage(connectorStr));
                    connector.setMaxAmperage(getAmperage(connectorStr));
                    int hourlyPowerChargeFilling = 0;
                    if(connectorStr.getHourlyPowerChargeFilling() != null && !connectorStr.getHourlyPowerChargeFilling().isEmpty() && connectorStr.getHourlyPowerChargeFilling().chars().allMatch(Character::isDigit)) {
                        hourlyPowerChargeFilling = Integer.parseInt(connectorStr.getHourlyPowerChargeFilling());
                    }                    
                    connector.setMaxElectricPower(hourlyPowerChargeFilling * 1000);
                    if(connectorStr.getTariffId().trim().equals("")) {
                        connector.setTariffIds(Collections.emptyList());    
                    }
                    else {
                        List<String> tariffIds = connectorStr.getTariffId().contains(",") ? Arrays.asList(connectorStr.getTariffId().split(",")) : List.of(connectorStr.getTariffId());
                        List<String> tariffIdList = new ArrayList<>();
                        for(String tariffId : tariffIds) {
                            boolean hasTarriId = false;
                            for(String id : tariffIdList) {
                                if(id.equals(tariffId)) {
                                    hasTarriId = true;
                                    break;
                                }
                            }
                            if(!hasTarriId) {
                                tariffIdList.add(tariffId);
                            }
                        }
                        connector.setTariffIds(tariffIdList);    
                    }
                    Instant instant = connectorStr.getModifiedDt().toInstant();
                    connector.setLastUpdated(formatter.format(instant));

                    OcpiEvseConnector ocpiEvseConnector = new OcpiEvseConnector();
                    OcpiEvseConnector.OcpiEvseConnectorId ocpiEvseConnectorId = new OcpiEvseConnector.OcpiEvseConnectorId();
                    ocpiEvseConnectorId.setLocationId(loc.getId());
                    ocpiEvseConnectorId.setOemId(oemId);
                    ocpiEvseConnectorId.setEvseId(evse.getEvseId());
                    ocpiEvseConnectorId.setConnectorId(connector.getId());
                    ocpiEvseConnector.setId(ocpiEvseConnectorId);
                    ocpiEvseConnector.setUid(evse.getUid());
                    ObjectMapper objectMapperForConnector = new ObjectMapper();
                    try {
                        // Convert Java object to JSON string
                        String jsonString = objectMapperForConnector.writeValueAsString(connector);
                        ocpiEvseConnector.setConnectorData(jsonString);
                        ocpiEvseConnector.setUpdated(new Date());
                        ocpiEvseConnectorRepository.save(ocpiEvseConnector);
                    } catch (JsonProcessingException e) {
                        //e.printStackTrace();
                    }

                    connectors.add(connector);
                }
                evse.setConnectors(connectors);
                Access access = Access.fromValue(evseStr.getAccess());
                if(access == null) {
                    evse.setAccess(Access.UNKNOWN);
                }
                evse.setAccess(access);
                evse.setPhysicalReference(evseStr.getPhysicalReference());
                evse.setPaymentMethods(List.of("CONTRACT")); // 단일 값으로 정의됨 
                
                Instant instant2 = evseStr.getModifiedDt().toInstant();
                evse.setLastUpdated(formatter.format(instant2));
                List<DisplayText> displayTexts = new ArrayList<>();
                if(evseStr.getDirections() != null) {
                    Directions direction = evseStr.getDirections();
                    DisplayText displayText = new DisplayText();
                    displayText.setLanguage(direction.getLanguage());
                    displayText.setText(direction.getText());
                    displayTexts.add(displayText);
                }
                evse.setDirections(displayTexts);

                // result: OCPIEvseRepository.save(evse);
                OcpiEvse ocpiEvse = new OcpiEvse();
                OcpiEvse.OcpiEvseId ocpiEvseId = new OcpiEvse.OcpiEvseId();
                ocpiEvseId.setLocationId(loc.getId());
                ocpiEvseId.setOemId(oemId);
                ocpiEvseId.setEvseId(evse.getEvseId());
                ocpiEvse.setId(ocpiEvseId);
                ocpiEvse.setUid(evse.getUid());
                ObjectMapper objectMapperForEvse = new ObjectMapper();
                try {
                    // Convert Java object to JSON string
                    String jsonString = objectMapperForEvse.writeValueAsString(evse);
                    ocpiEvse.setEvseData(jsonString);
                    ocpiEvse.setUpdated(new Date());
                    ocpiEvseRepository.save(ocpiEvse);
                } catch (JsonProcessingException e) {
                    //e.printStackTrace();
                }
                evses.add(evse);
            }
            loc.setEvses(evses);
            // poi_master의 direction 은 들어오지 않음
            List<DisplayText> directions = new ArrayList<>();
            DisplayText globalDirections = new DisplayText();
            globalDirections.setLanguage("ko");
            globalDirections.setText(poiMaster.getStationDescript());
            directions.add(globalDirections);
            loc.setDirections(directions); 
            ParkingType parkingType = ParkingType.fromValue(poiMaster.getCategory());
            if(parkingType == null) {
                parkingType = ParkingType.UNKNOWN;
            }
            loc.setParkingType(parkingType);
            BusinessDetails operator = new BusinessDetails();
            Operator op = poiMaster.getOperator();
            Hotline hotline = new Hotline();
            if(op != null) {
                operator.setName(op.getName());
                operator.setWebsite(op.getWebsite());
                operator.setLogo(op.getLogo());
                hotline.setPhoneNumber(op.getPhoneNumber());
            }
            loc.setOperator(operator);
            loc.setHotline(hotline);

            if(poiMaster.getEntryGpsLat() != null && poiMaster.getEntryGpsLng() != null) {
                AdditionalGeoLocation relatedLocation = new AdditionalGeoLocation();
                relatedLocation.setLatitude(poiMaster.getEntryGpsLat().toString());
                relatedLocation.setLongitude(poiMaster.getEntryGpsLng().toString());
                DisplayText relatedLocationName = new DisplayText();
                relatedLocationName.setLanguage("ko");
                relatedLocationName.setText("입구");
                relatedLocation.setName(relatedLocationName);
                loc.setRelatedLocations(List.of(relatedLocation));
            }
            else {
                loc.setRelatedLocations(Collections.emptyList());
            }

            loc.setTimeZone("Asia/Seoul");
            
            Hours openingTimes = new Hours();
            openingTimes.setTwentyfourseven(true);
            loc.setOpeningTimes(convertOpeningTime(poiMaster.getOperationTimeType(), poiMaster.getOpenTime(), poiMaster.getCloseTime()));
            loc.setChargingWhenClosed(false);
            List<ConnectorTypeCount> connectorTypeCounts = new ArrayList<>();
            for(ConnectorTypeStr connectorStr : poiMaster.getConncetTypeStr()) {
                ConnectorTypeCount connectorTypeCount = new ConnectorTypeCount();

                connectorTypeCount.setStandard(getConnectorType(connectorStr.getStandard()));
                connectorTypeCount.setCount(1);
                connectorTypeCounts.add(connectorTypeCount);
            }
            loc.setConnectorTypeCounts(connectorTypeCounts);
            
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            if(poiMaster.getLastUpdated() != null && poiMaster.getLastUpdated().length() == 14) {
                LocalDateTime localDateTime = LocalDateTime.parse(poiMaster.getLastUpdated(), inputFormatter);
                ZoneId localZoneId = ZoneId.systemDefault();
        
                ZonedDateTime localZonedDateTime = localDateTime.atZone(localZoneId);
        
                ZonedDateTime gmtZonedDateTime = localZonedDateTime.withZoneSameInstant(ZoneId.of("GMT"));
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                                                    .withZone(ZoneId.of("GMT"));
                String formattedDate = outputFormatter.format(gmtZonedDateTime);        
                loc.setLastUpdated(formattedDate);

                ObjectMapper objectMapper = new ObjectMapper();
                
                try {
                    // Convert Java object to JSON string
                    String jsonString = objectMapper.writeValueAsString(loc);
                    OcpiLocation ocpiLocation = new OcpiLocation();
                    OcpiLocation.OcpiLocationId ocpilocationId = new OcpiLocation.OcpiLocationId();
                    ocpilocationId.setLocationId(loc.getId());
                    ocpilocationId.setOemId(oemId);
                    ocpiLocation.setId(ocpilocationId);
                    ocpiLocation.setOcpiData(jsonString);
                    ocpiLocation.setLastUpdated(poiMaster.getLastUpdated());
                    ocpiLocation.setUpdatedDate(new Date());
                    ocpiLocation.setStationName(loc.getName());
                    ocpiLocation.setZipCode(loc.getPostalCode());

                    OcpiLocation loc1 = ocpiLocationRepository.save(ocpiLocation);
                    log.info(oemId + " " + loc1.getId().getLocationId() + " " + loc1.getLastUpdated());
                    
                } catch (JsonProcessingException e) {
                    log.info(e.getMessage());
                }
                // Sleep 1 sec
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    log.info(e.getMessage());
                }

                //locations.add(loc);
            }
        }
        return locations;

    }
}
