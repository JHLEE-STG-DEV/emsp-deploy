package com.chargev.emsp.service.dummy;

import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.util.StringList;
import org.springframework.stereotype.Service;

import com.chargev.emsp.model.dto.ocpi.Access;
import com.chargev.emsp.model.dto.ocpi.AdditionalGeoLocation;
import com.chargev.emsp.model.dto.ocpi.AuthMethod;
import com.chargev.emsp.model.dto.ocpi.BusinessDetails;
import com.chargev.emsp.model.dto.ocpi.Capability;
import com.chargev.emsp.model.dto.ocpi.Cdr;
import com.chargev.emsp.model.dto.ocpi.CdrDimension;
import com.chargev.emsp.model.dto.ocpi.CdrDimensionType;
import com.chargev.emsp.model.dto.ocpi.CdrForMb;
import com.chargev.emsp.model.dto.ocpi.CdrLocation;
import com.chargev.emsp.model.dto.ocpi.CdrLocationForMb;
import com.chargev.emsp.model.dto.ocpi.CdrToken;
import com.chargev.emsp.model.dto.ocpi.ChargingPeriod;
import com.chargev.emsp.model.dto.ocpi.Connector;
import com.chargev.emsp.model.dto.ocpi.ConnectorFormat;
import com.chargev.emsp.model.dto.ocpi.ConnectorType;
import com.chargev.emsp.model.dto.ocpi.ConnectorTypeCount;
import com.chargev.emsp.model.dto.ocpi.DisplayText;
import com.chargev.emsp.model.dto.ocpi.EVSE;
import com.chargev.emsp.model.dto.ocpi.Element;
import com.chargev.emsp.model.dto.ocpi.EnergyMix;
import com.chargev.emsp.model.dto.ocpi.EnergySource;
import com.chargev.emsp.model.dto.ocpi.EnergySourceCategory;
import com.chargev.emsp.model.dto.ocpi.EnvironmentImpact;
import com.chargev.emsp.model.dto.ocpi.EnvironmentalImpactCategory;
import com.chargev.emsp.model.dto.ocpi.GeoLocation;
import com.chargev.emsp.model.dto.ocpi.Hotline;
import com.chargev.emsp.model.dto.ocpi.Hours;
import com.chargev.emsp.model.dto.ocpi.Location;
import com.chargev.emsp.model.dto.ocpi.ParkingType;
import com.chargev.emsp.model.dto.ocpi.PowerType;
import com.chargev.emsp.model.dto.ocpi.Price;
import com.chargev.emsp.model.dto.ocpi.PriceComponent;
import com.chargev.emsp.model.dto.ocpi.RegularHours;
import com.chargev.emsp.model.dto.ocpi.Restrictions;
import com.chargev.emsp.model.dto.ocpi.Session;
import com.chargev.emsp.model.dto.ocpi.SessionForMb;
import com.chargev.emsp.model.dto.ocpi.SessionStatus;
import com.chargev.emsp.model.dto.ocpi.Status;
import com.chargev.emsp.model.dto.ocpi.Tariff;
import com.chargev.emsp.model.dto.ocpi.TariffDimensionType;
import com.chargev.emsp.model.dto.ocpi.TariffType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DummyDataServiceImpl implements DummyDataService  {
    @Override
    public List<Location> makeLocationList() {
        List<Location> locations = new ArrayList<>();

        // 첫 번째 Location 객체 생성
        Location l1 = new Location();
        l1.setCountry_code("KR");
        l1.setParty_id("CEV");
        l1.setId("PI-200006");
        l1.setPublish(true);
        l1.setName("서울시 이마트에브리데이이문점");
        l1.setAddress("이문로 136");
        l1.setCity("서울특별시 동대문구");
        l1.setPostal_code("02418");
        l1.setCountry("KOR");
            GeoLocation l1Coordinates = new GeoLocation();
            l1Coordinates.setLatitude("37.598480");
            l1Coordinates.setLongitude("127.061874");
        l1.setCoordinates(l1Coordinates);
            List<EVSE> l1Evses = new ArrayList<>();
                EVSE l1Evse1 = new EVSE();
                l1Evse1.setUid("PI-200006-2111");
                l1Evse1.setEvse_id("PI-200006-2111");
                l1Evse1.setStatus(Status.AVAILABLE);
                    List<Capability> l1EvseCapa = new ArrayList<>();
                    l1EvseCapa.add(Capability.RFID_READER);
                    l1EvseCapa.add(Capability.REMOTE_START_STOP_CAPABLE);
                l1Evse1.setCapabilities(l1EvseCapa);
                    Connector l1Evse1Connector1 = new Connector();
                    l1Evse1Connector1.setId("1");
                    l1Evse1Connector1.setStandard(ConnectorType.IEC_62196_T1);
                    l1Evse1Connector1.setFormat(ConnectorFormat.CABLE);
                    l1Evse1Connector1.setPower_type(PowerType.AC_1_PHASE);
                    l1Evse1Connector1.setMax_voltage(220);
                    l1Evse1Connector1.setMax_amperage(32);
                    l1Evse1Connector1.setMax_electric_power(7000);
                    l1Evse1Connector1.setTariff_ids(List.of("CG202312220001-1,CG202312220001-1"));
                    l1Evse1Connector1.setLast_updated("2024-05-13T20:00:02.000Z");
                l1Evse1.setConnectors(List.of(l1Evse1Connector1));
                l1Evse1.setAccess(Access.SEMIPUBLIC);
                l1Evse1.setPhysical_reference("JC6111B");
                l1Evse1.setPayment_methods(List.of("CONTRACT"));
                l1Evse1.setLast_updated("2024-05-13T20:00:02.000Z");
            l1Evses.add(l1Evse1);
        l1.setEvses(l1Evses);
            List<DisplayText> l1Directions = new ArrayList<>();
                DisplayText l1Direction1 = new DisplayText();
                l1Direction1.setLanguage("ko");
                l1Direction1.setText("마트(쇼핑몰)");
            l1Directions.add(l1Direction1);
        l1.setDirections(l1Directions);
        l1.setParking_type(ParkingType.PARKING_LOT);
            BusinessDetails l1Operator = new BusinessDetails();
            // l1Operator.setId("PI");
            l1Operator.setName("차지비");
            l1Operator.setWebsite("https://www.chargev.co.kr/customer-support/charging_fee");
            // l1Operator.setPhone_number("1600-4047");
        l1.setOperator(l1Operator);
            Hotline l1Hotline = new Hotline();
            l1Hotline.setPhone_number("1600-4047");
        l1.setHotline(l1Hotline);
            List<AdditionalGeoLocation> l1RelatedLocations = new ArrayList<>();
                AdditionalGeoLocation l1RelatedLocation1 = new AdditionalGeoLocation();
                l1RelatedLocation1.setLatitude("37.598586");
                l1RelatedLocation1.setLongitude("127.061732");
                    DisplayText l1RelatedLocation1Name = new DisplayText();
                    l1RelatedLocation1Name.setLanguage("ko");
                    l1RelatedLocation1Name.setText("입구");
                l1RelatedLocation1.setName(l1RelatedLocation1Name);
            l1RelatedLocations.add(l1RelatedLocation1);
        l1.setRelated_locations(l1RelatedLocations);
        l1.setTime_zone("Asia/Seoul");
            Hours l1OpeningTimes = new Hours();
            l1OpeningTimes.setTwentyfourseven(true);
        l1.setOpening_times(l1OpeningTimes);
        l1.setCharging_when_closed(false);
            List<ConnectorTypeCount> l1ConnectorTypeCounts = new ArrayList<>();
                ConnectorTypeCount l1ConnectorTypeCount1 = new ConnectorTypeCount();
                l1ConnectorTypeCount1.setStandard("IEC_62196_T1");
                l1ConnectorTypeCount1.setCount(1);
                l1ConnectorTypeCounts.add(l1ConnectorTypeCount1);
            l1.setConnector_type_counts(l1ConnectorTypeCounts);
        l1.setLast_updated("2024-05-13T20:00:02.000Z");
        locations.add(l1);

        // 두 번째 Location 객체 생성
        Location l2 = new Location();
        l2.setCountry_code("KR");
        l2.setParty_id("CEV");
        l2.setId("PI-200009");
        l2.setPublish(true);
        l2.setName("서울시 이마트(월계점)");
        l2.setAddress("마들로3길 15");
        l2.setCity("서울특별시 노원구");
        l2.setPostal_code("01906");
        l2.setCountry("KOR");
            GeoLocation l2Coordinates = new GeoLocation();
            l2Coordinates.setLatitude("37.626421");
            l2Coordinates.setLongitude("127.061997");
        l2.setCoordinates(l2Coordinates);
            List<EVSE> l2Evses = new ArrayList<>();
                EVSE l2Evse1 = new EVSE();
                l2Evse1.setUid("PI-200009-2111");
                l2Evse1.setStatus(Status.AVAILABLE);
                    List<Capability> l2EvseCapa = new ArrayList<>();
                    l2EvseCapa.add(Capability.RFID_READER);
                    l2EvseCapa.add(Capability.REMOTE_START_STOP_CAPABLE);
                l2Evse1.setCapabilities(l2EvseCapa);
                    Connector l2Evse1Connector1 = new Connector();
                    l2Evse1Connector1.setId("1");
                    l1Evse1Connector1.setStandard(ConnectorType.IEC_62196_T1);
                    l1Evse1Connector1.setFormat(ConnectorFormat.CABLE);
                    l1Evse1Connector1.setPower_type(PowerType.AC_1_PHASE);
                    l1Evse1Connector1.setMax_voltage(220);
                    l1Evse1Connector1.setMax_amperage(32);
                    l1Evse1Connector1.setMax_electric_power(7000);
                    l1Evse1Connector1.setTariff_ids(List.of("CG202312220001-1,CG202312220001-1"));
                    l1Evse1Connector1.setLast_updated("2024-05-13T20:00:02.000Z");
                l2Evse1.setConnectors(List.of(l2Evse1Connector1));
                l2Evse1.setAccess(Access.SEMIPUBLIC);
                l2Evse1.setEvse_id("PI-200009-2111");
                l2Evse1.setPhysical_reference("JC6111B");
                l2Evse1.setPayment_methods(List.of("CONTRACT"));
                l2Evse1.setLast_updated("2024-05-13T20:00:02.000Z");
                EVSE l2Evse2 = new EVSE();
                l2Evse2.setUid("PI-200009-2112");
                l2Evse2.setStatus(Status.AVAILABLE);
                    l2EvseCapa = new ArrayList<>();
                    l2EvseCapa.add(Capability.RFID_READER);
                    l2EvseCapa.add(Capability.REMOTE_START_STOP_CAPABLE);
                l2Evse2.setCapabilities(l2EvseCapa);
                    Connector l2Evse2Connector1 = new Connector();
                    l2Evse2Connector1.setId("1");
                    l2Evse2Connector1.setStandard(ConnectorType.IEC_62196_T1);
                    l2Evse2Connector1.setFormat(ConnectorFormat.CABLE);
                    l2Evse2Connector1.setPower_type(PowerType.AC_1_PHASE);
                    l2Evse2Connector1.setMax_voltage(220);
                    l2Evse2Connector1.setMax_amperage(32);
                    l2Evse2Connector1.setMax_electric_power(7000);
                    l2Evse2Connector1.setTariff_ids(List.of("CG202312220001-1,CG202312220001-1"));
                    l2Evse2Connector1.setLast_updated("2024-05-13T20:00:02.000Z");
                l2Evse2.setConnectors(List.of(l2Evse2Connector1));
                l2Evse2.setAccess(Access.SEMIPUBLIC);
                l2Evse2.setEvse_id("PI-200009-2112");
                l2Evse2.setPhysical_reference("JC6111B");
                l2Evse2.setPayment_methods(List.of("CONTRACT"));
                l2Evse2.setLast_updated("2024-05-13T20:00:02.000Z");
            l2Evses.add(l2Evse1);
            l2Evses.add(l2Evse2);
        l2.setEvses(l2Evses);
            List<DisplayText> l2Directions = new ArrayList<>();
                DisplayText l2Direction1 = new DisplayText();
                l2Direction1.setLanguage("ko");
                l2Direction1.setText("마트(쇼핑몰)");
            l2Directions.add(l2Direction1);
        l2.setDirections(l2Directions);
        l2.setParking_type(ParkingType.PARKING_LOT);
            BusinessDetails l2Operator = new BusinessDetails();
            // l2Operator.setId("PI");
            l2Operator.setName("차지비");
            l2Operator.setWebsite("https://www.chargev.co.kr/customer-support/charging_fee");
            // l2Operator.setPhone_number("1600-4047");
        l2.setOperator(l2Operator);
            Hotline l2Hotline = new Hotline();
            l2Hotline.setPhone_number("1600-4047");
        l2.setHotline(l2Hotline);
        List<AdditionalGeoLocation> l2RelatedLocations = new ArrayList<>();
        AdditionalGeoLocation l2RelatedLocation1 = new AdditionalGeoLocation();
        l2RelatedLocation1.setLatitude("37.627114");
        l2RelatedLocation1.setLongitude("127.062254");
        DisplayText l2RelatedLocation1Name = new DisplayText();
        l2RelatedLocation1Name.setLanguage("ko");
        l2RelatedLocation1Name.setText("입구");
        l2RelatedLocation1.setName(l2RelatedLocation1Name);
        l2RelatedLocations.add(l2RelatedLocation1);
        l2.setRelated_locations(l2RelatedLocations);
        l2.setTime_zone("Asia/Seoul");
        Hours l2OpeningTimes = new Hours();
        l2OpeningTimes.setTwentyfourseven(true);
        l2.setOpening_times(l2OpeningTimes);
        l2.setCharging_when_closed(false);
        List<ConnectorTypeCount> l2ConnectorTypeCounts = new ArrayList<>();
        ConnectorTypeCount l2ConnectorTypeCount1 = new ConnectorTypeCount();
        l2ConnectorTypeCount1.setStandard("IEC_62196_T1");
        l2ConnectorTypeCount1.setCount(2);
        l2ConnectorTypeCounts.add(l2ConnectorTypeCount1);
        l2.setConnector_type_counts(l2ConnectorTypeCounts);
        l2.setLast_updated("2024-05-13T20:00:02.000Z");
        locations.add(l2);

        // 세 번째 Location 객체 생성
        Location l3 = new Location();
        l3.setCountry_code("KR");
        l3.setParty_id("CEV");
        l3.setId("PI-200012");
        l3.setPublish(true);
        l3.setName("서울시 이마트(상봉점)");
        l3.setAddress("상봉로 118");
        l3.setCity("서울특별시 중랑구");
        l3.setPostal_code("02169");
        l3.setCountry("KOR");
        GeoLocation l3Coordinates = new GeoLocation();
        l3Coordinates.setLatitude("37.596466");
        l3Coordinates.setLongitude("127.093609");
        l3.setCoordinates(l3Coordinates);
        List<EVSE> l3Evses = new ArrayList<>();
        EVSE l3Evse1 = new EVSE();
        l3Evse1.setUid("PI-200012-2111");
        l3Evse1.setStatus(Status.UNKNOWN);
        List<Capability> l3EvseCapa = new ArrayList<>();
        l3EvseCapa.add(Capability.RFID_READER);
        l3EvseCapa.add(Capability.REMOTE_START_STOP_CAPABLE);
        l3Evse1.setCapabilities(l3EvseCapa);
        Connector l3Evse1Connector1 = new Connector();
        l3Evse1Connector1.setId("1");
        l3Evse1Connector1.setStandard(ConnectorType.IEC_62196_T1);
        l3Evse1Connector1.setFormat(ConnectorFormat.CABLE);
        l3Evse1Connector1.setPower_type(PowerType.AC_1_PHASE);
        l3Evse1Connector1.setMax_voltage(220);
        l3Evse1Connector1.setMax_amperage(32);
        l3Evse1Connector1.setMax_electric_power(7000);
        l3Evse1Connector1.setTariff_ids(List.of("CG202312220001-1,CG202312220001-1"));
        l3Evse1Connector1.setLast_updated("2024-05-13T20:00:02.000Z");
        l3Evse1.setConnectors(List.of(l3Evse1Connector1));
        l3Evse1.setAccess(Access.SEMIPUBLIC);
        l3Evse1.setEvse_id("PI-200012-2111");
        l3Evse1.setPhysical_reference("JC6111B");
        l3Evse1.setPayment_methods(List.of("CONTRACT"));
        l3Evse1.setLast_updated("2024-05-13T20:00:02.000Z");
        l3Evses.add(l3Evse1);
        l3.setEvses(l3Evses);
        List<DisplayText> l3Directions = new ArrayList<>();
        DisplayText l3Direction1 = new DisplayText();
        l3Direction1.setLanguage("ko");
        l3Direction1.setText("마트(쇼핑몰)");
        l3Directions.add(l3Direction1);
        l3.setDirections(l3Directions);
        l3.setParking_type(ParkingType.PARKING_LOT);
        BusinessDetails l3Operator = new BusinessDetails();
        // l3Operator.setId("PI");
        l3Operator.setName("차지비");
        l3Operator.setWebsite("https://www.chargev.co.kr/customer-support/charging_fee");
        // l3Operator.setPhone_number("1600-4047");
        l3.setOperator(l3Operator);
        Hotline l3Hotline = new Hotline();
        l3Hotline.setPhone_number("1600-4047");
        l3.setHotline(l3Hotline);
        List<AdditionalGeoLocation> l3RelatedLocations = new ArrayList<>();
        AdditionalGeoLocation l3RelatedLocation1 = new AdditionalGeoLocation();
        l3RelatedLocation1.setLatitude("37.596377");
        l3RelatedLocation1.setLongitude("127.093403");
        DisplayText l3RelatedLocation1Name = new DisplayText();
        l3RelatedLocation1Name.setLanguage("ko");
        l3RelatedLocation1Name.setText("입구");
        l3RelatedLocation1.setName(l3RelatedLocation1Name);
        l3RelatedLocations.add(l3RelatedLocation1);
        l3.setRelated_locations(l3RelatedLocations);
        l3.setTime_zone("Asia/Seoul");
        Hours l3OpeningTimes = new Hours();
        l3OpeningTimes.setTwentyfourseven(false);
        List<RegularHours> l3RegularHours = new ArrayList<>();
        RegularHours l3RegularHour1 = new RegularHours();
        l3RegularHour1.setWeekday(1);
        l3RegularHour1.setPeriod_begin("10:00");
        l3RegularHour1.setPeriod_end("24:00");
        l3RegularHours.add(l3RegularHour1);
        RegularHours l3RegularHour2 = new RegularHours();
        l3RegularHour2.setWeekday(2);
        l3RegularHour2.setPeriod_begin("10:00");
        l3RegularHour2.setPeriod_end("24:00");
        l3RegularHours.add(l3RegularHour2);
        RegularHours l3RegularHour3 = new RegularHours();
        l3RegularHour3.setWeekday(3);
        l3RegularHour3.setPeriod_begin("10:00");
        l3RegularHour3.setPeriod_end("24:00");
        l3RegularHours.add(l3RegularHour3);
        RegularHours l3RegularHour4 = new RegularHours();
        l3RegularHour4.setWeekday(4);
        l3RegularHour4.setPeriod_begin("10:00");
        l3RegularHour4.setPeriod_end("24:00");
        l3RegularHours.add(l3RegularHour4);
        RegularHours l3RegularHour5 = new RegularHours();
        l3RegularHour5.setWeekday(5);
        l3RegularHour5.setPeriod_begin("10:00");
        l3RegularHour5.setPeriod_end("24:00");
        l3RegularHours.add(l3RegularHour5);
        RegularHours l3RegularHour6 = new RegularHours();
        l3RegularHour6.setWeekday(6);
        l3RegularHour6.setPeriod_begin("10:00");
        l3RegularHour6.setPeriod_end("24:00");
        l3RegularHours.add(l3RegularHour6);
        RegularHours l3RegularHour7 = new RegularHours();
        l3RegularHour7.setWeekday(7);
        l3RegularHour7.setPeriod_begin("10:00");
        l3RegularHour7.setPeriod_end("24:00");
        l3RegularHours.add(l3RegularHour7);
        l3OpeningTimes.setRegular_hours(l3RegularHours);
        l3.setOpening_times(l3OpeningTimes);
        l3.setCharging_when_closed(false);
        List<ConnectorTypeCount> l3ConnectorTypeCounts = new ArrayList<>();
        ConnectorTypeCount l3ConnectorTypeCount1 = new ConnectorTypeCount();
        l3ConnectorTypeCount1.setStandard("IEC_62196_T1");
        l3ConnectorTypeCount1.setCount(1);
        l3ConnectorTypeCounts.add(l3ConnectorTypeCount1);
        l3.setConnector_type_counts(l3ConnectorTypeCounts);
        l3.setLast_updated("2024-05-13T20:00:02.000Z");
        locations.add(l3);

        // 네 번째 Location 객체 생성
        Location l4 = new Location();
        l4.setCountry_code("KR");
        l4.setParty_id("CEV");
        l4.setId("PI-200013");
        l4.setPublish(true);
        l4.setName("서울시 이마트(마포점)");
        l4.setAddress("백범로 212");
        l4.setCity("서울특별시 마포구");
        l4.setPostal_code("04196");
        l4.setCountry("KOR");
        GeoLocation l4Coordinates = new GeoLocation();
        l4Coordinates.setLatitude("37.542347");
        l4Coordinates.setLongitude("126.953424");
        l4.setCoordinates(l4Coordinates);
        List<EVSE> l4Evses = new ArrayList<>();
        EVSE l4Evse1 = new EVSE();
        l4Evse1.setUid("PI-200013-2111");
        l4Evse1.setStatus(Status.AVAILABLE);
        List<Capability> l4EvseCapa = new ArrayList<>();
        l4EvseCapa.add(Capability.RFID_READER);
        l4EvseCapa.add(Capability.REMOTE_START_STOP_CAPABLE);
        l4Evse1.setCapabilities(l4EvseCapa);
        Connector l4Evse1Connector1 = new Connector();
        l4Evse1Connector1.setId("1");
        l4Evse1Connector1.setStandard(ConnectorType.IEC_62196_T1);
        l4Evse1Connector1.setFormat(ConnectorFormat.CABLE);
        l4Evse1Connector1.setPower_type(PowerType.AC_1_PHASE);
        l4Evse1Connector1.setMax_voltage(220);
        l4Evse1Connector1.setMax_amperage(32);
        l4Evse1Connector1.setMax_electric_power(7000);
        l4Evse1Connector1.setTariff_ids(List.of("CG202312220001-1,CG202312220001-1"));
        l4Evse1Connector1.setLast_updated("2024-05-13T20:00:02.000Z");
        l4Evse1.setConnectors(List.of(l4Evse1Connector1));
        l4Evse1.setAccess(Access.SEMIPUBLIC);
        l4Evse1.setEvse_id("PI-200013-2111");
        l4Evse1.setPhysical_reference("JC6111B");
        l4Evse1.setPayment_methods(List.of("CONTRACT"));
        l4Evse1.setLast_updated("2024-05-13T20:00:02.000Z");
        EVSE l4Evse2 = new EVSE();
        l4Evse2.setUid("PI-200013-2112");
        l4Evse2.setStatus(Status.AVAILABLE);
        l4EvseCapa = new ArrayList<>();
        l4EvseCapa.add(Capability.RFID_READER);
        l4EvseCapa.add(Capability.REMOTE_START_STOP_CAPABLE);
        l4Evse2.setCapabilities(l4EvseCapa);
        Connector l4Evse2Connector1 = new Connector();
        l4Evse2Connector1.setId("1");
        l4Evse2Connector1.setStandard(ConnectorType.IEC_62196_T1);
        l4Evse2Connector1.setFormat(ConnectorFormat.CABLE);
        l4Evse2Connector1.setPower_type(PowerType.AC_1_PHASE);
        l4Evse2Connector1.setMax_voltage(220);
        l4Evse2Connector1.setMax_amperage(32);
        l4Evse2Connector1.setMax_electric_power(7000);
        l4Evse2Connector1.setTariff_ids(List.of("CG202312220001-1,CG202312220001-1"));
        l4Evse2Connector1.setLast_updated("2024-05-13T20:00:02.000Z");
        l4Evse2.setConnectors(List.of(l4Evse2Connector1));
        l4Evse2.setAccess(Access.SEMIPUBLIC);
        l4Evse2.setEvse_id("PI-200013-2112");
        l4Evse2.setPhysical_reference("JC6111B");
        l4Evse2.setPayment_methods(List.of("CONTRACT"));
        l4Evse2.setLast_updated("2024-05-13T20:00:02.000Z");
        l4Evses.add(l4Evse1);
        l4Evses.add(l4Evse2);
        l4.setEvses(l4Evses);
        List<DisplayText> l4Directions = new ArrayList<>();
        DisplayText l4Direction1 = new DisplayText();
        l4Direction1.setLanguage("ko");
        l4Direction1.setText("마트(쇼핑몰)");
        l4Directions.add(l4Direction1);
        l4.setDirections(l4Directions);
        l4.setParking_type(ParkingType.PARKING_LOT);
        BusinessDetails l4Operator = new BusinessDetails();
        // l4Operator.setId("PI");
        l4Operator.setName("차지비");
        l4Operator.setWebsite("https://www.chargev.co.kr/customer-support/charging_fee");
        // l4Operator.setPhone_number("1600-4047");
        l4.setOperator(l4Operator);
        Hotline l4Hotline = new Hotline();
        l4Hotline.setPhone_number("1600-4047");
        l4.setHotline(l4Hotline);
        List<AdditionalGeoLocation> l4RelatedLocations = new ArrayList<>();
        AdditionalGeoLocation l4RelatedLocation1 = new AdditionalGeoLocation();
        l4RelatedLocation1.setLatitude("37.542589");
        l4RelatedLocation1.setLongitude("126.953407");
        DisplayText l4RelatedLocation1Name = new DisplayText();
        l4RelatedLocation1Name.setLanguage("ko");
        l4RelatedLocation1Name.setText("입구");
        l4RelatedLocation1.setName(l4RelatedLocation1Name);
        l4RelatedLocations.add(l4RelatedLocation1);
        l4.setRelated_locations(l4RelatedLocations);
        l4.setTime_zone("Asia/Seoul");
        Hours l4OpeningTimes = new Hours();
        l4OpeningTimes.setTwentyfourseven(true);
        l4.setOpening_times(l4OpeningTimes);
        l4.setCharging_when_closed(false);
        List<ConnectorTypeCount> l4ConnectorTypeCounts = new ArrayList<>();
        ConnectorTypeCount l4ConnectorTypeCount1 = new ConnectorTypeCount();
        l4ConnectorTypeCount1.setStandard("IEC_62196_T1");
        l4ConnectorTypeCount1.setCount(2);
        l4ConnectorTypeCounts.add(l4ConnectorTypeCount1);
        l4.setConnector_type_counts(l4ConnectorTypeCounts);
        l4.setLast_updated("2024-05-13T20:00:02.000Z");
        locations.add(l4);

        // // 다섯 번째 Location 객체 생성
        // Location l5 = new Location();
        // l5.setCountry_code("KR");
        // l5.setParty_id("CEV");
        // l5.setId("PI-200014");
        // l5.setPublish(true);
        // l5.setName("서울시 이마트(목동점)");
        // l5.setAddress("오목로 299");
        // l5.setCity("서울특별시 양천구");
        // l5.setPostal_code("08001");
        // l5.setCountry("KOR");

        // GeoLocation l5Coordinates = new GeoLocation();
        // l5Coordinates.setLatitude("37.525562");
        // l5Coordinates.setLongitude("126.870933");
        // l5.setCoordinates(l5Coordinates);

        // List<Evse> l5Evses = new ArrayList<>();
        // Evse l5Evse1 = new Evse();
        // l5Evse1.setUid("PI-200014-2111");
        // l5Evse1.setStatus("AVAILABLE");
        // l5Evse1.setCapabilities(List.of("RFID_READER", "REMOTE_START_STOP_CAPABLE"));

        // Connector l5Evse1Connector1 = new Connector();
        // l5Evse1Connector1.setId("1");
        // l5Evse1Connector1.setStandard("IEC_62196_T1");
        // l5Evse1Connector1.setFormat("CABLE");
        // l5Evse1Connector1.setVoltage(220);
        // l5Evse1Connector1.setAmperage(32);
        // l5Evse1Connector1.setPower_type("AC_1_PHASE");
        // l5Evse1Connector1.setMax_power(7);
        // l5Evse1Connector1.setTariff_ids(List.of("CG202312220001-1,CG202312220001-1"));
        // l5Evse1Connector1.setLast_updated("2024-05-13T20:00:02.000Z");

        // l5Evse1.setConnectors(List.of(l5Evse1Connector1));
        // l5Evse1.setAccess("SEMIPUBLIC");
        // l5Evse1.setEvse_id("PI-200014-2111");
        // l5Evse1.setPhysical_reference("JC6111B");
        // l5Evse1.setPayment_methods(List.of("CONTRACT"));
        // l5Evse1.setLast_updated("2024-05-13T20:00:02.000Z");

        // Evse l5Evse2 = new Evse();
        // l5Evse2.setUid("PI-200014-2112");
        // l5Evse2.setStatus("AVAILABLE");
        // l5Evse2.setCapabilities(List.of("RFID_READER", "REMOTE_START_STOP_CAPABLE"));

        // Connector l5Evse2Connector1 = new Connector();
        // l5Evse2Connector1.setId("1");
        // l5Evse2Connector1.setStandard("IEC_62196_T1");
        // l5Evse2Connector1.setFormat("CABLE");
        // l5Evse2Connector1.setVoltage(220);
        // l5Evse2Connector1.setAmperage(32);
        // l5Evse2Connector1.setPower_type("AC_1_PHASE");
        // l5Evse2Connector1.setMax_power(7);
        // l5Evse2Connector1.setTariff_ids(List.of("CG202312220001-1,CG202312220001-1"));
        // l5Evse2Connector1.setLast_updated("2024-05-13T20:00:02.000Z");

        // l5Evse2.setConnectors(List.of(l5Evse2Connector1));
        // l5Evse2.setAccess("SEMIPUBLIC");
        // l5Evse2.setEvse_id("PI-200014-2112");
        // l5Evse2.setPhysical_reference("JC6111B");
        // l5Evse2.setPayment_methods(List.of("CONTRACT"));
        // l5Evse2.setLast_updated("2024-05-13T20:00:02.000Z");

        // l5Evses.add(l5Evse1);
        // l5Evses.add(l5Evse2);
        // l5.setEvses(l5Evses);

        // List<Direction> l5Directions = new ArrayList<>();
        // Direction l5Direction1 = new Direction();
        // l5Direction1.setLanguage("ko");
        // l5Direction1.setText("마트(쇼핑몰)");
        // l5Directions.add(l5Direction1);
        // l5.setDirections(l5Directions);

        // Operator l5Operator = new Operator();
        // l5Operator.setId("PI");
        // l5Operator.setName("차지비");
        // l5Operator.setWebsite("https://www.chargev.co.kr/customer-support/charging_fee");
        // l5Operator.setPhone_number("1600-4047");
        // l5.setOperator(l5Operator);

        // Hotline l5Hotline = new Hotline();
        // l5Hotline.setPhone_number("1600-4047");
        // l5.setHotline(l5Hotline);

        // List<RelatedLocation> l5RelatedLocations = new ArrayList<>();
        // RelatedLocation l5RelatedLocation1 = new RelatedLocation();
        // l5RelatedLocation1.setLatitude("37.525357");
        // l5RelatedLocation1.setLongitude("126.871095");
        // Name l5RelatedLocation1Name = new Name();
        // l5RelatedLocation1Name.setLanguage("ko");
        // l5RelatedLocation1Name.setText("입구");
        // l5RelatedLocation1.setName(l5RelatedLocation1Name);
        // l5RelatedLocations.add(l5RelatedLocation1);
        // l5.setRelated_locations(l5RelatedLocations);

        // l5.setTime_zone("Asia/Seoul");

        // OpeningTimes l5OpeningTimes = new OpeningTimes();
        // l5OpeningTimes.setTwentyfourseven(false);

        // List<RegularHour> l5RegularHours = new ArrayList<>();
        // RegularHour l5RegularHour1 = new RegularHour();
        // l5RegularHour1.setWeekday(1);
        // l5RegularHour1.setPeriod_begin("10:00");
        // l5RegularHour1.setPeriod_end("24:00");
        // l5RegularHours.add(l5RegularHour1);

        // RegularHour l5RegularHour2 = new RegularHour();
        // l5RegularHour2.setWeekday(2);
        // l5RegularHour2.setPeriod_begin("10:00");
        // l5RegularHour2.setPeriod_end("24:00");
        // l5RegularHours.add(l5RegularHour2);

        // RegularHour l5RegularHour3 = new RegularHour();
        // l5RegularHour3.setWeekday(3);
        // l5RegularHour3.setPeriod_begin("10:00");
        // l5RegularHour3.setPeriod_end("24:00");
        // l5RegularHours.add(l5RegularHour3);

        // RegularHour l5RegularHour4 = new RegularHour();
        // l5RegularHour4.setWeekday(4);
        // l5RegularHour4.setPeriod_begin("10:00");
        // l5RegularHour4.setPeriod_end("24:00");
        // l5RegularHours.add(l5RegularHour4);

        // RegularHour l5RegularHour5 = new RegularHour();
        // l5RegularHour5.setWeekday(5);
        // l5RegularHour5.setPeriod_begin("10:00");
        // l5RegularHour5.setPeriod_end("24:00");
        // l5RegularHours.add(l5RegularHour5);

        // RegularHour l5RegularHour6 = new RegularHour();
        // l5RegularHour6.setWeekday(6);
        // l5RegularHour6.setPeriod_begin("10:00");
        // l5RegularHour6.setPeriod_end("24:00");
        // l5RegularHours.add(l5RegularHour6);

        // RegularHour l5RegularHour7 = new RegularHour();
        // l5RegularHour7.setWeekday(7);
        // l5RegularHour7.setPeriod_begin("10:00");
        // l5RegularHour7.setPeriod_end("24:00");
        // l5RegularHours.add(l5RegularHour7);

        // l5OpeningTimes.setRegular_hours(l5RegularHours);
        // l5.setOpening_times(l5OpeningTimes);

        // l5.setCharging_when_closed(false);

        // List<ConnectorTypeCount> l5ConnectorTypeCounts = new ArrayList<>();
        // ConnectorTypeCount l5ConnectorTypeCount1 = new ConnectorTypeCount();
        // l5ConnectorTypeCount1.setStandard("IEC_62196_T1");
        // l5ConnectorTypeCount1.setCount(2);
        // l5ConnectorTypeCounts.add(l5ConnectorTypeCount1);
        // l5.setConnector_type_counts(l5ConnectorTypeCounts);

        // l5.setLast_updated("2016-02-04T15:00:00.000Z");

        // locations.add(l5);

        // // 여섯 번째 Location 객체 생성
        // Location l6 = new Location();
        // l6.setCountry_code("KR");
        // l6.setParty_id("CEV");
        // l6.setId("PI-200016");
        // l6.setPublish(true);
        // l6.setName("서울시 이마트(명일점)");
        // l6.setAddress("고덕로 276");
        // l6.setCity("서울특별시 강동구");
        // l6.setPostal_code("05269");
        // l6.setCountry("KOR");

        // GeoLocation l6Coordinates = new GeoLocation();
        // l6Coordinates.setLatitude("37.554706");
        // l6Coordinates.setLongitude("127.155612");
        // l6.setCoordinates(l6Coordinates);

        // List<Evse> l6Evses = new ArrayList<>();
        // Evse l6Evse1 = new Evse();
        // l6Evse1.setUid("PI-200016-2111");
        // l6Evse1.setStatus("AVAILABLE");
        // l6Evse1.setCapabilities(List.of("RFID_READER", "REMOTE_START_STOP_CAPABLE"));

        // Connector l6Evse1Connector1 = new Connector();
        // l6Evse1Connector1.setId("1");
        // l6Evse1Connector1.setStandard("IEC_62196_T1");
        // l6Evse1Connector1.setFormat("CABLE");
        // l6Evse1Connector1.setVoltage(220);
        // l6Evse1Connector1.setAmperage(32);
        // l6Evse1Connector1.setPower_type("AC_1_PHASE");
        // l6Evse1Connector1.setMax_power(7);
        // l6Evse1Connector1.setTariff_ids(List.of("CG202312220001-1,CG202312220001-1"));
        // l6Evse1Connector1.setLast_updated("2024-05-13T20:00:02.000Z");

        // l6Evse1.setConnectors(List.of(l6Evse1Connector1));
        // l6Evse1.setAccess("SEMIPUBLIC");
        // l6Evse1.setEvse_id("PI-200016-2111");
        // l6Evse1.setPhysical_reference("JC6111B");
        // l6Evse1.setPayment_methods(List.of("CONTRACT"));
        // l6Evse1.setLast_updated("2024-05-13T20:00:02.000Z");

        // l6Evses.add(l6Evse1);
        // l6.setEvses(l6Evses);

        // List<Direction> l6Directions = new ArrayList<>();
        // Direction l6Direction1 = new Direction();
        // l6Direction1.setLanguage("ko");
        // l6Direction1.setText("마트(쇼핑몰)");
        // l6Directions.add(l6Direction1);
        // l6.setDirections(l6Directions);

        // Operator l6Operator = new Operator();
        // l6Operator.setId("PI");
        // l6Operator.setName("차지비");
        // l6Operator.setWebsite("https://www.chargev.co.kr/customer-support/charging_fee");
        // l6Operator.setPhone_number("1600-4047");
        // l6.setOperator(l6Operator);

        // Hotline l6Hotline = new Hotline();
        // l6Hotline.setPhone_number("1600-4047");
        // l6.setHotline(l6Hotline);

        // List<RelatedLocation> l6RelatedLocations = new ArrayList<>();
        // RelatedLocation l6RelatedLocation1 = new RelatedLocation();
        // l6RelatedLocation1.setLatitude("37.554912");
        // l6RelatedLocation1.setLongitude("127.156140");
        // Name l6RelatedLocation1Name = new Name();
        // l6RelatedLocation1Name.setLanguage("ko");
        // l6RelatedLocation1Name.setText("입구");
        // l6RelatedLocation1.setName(l6RelatedLocation1Name);
        // l6RelatedLocations.add(l6RelatedLocation1);
        // l6.setRelated_locations(l6RelatedLocations);

        // l6.setTime_zone("Asia/Seoul");

        // OpeningTimes l6OpeningTimes = new OpeningTimes();
        // l6OpeningTimes.setTwentyfourseven(false);

        // List<RegularHour> l6RegularHours = new ArrayList<>();
        // RegularHour l6RegularHour1 = new RegularHour();
        // l6RegularHour1.setWeekday(1);
        // l6RegularHour1.setPeriod_begin("10:00");
        // l6RegularHour1.setPeriod_end("24:00");
        // l6RegularHours.add(l6RegularHour1);

        // RegularHour l6RegularHour2 = new RegularHour();
        // l6RegularHour2.setWeekday(2);
        // l6RegularHour2.setPeriod_begin("10:00");
        // l6RegularHour2.setPeriod_end("24:00");
        // l6RegularHours.add(l6RegularHour2);

        // RegularHour l6RegularHour3 = new RegularHour();
        // l6RegularHour3.setWeekday(3);
        // l6RegularHour3.setPeriod_begin("10:00");
        // l6RegularHour3.setPeriod_end("24:00");
        // l6RegularHours.add(l6RegularHour3);

        // RegularHour l6RegularHour4 = new RegularHour();
        // l6RegularHour4.setWeekday(4);
        // l6RegularHour4.setPeriod_begin("10:00");
        // l6RegularHour4.setPeriod_end("24:00");
        // l6RegularHours.add(l6RegularHour4);

        // RegularHour l6RegularHour5 = new RegularHour();
        // l6RegularHour5.setWeekday(5);
        // l6RegularHour5.setPeriod_begin("10:00");
        // l6RegularHour5.setPeriod_end("24:00");
        // l6RegularHours.add(l6RegularHour5);

        // RegularHour l6RegularHour6 = new RegularHour();
        // l6RegularHour6.setWeekday(6);
        // l6RegularHour6.setPeriod_begin("10:00");
        // l6RegularHour6.setPeriod_end("24:00");
        // l6RegularHours.add(l6RegularHour6);

        // RegularHour l6RegularHour7 = new RegularHour();
        // l6RegularHour7.setWeekday(7);
        // l6RegularHour7.setPeriod_begin("10:00");
        // l6RegularHour7.setPeriod_end("24:00");
        // l6RegularHours.add(l6RegularHour7);

        // l6OpeningTimes.setRegular_hours(l6RegularHours);
        // l6.setOpening_times(l6OpeningTimes);

        // l6.setCharging_when_closed(false);

        // List<ConnectorTypeCount> l6ConnectorTypeCounts = new ArrayList<>();
        // ConnectorTypeCount l6ConnectorTypeCount1 = new ConnectorTypeCount();
        // l6ConnectorTypeCount1.setStandard("IEC_62196_T1");
        // l6ConnectorTypeCount1.setCount(1);
        // l6ConnectorTypeCounts.add(l6ConnectorTypeCount1);
        // l6.setConnector_type_counts(l6ConnectorTypeCounts);

        // l6.setLast_updated("2016-02-04T15:00:00.000Z");

        // locations.add(l6);
        return locations;
    };

    @Override
    public Location makeLocation() {
        Location l1 = new Location();
        l1.setCountry_code("KR");
        l1.setParty_id("CEV");
        l1.setId("PI-200006");
        l1.setPublish(true);
        l1.setName("서울시 이마트에브리데이이문점");
        l1.setAddress("이문로 136");
        l1.setCity("서울특별시 동대문구");
        l1.setPostal_code("02418");
        l1.setCountry("KOR");
            GeoLocation l1Coordinates = new GeoLocation();
            l1Coordinates.setLatitude("37.598480");
            l1Coordinates.setLongitude("127.061874");
        l1.setCoordinates(l1Coordinates);
            List<EVSE> l1Evses = new ArrayList<>();
                EVSE l1Evse1 = new EVSE();
                l1Evse1.setUid("PI-200006-2111");
                l1Evse1.setEvse_id("PI-200006-2111");
                l1Evse1.setStatus(Status.AVAILABLE);
                    List<Capability> l1EvseCapa = new ArrayList<>();
                    l1EvseCapa.add(Capability.RFID_READER);
                    l1EvseCapa.add(Capability.REMOTE_START_STOP_CAPABLE);
                l1Evse1.setCapabilities(l1EvseCapa);
                    Connector l1Evse1Connector1 = new Connector();
                    l1Evse1Connector1.setId("1");
                    l1Evse1Connector1.setStandard(ConnectorType.IEC_62196_T1);
                    l1Evse1Connector1.setFormat(ConnectorFormat.CABLE);
                    l1Evse1Connector1.setPower_type(PowerType.AC_1_PHASE);
                    l1Evse1Connector1.setMax_voltage(220);
                    l1Evse1Connector1.setMax_amperage(32);
                    l1Evse1Connector1.setMax_electric_power(7000);
                    l1Evse1Connector1.setTariff_ids(List.of("CG202312220001-1,CG202312220001-1"));
                    l1Evse1Connector1.setLast_updated("2024-05-13T20:00:02.000Z");
                l1Evse1.setConnectors(List.of(l1Evse1Connector1));
                l1Evse1.setAccess(Access.SEMIPUBLIC);
                l1Evse1.setPhysical_reference("JC6111B");
                l1Evse1.setPayment_methods(List.of("CONTRACT"));
                l1Evse1.setLast_updated("2024-05-13T20:00:02.000Z");
            l1Evses.add(l1Evse1);
        l1.setEvses(l1Evses);
            List<DisplayText> l1Directions = new ArrayList<>();
                DisplayText l1Direction1 = new DisplayText();
                l1Direction1.setLanguage("ko");
                l1Direction1.setText("마트(쇼핑몰)");
            l1Directions.add(l1Direction1);
        l1.setDirections(l1Directions);
        l1.setParking_type(ParkingType.PARKING_LOT);
            BusinessDetails l1Operator = new BusinessDetails();
            // l1Operator.setId("PI");
            l1Operator.setName("차지비");
            l1Operator.setWebsite("https://www.chargev.co.kr/customer-support/charging_fee");
            // l1Operator.setPhone_number("1600-4047");
        l1.setOperator(l1Operator);
            Hotline l1Hotline = new Hotline();
            l1Hotline.setPhone_number("1600-4047");
        l1.setHotline(l1Hotline);
            List<AdditionalGeoLocation> l1RelatedLocations = new ArrayList<>();
                AdditionalGeoLocation l1RelatedLocation1 = new AdditionalGeoLocation();
                l1RelatedLocation1.setLatitude("37.598586");
                l1RelatedLocation1.setLongitude("127.061732");
                    DisplayText l1RelatedLocation1Name = new DisplayText();
                    l1RelatedLocation1Name.setLanguage("ko");
                    l1RelatedLocation1Name.setText("입구");
                l1RelatedLocation1.setName(l1RelatedLocation1Name);
            l1RelatedLocations.add(l1RelatedLocation1);
        l1.setRelated_locations(l1RelatedLocations);
        l1.setTime_zone("Asia/Seoul");
            Hours l1OpeningTimes = new Hours();
            l1OpeningTimes.setTwentyfourseven(true);
        l1.setOpening_times(l1OpeningTimes);
        l1.setCharging_when_closed(false);
            List<ConnectorTypeCount> l1ConnectorTypeCounts = new ArrayList<>();
                ConnectorTypeCount l1ConnectorTypeCount1 = new ConnectorTypeCount();
                l1ConnectorTypeCount1.setStandard("IEC_62196_T1");
                l1ConnectorTypeCount1.setCount(1);
                l1ConnectorTypeCounts.add(l1ConnectorTypeCount1);
            l1.setConnector_type_counts(l1ConnectorTypeCounts);
        l1.setLast_updated("2016-02-04T15:00:00.000Z");
        return l1;
    }

    @Override
    public EVSE makeEvse() {
        EVSE l1Evse1 = new EVSE();
                l1Evse1.setUid("PI-200006-2111");
                l1Evse1.setEvse_id("PI-200006-2111");
                l1Evse1.setStatus(Status.AVAILABLE);
                    List<Capability> l1EvseCapa = new ArrayList<>();
                    l1EvseCapa.add(Capability.RFID_READER);
                    l1EvseCapa.add(Capability.REMOTE_START_STOP_CAPABLE);
                l1Evse1.setCapabilities(l1EvseCapa);
                    Connector l1Evse1Connector1 = new Connector();
                    l1Evse1Connector1.setId("1");
                    l1Evse1Connector1.setStandard(ConnectorType.IEC_62196_T1);
                    l1Evse1Connector1.setFormat(ConnectorFormat.CABLE);
                    l1Evse1Connector1.setPower_type(PowerType.AC_1_PHASE);
                    l1Evse1Connector1.setMax_voltage(220);
                    l1Evse1Connector1.setMax_amperage(32);
                    l1Evse1Connector1.setMax_electric_power(7000);
                    l1Evse1Connector1.setTariff_ids(List.of("CG202312220001-1,CG202312220001-1"));
                    l1Evse1Connector1.setLast_updated("2024-05-13T20:00:02.000Z");
                l1Evse1.setConnectors(List.of(l1Evse1Connector1));
                l1Evse1.setAccess(Access.SEMIPUBLIC);
                l1Evse1.setPhysical_reference("JC6111B");
                l1Evse1.setPayment_methods(List.of("CONTRACT"));
                l1Evse1.setLast_updated("2024-05-13T20:00:02.000Z");

                return l1Evse1;
    }

    @Override
    public Connector makeConnector() {
        Connector l1Evse1Connector1 = new Connector();
        l1Evse1Connector1.setId("1");
        l1Evse1Connector1.setStandard(ConnectorType.IEC_62196_T1);
        l1Evse1Connector1.setFormat(ConnectorFormat.CABLE);
        l1Evse1Connector1.setPower_type(PowerType.AC_1_PHASE);
        l1Evse1Connector1.setMax_voltage(220);
        l1Evse1Connector1.setMax_amperage(32);
        l1Evse1Connector1.setMax_electric_power(7000);
        l1Evse1Connector1.setTariff_ids(List.of("CG202312220001-1,CG202312220001-1"));
        l1Evse1Connector1.setLast_updated("2024-05-13T20:00:02.000Z");

        return l1Evse1Connector1;
    }

    @Override
    public List<Tariff> makeTariffs() {
        List<Tariff> tariffs = new ArrayList<>();

        Tariff tariff = new Tariff();

        tariff.setCountry_code("KR");
        tariff.setParty_id("ALL");
        tariff.setId("ABC10334908");
        List<String> dg = new ArrayList<String>();
        dg.add("1");
        tariff.setDriver_groups(dg);
        tariff.setCurrency("KRW");
        tariff.setType(TariffType.REGULAR);
        List<DisplayText> tat = new ArrayList<DisplayText>();
        DisplayText tat1 = new DisplayText();
        tat1.setLanguage("ko");
        tat1.setText("시간 당 00000원 (부가세 포함)");
        tat.add(tat1);
        tariff.setTariff_alt_text(tat);
        tariff.setTariff_alt_url("https://company.com/tariffs/13");
        Price min_price = new Price();
        min_price.setExcl_vat(30000);
        min_price.setIncl_vat(33000);
        tariff.setMin_price(min_price);
        Price max_price = new Price();
        max_price.setExcl_vat(150000);
        max_price.setIncl_vat(165000);
        tariff.setMax_price(max_price);
        List<Element> elements = new ArrayList<Element>();
        Element e1 = new Element();
        List<PriceComponent> pcl = new ArrayList<PriceComponent>();
        PriceComponent pc1 = new PriceComponent();
        pc1.setType(TariffDimensionType.ENERGY);
        pc1.setPrice(0);
        pc1.setVat(20);
        pc1.setStep_size(1);
        pcl.add(pc1);
        e1.setPrice_components(pcl);
        Restrictions r1 = new Restrictions();
        r1.setMax_duration(1800);
        e1.setRestrictions(r1);
        elements.add(e1);
        Element e2 = new Element();
        pcl = new ArrayList<PriceComponent>();
        pc1 = new PriceComponent();
        pc1.setType(TariffDimensionType.ENERGY);
        pc1.setPrice(0.4);
        pc1.setVat(20);
        pc1.setStep_size(1);
        pcl.add(pc1);
        e1.setPrice_components(pcl);
        r1 = new Restrictions();
        r1.setMax_duration(1800);
        e1.setRestrictions(r1);
        elements.add(e2);
        tariff.setElements(elements);
        tariff.setStart_date_time("2024-01-01T00:00:00z");
        tariff.setEnd_date_time("2024-12-31T23:39:09z");
        EnergyMix energy_mix = new EnergyMix();
        energy_mix.set_green_energy(false);
        List<EnergySource> esl = new ArrayList<>();
        EnergySource es1 = new EnergySource();
        es1.setSource(EnergySourceCategory.GENERAL_GREEN);
        es1.setPercentage(35.9);
        esl.add(es1);
        EnergySource es2 = new EnergySource();
        es2.setSource(EnergySourceCategory.GAS);
        es2.setPercentage(6.3);
        esl.add(es2);
        EnergySource es3 = new EnergySource();
        es3.setSource(EnergySourceCategory.COAL);
        es3.setPercentage(33.2);
        esl.add(es3);
        EnergySource es4 = new EnergySource();
        es4.setSource(EnergySourceCategory.GENERAL_FOSSIL);
        es4.setPercentage(2.9);
        esl.add(es4);
        EnergySource es5 = new EnergySource();
        es5.setSource(EnergySourceCategory.NUCLEAR);
        es5.setPercentage(21.7);
        esl.add(es5);
        energy_mix.setEnergy_sources(esl);
        List<EnvironmentImpact> environ_impact = new ArrayList<>();
        EnvironmentImpact ei1 = new EnvironmentImpact();
        ei1.setCategory(EnvironmentalImpactCategory.NUCLEAR_WASTE);
        ei1.setAmount(0.0006);
        environ_impact.add(ei1);
        EnvironmentImpact ei2 = new EnvironmentImpact();
        ei2.setCategory(EnvironmentalImpactCategory.CARBON_DIOXIDE);
        ei2.setAmount(372);
        environ_impact.add(ei2);
        energy_mix.setEnviron_impact(environ_impact);
        energy_mix.setSupplier_name("dummy_supplier_name");
        energy_mix.setEnergy_product_name("dummy_energy_product_name");
        tariff.setEnergy_mix(energy_mix);
        tariff.setLast_updated("2024-01-01T23:39:09z");

        tariffs.add(tariff);

        tariff = new Tariff();

        tariff.setCountry_code("KR");
        tariff.setParty_id("ALL");
        tariff.setId("ABC10334909");
        dg = new ArrayList<String>();
        dg.add("1");
        tariff.setDriver_groups(dg);
        tariff.setCurrency("KRW");
        tariff.setType(TariffType.REGULAR);
        tat = new ArrayList<DisplayText>();
        tat1 = new DisplayText();
        tat1.setLanguage("ko");
        tat1.setText("시간 당 00000원 (부가세 포함)");
        tat.add(tat1);
        tariff.setTariff_alt_text(tat);
        tariff.setTariff_alt_url("https://company.com/tariffs/13");
        min_price = new Price();
        min_price.setExcl_vat(30000);
        min_price.setIncl_vat(33000);
        tariff.setMin_price(min_price);
        max_price = new Price();
        max_price.setExcl_vat(150000);
        max_price.setIncl_vat(165000);
        tariff.setMax_price(max_price);
        elements = new ArrayList<Element>();
        e1 = new Element();
        pcl = new ArrayList<PriceComponent>();
        pc1 = new PriceComponent();
        pc1.setType(TariffDimensionType.ENERGY);
        pc1.setPrice(0);
        pc1.setVat(20);
        pc1.setStep_size(1);
        pcl.add(pc1);
        e1.setPrice_components(pcl);
        r1 = new Restrictions();
        r1.setMax_duration(1800);
        e1.setRestrictions(r1);
        elements.add(e1);
        e2 = new Element();
        pcl = new ArrayList<PriceComponent>();
        pc1 = new PriceComponent();
        pc1.setType(TariffDimensionType.ENERGY);
        pc1.setPrice(0.4);
        pc1.setVat(20);
        pc1.setStep_size(1);
        pcl.add(pc1);
        e1.setPrice_components(pcl);
        r1 = new Restrictions();
        r1.setMax_duration(1800);
        e1.setRestrictions(r1);
        elements.add(e2);
        tariff.setElements(elements);
        tariff.setStart_date_time("2024-01-01T00:00:00z");
        tariff.setEnd_date_time("2024-12-31T23:39:09z");
        energy_mix = new EnergyMix();
        energy_mix.set_green_energy(false);
        esl = new ArrayList<>();
        es1 = new EnergySource();
        es1.setSource(EnergySourceCategory.GENERAL_GREEN);
        es1.setPercentage(35.9);
        esl.add(es1);
        es2 = new EnergySource();
        es2.setSource(EnergySourceCategory.GAS);
        es2.setPercentage(6.3);
        esl.add(es2);
        es3 = new EnergySource();
        es3.setSource(EnergySourceCategory.COAL);
        es3.setPercentage(33.2);
        esl.add(es3);
        es4 = new EnergySource();
        es4.setSource(EnergySourceCategory.GENERAL_FOSSIL);
        es4.setPercentage(2.9);
        esl.add(es4);
        es5 = new EnergySource();
        es5.setSource(EnergySourceCategory.NUCLEAR);
        es5.setPercentage(21.7);
        esl.add(es5);
        energy_mix.setEnergy_sources(esl);
        environ_impact = new ArrayList<>();
        ei1 = new EnvironmentImpact();
        ei1.setCategory(EnvironmentalImpactCategory.NUCLEAR_WASTE);
        ei1.setAmount(0.0006);
        environ_impact.add(ei1);
        ei2 = new EnvironmentImpact();
        ei2.setCategory(EnvironmentalImpactCategory.CARBON_DIOXIDE);
        ei2.setAmount(372);
        environ_impact.add(ei2);
        energy_mix.setEnviron_impact(environ_impact);
        energy_mix.setSupplier_name("dummy_supplier_name");
        energy_mix.setEnergy_product_name("dummy_energy_product_name");
        tariff.setEnergy_mix(energy_mix);
        tariff.setLast_updated("2024-01-01T23:39:09z");

        tariffs.add(tariff);

        return tariffs;
    }

    @Override
    public Tariff makeTariff() {
        Tariff tariff = new Tariff();

        tariff.setCountry_code("KR");
        tariff.setParty_id("ALL");
        tariff.setId("ABC10334908");
        List<String> dg = new ArrayList<String>();
        dg.add("1");
        tariff.setDriver_groups(dg);
        tariff.setCurrency("KRW");
        tariff.setType(TariffType.REGULAR);
        List<DisplayText> tat = new ArrayList<DisplayText>();
        DisplayText tat1 = new DisplayText();
        tat1.setLanguage("ko");
        tat1.setText("시간 당 00000원 (부가세 포함)");
        tat.add(tat1);
        tariff.setTariff_alt_text(tat);
        tariff.setTariff_alt_url("https://company.com/tariffs/13");
        Price min_price = new Price();
        min_price.setExcl_vat(30000);
        min_price.setIncl_vat(33000);
        tariff.setMin_price(min_price);
        Price max_price = new Price();
        max_price.setExcl_vat(150000);
        max_price.setIncl_vat(165000);
        tariff.setMax_price(max_price);
        List<Element> elements = new ArrayList<Element>();
        Element e1 = new Element();
        List<PriceComponent> pcl = new ArrayList<PriceComponent>();
        PriceComponent pc1 = new PriceComponent();
        pc1.setType(TariffDimensionType.ENERGY);
        pc1.setPrice(0);
        pc1.setVat(20);
        pc1.setStep_size(1);
        pcl.add(pc1);
        e1.setPrice_components(pcl);
        Restrictions r1 = new Restrictions();
        r1.setMax_duration(1800);
        e1.setRestrictions(r1);
        elements.add(e1);
        Element e2 = new Element();
        pcl = new ArrayList<PriceComponent>();
        pc1 = new PriceComponent();
        pc1.setType(TariffDimensionType.ENERGY);
        pc1.setPrice(0.4);
        pc1.setVat(20);
        pc1.setStep_size(1);
        pcl.add(pc1);
        e1.setPrice_components(pcl);
        r1 = new Restrictions();
        r1.setMax_duration(1800);
        e1.setRestrictions(r1);
        elements.add(e2);
        tariff.setElements(elements);
        tariff.setStart_date_time("2024-01-01T00:00:00z");
        tariff.setEnd_date_time("2024-12-31T23:39:09z");
        EnergyMix energy_mix = new EnergyMix();
        energy_mix.set_green_energy(false);
        List<EnergySource> esl = new ArrayList<>();
        EnergySource es1 = new EnergySource();
        es1.setSource(EnergySourceCategory.GENERAL_GREEN);
        es1.setPercentage(35.9);
        esl.add(es1);
        EnergySource es2 = new EnergySource();
        es2.setSource(EnergySourceCategory.GAS);
        es2.setPercentage(6.3);
        esl.add(es2);
        EnergySource es3 = new EnergySource();
        es3.setSource(EnergySourceCategory.COAL);
        es3.setPercentage(33.2);
        esl.add(es3);
        EnergySource es4 = new EnergySource();
        es4.setSource(EnergySourceCategory.GENERAL_FOSSIL);
        es4.setPercentage(2.9);
        esl.add(es4);
        EnergySource es5 = new EnergySource();
        es5.setSource(EnergySourceCategory.NUCLEAR);
        es5.setPercentage(21.7);
        esl.add(es5);
        energy_mix.setEnergy_sources(esl);
        List<EnvironmentImpact> environ_impact = new ArrayList<>();
        EnvironmentImpact ei1 = new EnvironmentImpact();
        ei1.setCategory(EnvironmentalImpactCategory.NUCLEAR_WASTE);
        ei1.setAmount(0.0006);
        environ_impact.add(ei1);
        EnvironmentImpact ei2 = new EnvironmentImpact();
        ei2.setCategory(EnvironmentalImpactCategory.CARBON_DIOXIDE);
        ei2.setAmount(372);
        environ_impact.add(ei2);
        energy_mix.setEnviron_impact(environ_impact);
        energy_mix.setSupplier_name("dummy_supplier_name");
        energy_mix.setEnergy_product_name("dummy_energy_product_name");
        tariff.setEnergy_mix(energy_mix);
        tariff.setLast_updated("2024-01-01T23:39:09z");

        return tariff;
    }

    @Override
    public SessionForMb makeSession() {
        SessionForMb session = new SessionForMb();

        session.setCountry_code("KR");
        session.setParty_id("CEV");
        session.setId("sid12345");
        session.setStart_date_time("2024-07-09T05:27:05.931Z");
        session.setEnd_date_time("2024-07-09T05:27:05.931Z");
        session.setKwh(0.0);
        session.setEmsp_contract_id("KRCEV001ABC6");
        session.setAuth_method(AuthMethod.COMMAND);
        session.setLocation_id("PI-200013");
        session.setEvse_uid("PI-200013-2111");
        session.setConnector_id("1");
        session.setCurrency("KRW");

        Price total_cost = new Price();
        total_cost.setExcl_vat(0);
        total_cost.setIncl_vat(0);
        session.setTotal_cost(total_cost);

        session.setStatus(SessionStatus.PENDING);
        session.setLast_updated("2024-07-09T05:27:05.931Z");

        return session;
    };

    @Override
    public List<CdrForMb> makeCdrList() {
        List<CdrForMb> cdrs = new ArrayList<>();

        // 첫 번째 CDR 객체 생성
        CdrForMb cdr1 = new CdrForMb();
        cdr1.setCountry_code("KR");
        cdr1.setParty_id("ALL");
        cdr1.setId("adfc0c32-2c3e-44b7-a552-ea9fc7d4a71e");
        cdr1.setStart_date_time("2024-01-01T23:39:09z");
        cdr1.setEnd_date_time("2024-01-01T23:39:09z");

        CdrToken cdrToken1 = new CdrToken();
        cdrToken1.setCountry_code("KR");
        cdrToken1.setParty_id("ALL");
        cdrToken1.setUid("012345678");
        cdrToken1.setTokenType("RFID");
        cdrToken1.setContract_id("KRCEV001ABC6");
        cdr1.setCdr_token(cdrToken1);

        cdr1.setAuth_method(AuthMethod.WHITELIST);
        cdr1.setAuthorization_reference("authorization_reference");

        CdrLocationForMb cdrLocation1 = new CdrLocationForMb();
        cdrLocation1.setId("LOC1");
        cdrLocation1.setName("Gent Zuid");
        cdrLocation1.setTime_zone("Europe/Oslo");
        cdrLocation1.setAddress("F.Rooseveltlaan 3A");
        cdrLocation1.setCity("Gent");
        cdrLocation1.setState("state");
        cdrLocation1.setPostal_code("9000");
        cdrLocation1.setCountry("BEL");

        GeoLocation geoLocation1 = new GeoLocation();
        geoLocation1.setLatitude("3.729944");
        geoLocation1.setLongitude("51.047599");
        cdrLocation1.setCoordinates(geoLocation1);

        cdrLocation1.setEvse_uid("3256");
        cdrLocation1.setEvse_id("PI-200013-2111");
        cdrLocation1.setConnector_id("1");
        cdrLocation1.setConnector_standard(ConnectorType.IEC_62196_T2);
        cdrLocation1.setConnector_format(ConnectorFormat.SOCKET);
        cdrLocation1.setConnector_power_type(PowerType.AC_1_PHASE);
        cdr1.setCdr_location(cdrLocation1);

        cdr1.setCurrency("KRW");

        List<Tariff> tariffs1 = new ArrayList<>();
        Tariff tariff1 = new Tariff();
        tariff1.setCountry_code("KR");
        tariff1.setParty_id("ALL");
        tariff1.setId("ABC10334908");
        List<String> driver_groups = new ArrayList<>();
        driver_groups.add("102");
        tariff1.setDriver_groups(driver_groups);
        tariff1.setCurrency("KRW");
        tariff1.setType(TariffType.REGULAR);

        List<DisplayText> tariff_alt_text = new ArrayList<>();
        DisplayText tat1 = new DisplayText();
        tat1.setLanguage("en");
        tat1.setText("2.00 euro p/hour including VAT.");
        tariff_alt_text.add(tat1);
        DisplayText tat2 = new DisplayText();
        tat2.setLanguage("nl");
        tat2.setText("2.00 euro p/uur inclusief BTW.");
        tariff_alt_text.add(tat2);
        tariff1.setTariff_alt_text(tariff_alt_text);

        tariff1.setTariff_alt_url("https://company.com/tariffs/13");

        Price min_price = new Price();
        min_price.setExcl_vat(20.5);
        min_price.setIncl_vat(24.6);

        Price max_price = new Price();
        max_price.setExcl_vat(100);
        max_price.setIncl_vat(110);

        List<Element> elements1 = new ArrayList<>();
        Element element1 = new Element();

        List<PriceComponent> priceComponents1 = new ArrayList<>();
        PriceComponent priceComponent1 = new PriceComponent();
        priceComponent1.setType(TariffDimensionType.ENERGY);
        priceComponent1.setPrice(2.00);
        priceComponent1.setVat(10.0);
        priceComponent1.setStep_size(300);
        priceComponents1.add(priceComponent1);
        element1.setPrice_components(priceComponents1);
        elements1.add(element1);
        tariff1.setElements(elements1);
        tariff1.setLast_updated("2015-02-02T14:15:01Z");
        tariff1.setStart_date_time("2024-01-01T23:39:09z");
        tariff1.setEnd_date_time("2024-01-01T23:39:09z");

        EnergyMix energyMix = new EnergyMix();
        energyMix.set_green_energy(false);

        tariffs1.add(tariff1);
        cdr1.setTariffs(tariffs1);


        List<ChargingPeriod> chargingPeriods1 = new ArrayList<>();
        ChargingPeriod chargingPeriod1 = new ChargingPeriod();
        chargingPeriod1.setStart_date_time("2015-06-29T21:39:09Z");

        List<CdrDimension> dimensions1 = new ArrayList<>();
        CdrDimension dimension1 = new CdrDimension();
        dimension1.setType(CdrDimensionType.TIME);
        dimension1.setVolume(1.973);
        dimensions1.add(dimension1);
        chargingPeriod1.setDimensions(dimensions1);
        chargingPeriod1.setTariff_id("12");
        chargingPeriods1.add(chargingPeriod1);
        cdr1.setCharging_periods(chargingPeriods1);

        Price totalCost1 = new Price();
        totalCost1.setExcl_vat(4.00);
        totalCost1.setIncl_vat(4.40);
        cdr1.setTotal_cost(totalCost1);

        cdr1.setTotal_energy(15.342);
        cdr1.setTotal_time(1.973);

        Price totalTimeCost1 = new Price();
        totalTimeCost1.setExcl_vat(4.00);
        totalTimeCost1.setIncl_vat(4.40);
        cdr1.setTotal_time_cost(totalTimeCost1);

        cdr1.setLast_updated("2015-06-29T22:01:13Z");

        cdrs.add(cdr1);

        // 두 번째 CDR 객체 생성 (기존 데이터의 변경된 부분들만 업데이트)
        CdrForMb cdr2 = new CdrForMb();
        cdr2.setCountry_code("KR");
        cdr2.setParty_id("ALL");
        cdr2.setId("adfc0c32-2c3e-44b7-a552-ea9fc7d4a72e");
        cdr2.setStart_date_time("2024-01-02T10:00:00Z");
        cdr2.setEnd_date_time("2024-01-02T11:00:00Z");

        CdrToken cdrToken2 = new CdrToken();
        cdrToken2.setCountry_code("KR");
        cdrToken2.setParty_id("ALL");
        cdrToken2.setUid("987654321");
        cdrToken2.setTokenType("RFID");
        cdrToken2.setContract_id("KRCEV002ABC7");
        cdr2.setCdr_token(cdrToken2);

        cdr2.setAuth_method(AuthMethod.WHITELIST);
        cdr2.setAuthorization_reference("authorization_reference_2");

        CdrLocationForMb cdrLocation2 = new CdrLocationForMb();
        cdrLocation2.setId("LOC2");
        cdrLocation2.setName("Seoul Center");
        cdrLocation2.setTime_zone("Asia/Seoul");
        cdrLocation2.setAddress("Seoul Street 2");
        cdrLocation2.setCity("Seoul");
        cdrLocation2.setState("state");
        cdrLocation2.setPostal_code("1000");
        cdrLocation2.setCountry("KOR");

        GeoLocation geoLocation2 = new GeoLocation();
        geoLocation2.setLatitude("126.9780");
        geoLocation2.setLongitude("37.5665");
        cdrLocation2.setCoordinates(geoLocation2);

        cdrLocation2.setEvse_uid("4567");
        cdrLocation2.setEvse_id("PI-200014-2112");
        cdrLocation2.setConnector_id("2");
        cdrLocation2.setConnector_standard(ConnectorType.IEC_62196_T2);
        cdrLocation2.setConnector_format(ConnectorFormat.SOCKET);
        cdrLocation2.setConnector_power_type(PowerType.AC_1_PHASE);
        cdr2.setCdr_location(cdrLocation2);

        cdr2.setCurrency("KRW");

        List<Tariff> tariffs2 = new ArrayList<>();
        Tariff tariff2 = new Tariff();
        tariff2.setCountry_code("KR");
        tariff2.setParty_id("ALL");
        tariff2.setId("ABC10334909");
        List<String> driver_groups2 = new ArrayList<>();
        driver_groups2.add("103");
        tariff2.setDriver_groups(driver_groups2);
        tariff2.setCurrency("KRW");
        tariff2.setType(TariffType.REGULAR);

        List<DisplayText> tariff_alt_text2 = new ArrayList<>();
        DisplayText tat3 = new DisplayText();
        tat3.setLanguage("en");
        tat3.setText("3.00 euro p/hour including VAT.");
        tariff_alt_text2.add(tat3);
        DisplayText tat4 = new DisplayText();
        tat4.setLanguage("kr");
        tat4.setText("3.00 유로 시간당 부가세 포함.");
        tariff_alt_text2.add(tat4);
        tariff2.setTariff_alt_text(tariff_alt_text2);

        tariff2.setTariff_alt_url("https://company.com/tariffs/14");

        Price min_price2 = new Price();
        min_price2.setExcl_vat(25.0);
        min_price2.setIncl_vat(30.0);

        Price max_price2 = new Price();
        max_price2.setExcl_vat(150);
        max_price2.setIncl_vat(165);

        List<Element> elements2 = new ArrayList<>();
        Element element2 = new Element();

        List<PriceComponent> priceComponents2 = new ArrayList<>();
        PriceComponent priceComponent2 = new PriceComponent();
        priceComponent2.setType(TariffDimensionType.ENERGY);
        priceComponent2.setPrice(3.00);
        priceComponent2.setVat(10.0);
        priceComponent2.setStep_size(300);
        priceComponents2.add(priceComponent2);
        element2.setPrice_components(priceComponents2);
        elements2.add(element2);
        tariff2.setElements(elements2);
        tariff2.setLast_updated("2016-02-02T14:15:01Z");
        tariff2.setStart_date_time("2024-01-02T10:00:00Z");
        tariff2.setEnd_date_time("2024-01-02T11:00:00Z");

        EnergyMix energyMix2 = new EnergyMix();
        energyMix2.set_green_energy(false);

        tariffs2.add(tariff2);
        cdr2.setTariffs(tariffs2);

        List<ChargingPeriod> chargingPeriods2 = new ArrayList<>();
        ChargingPeriod chargingPeriod2 = new ChargingPeriod();
        chargingPeriod2.setStart_date_time("2015-07-01T10:15:30Z");

        List<CdrDimension> dimensions2 = new ArrayList<>();
        CdrDimension dimension2 = new CdrDimension();
        dimension2.setType(CdrDimensionType.TIME);
        dimension2.setVolume(0.75);
        dimensions2.add(dimension2);
        chargingPeriod2.setDimensions(dimensions2);
        chargingPeriod2.setTariff_id("13");
        chargingPeriods2.add(chargingPeriod2);
        cdr2.setCharging_periods(chargingPeriods2);

        Price totalCost2 = new Price();
        totalCost2.setExcl_vat(1.50);
        totalCost2.setIncl_vat(1.65);
        cdr2.setTotal_cost(totalCost2);

        cdr2.setTotal_energy(10.123);
        cdr2.setTotal_time(0.75);

        Price totalTimeCost2 = new Price();
        totalTimeCost2.setExcl_vat(1.50);
        totalTimeCost2.setIncl_vat(1.65);
        cdr2.setTotal_time_cost(totalTimeCost2);

        cdr2.setLast_updated("2015-07-01T12:00:00Z");

        cdrs.add(cdr2);

        // 세 번째 CDR 객체 생성 (기존 데이터의 변경된 부분들만 업데이트)
        CdrForMb cdr3 = new CdrForMb();
        cdr3.setCountry_code("KR");
        cdr3.setParty_id("ALL");
        cdr3.setId("adfc0c32-2c3e-44b7-a552-ea9fc7d4a73e");
        cdr3.setStart_date_time("2024-01-03T12:00:00Z");
        cdr3.setEnd_date_time("2024-01-03T14:00:00Z");

        CdrToken cdrToken3 = new CdrToken();
        cdrToken3.setCountry_code("KR");
        cdrToken3.setParty_id("ALL");
        cdrToken3.setUid("654321098");
        cdrToken3.setTokenType("RFID");
        cdrToken3.setContract_id("KRCEV003ABC8");
        cdr3.setCdr_token(cdrToken3);

        cdr3.setAuth_method(AuthMethod.WHITELIST);
        cdr3.setAuthorization_reference("authorization_reference_3");

        CdrLocationForMb cdrLocation3 = new CdrLocationForMb();
        cdrLocation3.setId("LOC3");
        cdrLocation3.setName("Busan Port");
        cdrLocation3.setTime_zone("Asia/Seoul");
        cdrLocation3.setAddress("Busan Street 3");
        cdrLocation3.setCity("Busan");
        cdrLocation3.setState("state");
        cdrLocation3.setPostal_code("2000");
        cdrLocation3.setCountry("KOR");

        GeoLocation geoLocation3 = new GeoLocation();
        geoLocation3.setLatitude("129.0756");
        geoLocation3.setLongitude("35.1796");
        cdrLocation3.setCoordinates(geoLocation3);

        cdrLocation3.setEvse_uid("5678");
        cdrLocation3.setEvse_id("PI-200015-2113");
        cdrLocation3.setConnector_id("3");
        cdrLocation3.setConnector_standard(ConnectorType.IEC_62196_T2);
        cdrLocation3.setConnector_format(ConnectorFormat.SOCKET);
        cdrLocation3.setConnector_power_type(PowerType.AC_1_PHASE);
        cdr3.setCdr_location(cdrLocation3);

        cdr3.setCurrency("KRW");

        List<Tariff> tariffs3 = new ArrayList<>();
        Tariff tariff3 = new Tariff();
        tariff3.setCountry_code("KR");
        tariff3.setParty_id("ALL");
        tariff3.setId("ABC10334910");
        List<String> driver_groups3 = new ArrayList<>();
        driver_groups3.add("104");
        tariff3.setDriver_groups(driver_groups3);
        tariff3.setCurrency("KRW");
        tariff3.setType(TariffType.REGULAR);

        List<DisplayText> tariff_alt_text3 = new ArrayList<>();
        DisplayText tat5 = new DisplayText();
        tat5.setLanguage("en");
        tat5.setText("4.00 euro p/hour including VAT.");
        tariff_alt_text3.add(tat5);
        DisplayText tat6 = new DisplayText();
        tat6.setLanguage("kr");
        tat6.setText("4.00 유로 시간당 부가세 포함.");
        tariff_alt_text3.add(tat6);
        tariff3.setTariff_alt_text(tariff_alt_text3);

        tariff3.setTariff_alt_url("https://company.com/tariffs/15");

        Price min_price3 = new Price();
        min_price3.setExcl_vat(30.0);
        min_price3.setIncl_vat(33.0);

        Price max_price3 = new Price();
        max_price3.setExcl_vat(180);
        max_price3.setIncl_vat(198);

        List<Element> elements3 = new ArrayList<>();
        Element element3 = new Element();

        List<PriceComponent> priceComponents3 = new ArrayList<>();
        PriceComponent priceComponent3 = new PriceComponent();
        priceComponent3.setType(TariffDimensionType.ENERGY);
        priceComponent3.setPrice(4.00);
        priceComponent3.setVat(10.0);
        priceComponent3.setStep_size(300);
        priceComponents3.add(priceComponent3);
        element3.setPrice_components(priceComponents3);
        elements3.add(element3);
        tariff3.setElements(elements3);
        tariff3.setLast_updated("2017-02-02T14:15:01Z");
        tariff3.setStart_date_time("2024-01-03T12:00:00Z");
        tariff3.setEnd_date_time("2024-01-03T14:00:00Z");

        EnergyMix energyMix3 = new EnergyMix();
        energyMix3.set_green_energy(false);

        tariffs3.add(tariff3);
        cdr3.setTariffs(tariffs3);

        List<ChargingPeriod> chargingPeriods3 = new ArrayList<>();
        ChargingPeriod chargingPeriod3 = new ChargingPeriod();
        chargingPeriod3.setStart_date_time("2015-08-01T12:00:00Z");

        List<CdrDimension> dimensions3 = new ArrayList<>();
        CdrDimension dimension3 = new CdrDimension();
        dimension3.setType(CdrDimensionType.TIME);
        dimension3.setVolume(2.00);
        dimensions3.add(dimension3);
        chargingPeriod3.setDimensions(dimensions3);
        chargingPeriod3.setTariff_id("14");
        chargingPeriods3.add(chargingPeriod3);
        cdr3.setCharging_periods(chargingPeriods3);

        Price totalCost3 = new Price();
        totalCost3.setExcl_vat(6.00);
        totalCost3.setIncl_vat(6.60);
        cdr3.setTotal_cost(totalCost3);

        cdr3.setTotal_energy(20.456);
        cdr3.setTotal_time(2.00);

        Price totalTimeCost3 = new Price();
        totalTimeCost3.setExcl_vat(6.00);
        totalTimeCost3.setIncl_vat(6.60);
        cdr3.setTotal_time_cost(totalTimeCost3);

        cdr3.setLast_updated("2015-08-01T15:00:00Z");

        cdrs.add(cdr3);

        return cdrs;
    };
}
