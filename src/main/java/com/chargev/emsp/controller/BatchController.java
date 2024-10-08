package com.chargev.emsp.controller;

import java.util.List;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.JpaSort.Path;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.entity.ocpi.OcpiLocation;
import com.chargev.emsp.entity.poi.PoiMaster;
import com.chargev.emsp.model.dto.ocpi.Location;
import com.chargev.emsp.model.dto.response.ApiResponseObjectList;
import com.chargev.emsp.model.dto.response.OcpiResponseStatusCode;
import com.chargev.emsp.service.ocpi.LocationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("{version}/batch")
@RequiredArgsConstructor

public class BatchController {
    private final LocationService locationService;

    @GetMapping("/locations/{startUpdated}/{lastUpdated}/{oemId}")
    public ApiResponseObjectList<Location> locations(
        @PathVariable("startUpdated") String startUpdated,
        @PathVariable("lastUpdated") String lastUpdated,
        @PathVariable("oemId") String oemId) {
    
        ApiResponseObjectList<Location> response = new ApiResponseObjectList<>();
        response.setStatusCode(OcpiResponseStatusCode.SUCCESS);

        if(oemId.equals("MB")) {
            response.setData(locationService.updateLastPoi(startUpdated, lastUpdated, oemId));
        }
        return response;
    }

}

