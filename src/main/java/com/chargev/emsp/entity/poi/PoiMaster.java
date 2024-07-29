package com.chargev.emsp.entity.poi;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import lombok.Data;


@Data
@Entity
@Table(name = "poi_master")
public class PoiMaster {

    @EmbeddedId
    private PoiMasterId id;

    @Column(name = "volvo_yn", length = 2)
    private String volvoYn;

    @Column(name = "benz_yn", length = 2)
    private String benzYn;

    @Column(name = "station_name", length = 100)
    private String stationName;

    @Column(name = "category", length = 20)
    private String category;

    @Column(name = "road_address", length = 255)
    private String roadAddress;

    @Column(name = "road_detail", length = 255)
    private String roadDetail;

    @Column(name = "house_number", length = 100)
    private String houseNumber;

    @Column(name = "jibun_address", length = 255)
    private String jibunAddress;

    @Column(name = "jibun_detail", length = 255)
    private String jibunDetail;

    @Column(name = "station_descript", length = 255)
    private String stationDescript;

    @Column(name = "basic_address", length = 50)
    private String basicAddress;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "area_code", length = 20)
    private String areaCode;

    @Column(name = "area_detail_code", length = 20)
    private String areaDetailCode;

    @Column(name = "gps_lat", precision = 16, scale = 13)
    private BigDecimal gpsLat;

    @Column(name = "gps_lng", precision = 16, scale = 13)
    private BigDecimal gpsLng;

    @Column(name = "entry_gps_lat", precision = 16, scale = 13)
    private BigDecimal entryGpsLat;

    @Column(name = "entry_gps_lng", precision = 16, scale = 13)
    private BigDecimal entryGpsLng;

    @Column(name = "operation_time_type", length = 50)
    private String operationTimeType;

    @Column(name = "operation_time", length = 100)
    private String operationTime;

    @Column(name = "open_time", length = 30)
    private String openTime;

    @Column(name = "close_time", length = 30)
    private String closeTime;

    @Column(name = "membership_yn", length = 2)
    private String membershipYn;

    @Column(name = "stroghold_type_yn", length = 2)
    private String strogholdTypeYn;

    @Column(name = "highway_yn", length = 2)
    private String highwayYn;

    @Column(name = "highway_type", length = 10)
    private String highwayType;

    @Convert(converter = OperatorConverter.class)
    @Column(name = "operator", columnDefinition = "json")
    private Operator operator;

    @Convert(converter = EvseConverter.class)
    @Column(name = "evse_str", columnDefinition = "json")
    private ArrayList<EvseStr> evseStr;

    @Column(name = "evse_cnt", precision = 4, scale = 0)
    private BigDecimal evseCnt;

    @Convert(converter = ConnectorTypeStrConverter.class)
    @Column(name = "conncet_type_str", columnDefinition = "json")
    private ArrayList<ConnectorTypeStr> conncetTypeStr;

    @Column(name = "last_updated", length = 30)
    private String lastUpdated;

    @Embeddable
    @Data
    public static class PoiMasterId {

        @Column(name = "pkey", nullable = false, precision = 1, scale = 0)
        private BigDecimal pkey;

        @Column(name = "location_id", nullable = false, length = 100)
        private String locationId;
    }
}