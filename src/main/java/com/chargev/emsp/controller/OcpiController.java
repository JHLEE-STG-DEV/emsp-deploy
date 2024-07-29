package com.chargev.emsp.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.model.dto.cpo.RfidVerifyResponse;
import com.chargev.emsp.model.dto.ocpi.CdrForMb;
import com.chargev.emsp.model.dto.ocpi.CdrStatistics;
import com.chargev.emsp.model.dto.ocpi.CommandResponseForMb;
import com.chargev.emsp.model.dto.ocpi.CommandResponseType;
import com.chargev.emsp.model.dto.ocpi.Connector;
import com.chargev.emsp.model.dto.ocpi.EVSE;
import com.chargev.emsp.model.dto.ocpi.Location;
import com.chargev.emsp.model.dto.ocpi.Price;
import com.chargev.emsp.model.dto.ocpi.SessionForMb;
import com.chargev.emsp.model.dto.ocpi.StartSessionForMb;
import com.chargev.emsp.model.dto.ocpi.StopSession;
import com.chargev.emsp.model.dto.ocpi.Tariff;
import com.chargev.emsp.model.dto.pnc.OcspResponse;
import com.chargev.emsp.model.dto.pnc.PncReqBodyOCSPMessage;
import com.chargev.emsp.model.dto.response.ApiResponseObject;
import com.chargev.emsp.model.dto.response.ApiResponseObjectList;
import com.chargev.emsp.model.dto.response.OcpiResponseStatusCode;
import com.chargev.emsp.model.dto.response.PncApiResponseObject;
import com.chargev.emsp.model.dto.response.PncResponseResult;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.dummy.DummyDataService;
import com.chargev.emsp.service.formatter.DateTimeFormatterService;
import com.chargev.emsp.service.log.ControllerLogService;
import com.chargev.emsp.service.cpo.CpoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@Slf4j
@RequestMapping("{version}/ocpi")
@Validated
@RequiredArgsConstructor
public class OcpiController {
    private final DateTimeFormatterService dateTimeFormatterService;
    private final DummyDataService dummyDataService;
    private final ControllerLogService logService;
    private final HttpServletRequest httpRequest;
    private final CpoService cpoService;

    // #region LOCATIONS

    @GetMapping("/locations")
    @Operation(summary = "1-1. locations - locations list", description = "{date_from}에서 {date_to} 사이에 마지막으로 업데이트된 위치 목록을 가져온다.")
    @Parameters({
        @Parameter(required=false, name = "date_from", description = "날짜/시간(포함) 이후 last_updated 있는 개체만 반환", example = "2024-06-01T06:12:31.401Z"),
        @Parameter(required=false, name = "date_to", description = "이 날짜/시간(제외) 까지 last_updated 있는 개체만 반환", example = "2024-06-04T06:12:31.401Z"),
        @Parameter(required=false, name = "offset", description = "반환된 첫 번째 개체의 오프셋", example = "0"),
        @Parameter(required=false, name = "limit", description = "GET할 최대 개체 수", example = "0"),
    })
    public ApiResponseObjectList<Location> locations(
            @RequestParam(required = false, defaultValue = "") String date_from,
            @RequestParam(required = false, defaultValue = "") String date_to,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {

            ApiResponseObjectList<Location> result = new ApiResponseObjectList<>();

            // Parse the date_from and date_to strings
            final ZonedDateTime fromDate;
            final ZonedDateTime toDate;
            try {
                fromDate = (date_from != null && !date_from.isEmpty()) ? ZonedDateTime.parse(date_from) : null;
                toDate = (date_to != null && !date_to.isEmpty()) ? ZonedDateTime.parse(date_to) : null;
            } catch (DateTimeParseException e) {
                result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
                result.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
                result.setStatusMessage("Invalid date format");
                return result;
            }

            // Get the dummy data
            List<Location> locations = dummyDataService.makeLocationList();

            // Filter the locations by last_updated date
            List<Location> filteredLocations = locations.stream()
            .filter(location -> {
                ZonedDateTime lastUpdated = ZonedDateTime.parse(location.getLastUpdated());
                boolean isAfterFromDate = fromDate == null || lastUpdated.isAfter(fromDate) || lastUpdated.isEqual(fromDate);
                boolean isBeforeToDate = toDate == null || lastUpdated.isBefore(toDate);
                return isAfterFromDate && isBeforeToDate;
            })
            .collect(Collectors.toList());

            // Apply offset and limit for pagination
            int start = Math.min(offset, filteredLocations.size());
            int end = Math.min(offset + limit, filteredLocations.size());
            List<Location> paginatedLocations = filteredLocations.subList(start, end);

            result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
            result.setStatusMessage("Success");
            result.setData(paginatedLocations);

            return result;
    }

    @GetMapping("/locations/{location_id}")
    @Operation(summary = "1-2. locations - Get location by ID", description = "Retrieve details of a specific location by its ID.")
    public ApiResponseObject<Location> getLocationById(
            @PathVariable @Parameter(description = "ID of the location", example = "PI-200006") String location_id) {

            ApiResponseObject<Location> result = new ApiResponseObject<>();

            // Get the dummy data
            List<Location> locations = dummyDataService.makeLocationList();

            // Find the location by ID
            Optional<Location> location = locations.stream()
                    .filter(loc -> loc.getId().equals(location_id))
                    .findFirst();

            result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            
            if (location.isPresent()) {
                result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
                result.setStatusMessage("Success");
                result.setData(location.get());
            } else {
                result.setStatusCode(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION);
                result.setStatusMessage("Location not found");
                result.setData(null);
            }

            return result;
    }

    @GetMapping("/locations/{location_id}/{evse_uid}")
    @Operation(summary = "1-3. locations - Get EVSE by ID", description = "Retrieve details of a specific EVSE by its UID within a location.")
    public ApiResponseObject<EVSE> getEvseById(
            @PathVariable @Parameter(description = "ID of the location", example = "PI-200006") String location_id,
            @PathVariable @Parameter(description = "UID of the EVSE", example = "PI-200006-2111") String evse_uid) {

            ApiResponseObject<EVSE> result = new ApiResponseObject<>();

            // Get the dummy data
            List<Location> locations = dummyDataService.makeLocationList();

            // Find the location by ID
            Optional<Location> locationOpt = locations.stream()
                    .filter(loc -> loc.getId().equals(location_id))
                    .findFirst();

            if (locationOpt.isPresent()) {
                Location location = locationOpt.get();
                // Find the EVSE by UID within the location
                Optional<EVSE> evseOpt = location.getEvses().stream()
                        .filter(evse -> evse.getUid().equals(evse_uid))
                        .findFirst();

                if (evseOpt.isPresent()) {
                    result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
                    result.setStatusMessage("Success");
                    result.setData(evseOpt.get());
                } else {
                    result.setStatusCode(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION);
                    result.setStatusMessage("EVSE not found");
                    result.setData(null);
                }
            } else {
                result.setStatusCode(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION);
                result.setStatusMessage("Location not found");
                result.setData(null);
            }

            result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            return result;
    }

    @GetMapping("/locations/{location_id}/{evse_uid}/{connector_id}")
    @Operation(summary = "1-4. locations - Get connector by ID", description = "Retrieve details of a specific connector by its ID within an EVSE.")
    public ApiResponseObject<Connector> getConnectorById(
            @PathVariable @Parameter(description = "ID of the location", example = "PI-200006") String location_id,
            @PathVariable @Parameter(description = "UID of the EVSE", example = "PI-200006-2111") String evse_uid,
            @PathVariable @Parameter(description = "검색할 커넥터의 connector_id ", example = "1") String connector_id) {

            ApiResponseObject<Connector> result = new ApiResponseObject<>();
            // Get the dummy data
            List<Location> locations = dummyDataService.makeLocationList();

            // Find the location by ID
            Optional<Location> locationOpt = locations.stream()
                    .filter(loc -> loc.getId().equals(location_id))
                    .findFirst();

            if (locationOpt.isPresent()) {
                Location location = locationOpt.get();
                // Find the EVSE by UID within the location
                Optional<EVSE> evseOpt = location.getEvses().stream()
                        .filter(evse -> evse.getUid().equals(evse_uid))
                        .findFirst();

                if (evseOpt.isPresent()) {
                    EVSE evse = evseOpt.get();
                    // Find the connector by ID within the EVSE
                    Optional<Connector> connectorOpt = evse.getConnectors().stream()
                            .filter(connector -> connector.getId().equals(connector_id))
                            .findFirst();

                    if (connectorOpt.isPresent()) {
                        result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
                        result.setStatusMessage("Success");
                        result.setData(connectorOpt.get());
                    } else {
                        result.setStatusCode(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION);
                        result.setStatusMessage("Connector not found");
                        result.setData(null);
                    }
                } else {
                    result.setStatusCode(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION);
                    result.setStatusMessage("EVSE not found");
                    result.setData(null);
                }
            } else {
                result.setStatusCode(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION);
                result.setStatusMessage("Location not found");
                result.setData(null);
            }

            result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            return result;
    }

    // #endregion
    // #region SESSIONS

    @GetMapping("/sessions/{emsp_contract_id}")
    @Operation(summary = "2-1. sessions - Find Session By eMSP  Contract Id", description = "eMSP contract Id에 대한 충전 Session을 조회합니다.")
    public ApiResponseObject<SessionForMb> getSessions(
        @PathVariable @Parameter(description = "eMSP Contract Id", example = "KRCEV001ABC6") String emsp_contract_id) {

        ApiResponseObject<SessionForMb> result = new ApiResponseObject<>();

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        if(emsp_contract_id == null || !emsp_contract_id.equals("KRCEV001ABC6")) {
            result.setStatusCode(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION);
            result.setStatusMessage("eMSP Contract not found");    
        } else {
            // Get the dummy data
            SessionForMb session = dummyDataService.makeSession();
            result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
            result.setStatusMessage("Success");
            result.setData(session);
        }  

        return result;
    }

    // #endregion
    // #region CDRS

    @GetMapping("/cdrs")
    @Operation(summary = "3-1. cdrs - get cdrs", description = "{date_from}에서 {date_to} 사이에 마지막으로 업데이트된 CDR을 가져온다")
    @Parameters({
        @Parameter(required=false, name = "date_from", description = "날짜/시간(포함) 이후 last_updated 있는 개체만 반환", example = "2024-06-01T06:12:31.401Z"),
        @Parameter(required=false, name = "date_to", description = "이 날짜/시간(제외) 까지 last_updated 있는 개체만 반환", example = "2024-06-04T06:12:31.401Z"),
        @Parameter(required=false, name = "offset", description = "반환된 첫 번째 개체의 오프셋", example = "0"),
        @Parameter(required=false, name = "limit", description = "GET할 최대 개체 수", example = "0"),
    })
    public ApiResponseObjectList<CdrForMb> getCdrs(
        @RequestParam(required = false, defaultValue = "") String date_from,
        @RequestParam(required = false, defaultValue = "") String date_to,
        @RequestParam(required = false, defaultValue = "0") Integer offset,
        @RequestParam(required = false, defaultValue = "10") Integer limit) {

        ApiResponseObjectList<CdrForMb> result = new ApiResponseObjectList<>();

            final ZonedDateTime fromDate;
            final ZonedDateTime toDate;
            try {
                fromDate = (date_from != null && !date_from.isEmpty()) ? ZonedDateTime.parse(date_from) : null;
                toDate = (date_to != null && !date_to.isEmpty()) ? ZonedDateTime.parse(date_to) : null;
            } catch (DateTimeParseException e) {
                result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
                result.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
                result.setStatusMessage("Invalid date format");
                return result;
            }

            List<CdrForMb> cdrs = dummyDataService.makeCdrList();

            List<CdrForMb> filteredCdrs = cdrs.stream()
                    .filter(cdr -> {
                        ZonedDateTime lastUpdated = ZonedDateTime.parse(cdr.getLastUpdated());
                        boolean isAfterFromDate = fromDate == null || lastUpdated.isAfter(fromDate) || lastUpdated.isEqual(fromDate);
                        boolean isBeforeToDate = toDate == null || lastUpdated.isBefore(toDate);
                        return isAfterFromDate && isBeforeToDate;
                    })
                    .collect(Collectors.toList());

            // Apply offset and limit for pagination
            int start = Math.min(offset, filteredCdrs.size());
            int end = Math.min(offset + limit, filteredCdrs.size());
            List<CdrForMb> paginatedCdrs = filteredCdrs.subList(start, end);

            result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
            result.setStatusMessage("Success");
            result.setData(paginatedCdrs);

            return result;
    }

    @GetMapping("/cdrs/{cdr_id}")
    @Operation(summary = "3-2. cdrs - get cdr", description = "cdr id로 특정되는 단일 cdr을 가져온다")
    public ApiResponseObject<CdrForMb> getCdr(
        @PathVariable @Parameter(description = "검색할 개체의 cdr_id", example = "adfc0c32-2c3e-44b7-a552-ea9fc7d4a71e") String cdr_id) {

        ApiResponseObject<CdrForMb> result = new ApiResponseObject<>();


            // Get the dummy data
            List<CdrForMb> cdrs = dummyDataService.makeCdrList();

            // Find the location by ID
            Optional<CdrForMb> cdr = cdrs.stream()
                    .filter(c -> c.getId().equals(cdr_id))
                    .findFirst();

            result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            
            if (cdr.isPresent()) {
                result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
                result.setStatusMessage("Success");
                result.setData(cdr.get());
            } else {
                result.setStatusCode(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION);
                result.setStatusMessage("CDR not found");
                result.setData(null);
            }

            return result;
    }

    @GetMapping("/cdrs/statistics")
    @Operation(summary = "3-3. cdrs - Statistics", description = "(date_from & date_to) by aggregating all transactions (CDRs) in this time frame.")
    @Parameters({
        @Parameter(
            required=false,
            name = "contract_ids",
            description = "Optional filter for CDRs by contractID(s) to receive statistics for one or multiple vehicles (contracts)",
            example = "[\"KRCEV001ABC6\", \"KRCEV001ABC7\", \"KRCEV001ABC8\"]",
            in = ParameterIn.QUERY,
            array = @ArraySchema(schema = @Schema(type = "string"))),
        @Parameter(required=false, name = "date_from", description = "날짜/시간(포함) 이후 last_updated 있는 개체만 반환", example = "2024-06-01T06:12:31.401Z"),
        @Parameter(required=false, name = "date_to", description = "이 날짜/시간(제외) 까지 last_updated 있는 개체만 반환", example = "2024-06-04T06:12:31.401Z"),
        @Parameter(required=false, name = "offset", description = "반환된 첫 번째 개체의 오프셋", example = "0"),
        @Parameter(required=false, name = "limit", description = "GET할 최대 개체 수", example = "0"),
    })
    public ApiResponseObject<CdrStatistics> getCdrStatistics(
        @RequestParam(required = false) List<String> contract_ids,
        @RequestParam(required = false, defaultValue = "0") String date_from,
        @RequestParam(required = false, defaultValue = "0") String date_to,
        @RequestParam(required = false, defaultValue = "0") Integer offset,
        @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        ApiResponseObject<CdrStatistics> result = new ApiResponseObject<>();

        final ZonedDateTime fromDate;
        final ZonedDateTime toDate;

        try {
            fromDate = (date_from != null && !date_from.isEmpty()) ? ZonedDateTime.parse(date_from) : null;
            toDate = (date_to != null && !date_to.isEmpty()) ? ZonedDateTime.parse(date_to) : null;
        } catch (DateTimeParseException e) {
            result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            result.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
            result.setStatusMessage("Invalid date format");
            return result;
        }

        List<CdrForMb> cdrs = dummyDataService.makeCdrList();
    
        List<CdrForMb> filteredCdrs = cdrs.stream()
            .filter(cdr -> {
                boolean dateMatches = true;
                if (fromDate != null || toDate != null) {
                    ZonedDateTime lastUpdated = ZonedDateTime.parse(cdr.getLastUpdated());
                    dateMatches = (fromDate == null || lastUpdated.isAfter(fromDate) || lastUpdated.isEqual(fromDate)) &&
                                (toDate == null || lastUpdated.isBefore(toDate));
                }
                boolean contractMatches = contract_ids == null || contract_ids.isEmpty() || contract_ids.contains(cdr.getCdrToken().getContractId());
                return dateMatches && contractMatches;
            })
            .collect(Collectors.toList());

        // Apply offset and limit for pagination
        int start = Math.min(offset, filteredCdrs.size());
        int end = Math.min(offset + limit, filteredCdrs.size());
        List<CdrForMb> paginatedCdrs = filteredCdrs.subList(start, end);

        double totalEnergy = paginatedCdrs.stream().mapToDouble(cdr -> cdr.getTotalEnergy().doubleValue()).sum();
        double totalTime = paginatedCdrs.stream().mapToDouble(cdr -> cdr.getTotalTime().doubleValue()).sum();
        double totalCostExclVat = paginatedCdrs.stream()
            .mapToDouble(cdr -> cdr.getTotalCost().getExclVat().doubleValue()).sum();
        double totalCostInclVat = paginatedCdrs.stream()
            .mapToDouble(cdr -> cdr.getTotalCost().getInclVat().doubleValue()).sum();
    
        double totalFixedCostExclVat = paginatedCdrs.stream()
            .mapToDouble(cdr -> cdr.getTotalFixedCost() != null ? cdr.getTotalFixedCost().getExclVat().doubleValue() : 0).sum();
        double totalFixedCostInclVat = paginatedCdrs.stream()
            .mapToDouble(cdr -> cdr.getTotalFixedCost() != null ? cdr.getTotalFixedCost().getInclVat().doubleValue() : 0).sum();
    
        double totalEnergyCostExclVat = paginatedCdrs.stream()
            .mapToDouble(cdr -> cdr.getTotalEnergyCost() != null ? cdr.getTotalEnergyCost().getExclVat().doubleValue() : 0).sum();
        double totalEnergyCostInclVat = paginatedCdrs.stream()
            .mapToDouble(cdr -> cdr.getTotalEnergyCost() != null ? cdr.getTotalEnergyCost().getInclVat().doubleValue() : 0).sum();
    
        double totalTimeCostExclVat = paginatedCdrs.stream()
            .mapToDouble(cdr -> cdr.getTotalTimeCost() != null ? cdr.getTotalTimeCost().getExclVat().doubleValue() : 0).sum();
        double totalTimeCostInclVat = paginatedCdrs.stream()
            .mapToDouble(cdr -> cdr.getTotalTimeCost() != null ? cdr.getTotalTimeCost().getInclVat().doubleValue() : 0).sum();
    
        int totalParkingTime = paginatedCdrs.stream().mapToInt(CdrForMb::getTotalParkingTime).sum();
    
        double totalParkingCostExclVat = paginatedCdrs.stream()
            .mapToDouble(cdr -> cdr.getTotalParkingCost() != null ? cdr.getTotalParkingCost().getExclVat().doubleValue() : 0).sum();
        double totalParkingCostInclVat = paginatedCdrs.stream()
            .mapToDouble(cdr -> cdr.getTotalParkingCost() != null ? cdr.getTotalParkingCost().getInclVat().doubleValue() : 0).sum();
    
        double totalReservationCostExclVat = paginatedCdrs.stream()
            .mapToDouble(cdr -> cdr.getTotalReservationCost() != null ? cdr.getTotalReservationCost().getExclVat().doubleValue() : 0).sum();
        double totalReservationCostInclVat = paginatedCdrs.stream()
            .mapToDouble(cdr -> cdr.getTotalReservationCost() != null ? cdr.getTotalReservationCost().getInclVat().doubleValue() : 0).sum();
    
        Price totalCost = new Price();
        totalCost.setExclVat(totalCostExclVat);
        totalCost.setInclVat(totalCostInclVat);
    
        Price totalFixedCost = new Price();
        totalFixedCost.setExclVat(totalFixedCostExclVat);
        totalFixedCost.setInclVat(totalFixedCostInclVat);
    
        Price totalEnergyCost = new Price();
        totalEnergyCost.setExclVat(totalEnergyCostExclVat);
        totalEnergyCost.setInclVat(totalEnergyCostInclVat);
    
        Price totalTimeCost = new Price();
        totalTimeCost.setExclVat(totalTimeCostExclVat);
        totalTimeCost.setInclVat(totalTimeCostInclVat);
    
        Price totalParkingCost = new Price();
        totalParkingCost.setExclVat(totalParkingCostExclVat);
        totalParkingCost.setInclVat(totalParkingCostInclVat);
    
        Price totalReservationCost = new Price();
        totalReservationCost.setExclVat(totalReservationCostExclVat);
        totalReservationCost.setInclVat(totalReservationCostInclVat);
    
        CdrStatistics statistics = new CdrStatistics();
        statistics.setCurrency("KRW");
        statistics.setTotalCost(totalCost);
        statistics.setTotalFixedCost(totalFixedCost);
        statistics.setTotalEnergy(totalEnergy);
        statistics.setTotalEnergyCost(totalEnergyCost);
        statistics.setTotalTime(totalTime);
        statistics.setTotalTimeCost(totalTimeCost);
        statistics.setTotalParkingTime(totalParkingTime);
        statistics.setTotalParkingCost(totalParkingCost);
        statistics.setTotalReservationCost(totalReservationCost);
    
        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
        result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        result.setStatusMessage("Success");
        result.setData(statistics);
    
        return result;
    }

    // #endregion
    // #region TARIFFS

    @GetMapping("/tariffs")
    @Operation(summary = "4-1. tariffs - get tariffs", description = "{date_from}에서 {date_to} 사이에 마지막으로 업데이트된 tariff 개체들을 가져온다.")
    @Parameters({
        @Parameter(required=false, name = "date_from", description = "날짜/시간(포함) 이후 last_updated 있는 개체만 반환", example = "2024-06-01T06:12:31.401Z"),
        @Parameter(required=false, name = "date_to", description = "이 날짜/시간(제외) 까지 last_updated 있는 개체만 반환", example = "2024-06-04T06:12:31.401Z"),
        @Parameter(required=false, name = "offset", description = "반환된 첫 번째 개체의 오프셋", example = "0"),
        @Parameter(required=false, name = "limit", description = "GET할 최대 개체 수", example = "0"),
    })
    public ApiResponseObjectList<Tariff> getTariffs(
        @RequestParam(required = false, defaultValue = "") String date_from,
        @RequestParam(required = false, defaultValue = "") String date_to,
        @RequestParam(required = false, defaultValue = "0") Integer offset,
        @RequestParam(required = false, defaultValue = "10") Integer limit)  {

        ApiResponseObjectList<Tariff> result = new ApiResponseObjectList<>();

        // Parse the date_from and date_to strings
        final ZonedDateTime fromDate;
        final ZonedDateTime toDate;
        try {
            fromDate = (date_from != null && !date_from.isEmpty()) ? ZonedDateTime.parse(date_from) : null;
            toDate = (date_to != null && !date_to.isEmpty()) ? ZonedDateTime.parse(date_to) : null;
        } catch (DateTimeParseException e) {
            result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            result.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
            result.setStatusMessage("Invalid date format");
            return result;
        }

        // Get the dummy data
        List<Tariff> tariffs = dummyDataService.makeTariffs();

        // Filter the tariffs by last_updated date
        List<Tariff> filteredTariffs = tariffs.stream()
                .filter(tariff -> {
                    ZonedDateTime lastUpdated = ZonedDateTime.parse(tariff.getLastUpdated());
                    boolean isAfterFromDate = fromDate == null || lastUpdated.isAfter(fromDate) || lastUpdated.isEqual(fromDate);
                    boolean isBeforeToDate = toDate == null || lastUpdated.isBefore(toDate);
                    return isAfterFromDate && isBeforeToDate;
                })
                .collect(Collectors.toList());

        // Apply offset and limit for pagination
        int start = Math.min(offset, filteredTariffs.size());
        int end = Math.min(offset + limit, filteredTariffs.size());
        List<Tariff> paginatedTariffs = filteredTariffs.subList(start, end);

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
        result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        result.setStatusMessage("Success");
        result.setData(paginatedTariffs);

        return result;
    }

    @GetMapping("/tariffs/{tariffId}")
    @Operation(summary = "4-2. tariffs - get tariff", description = "tariff_id로 특정되는 하나의 tariff를 가져온다.")
    public ApiResponseObject<Tariff> getTariff(
        @PathVariable @Parameter(description = "unique tariff id from emsp", example = "ABC10334908") String tariffId) {

        ApiResponseObject<Tariff> result = new ApiResponseObject<>();

        // Get the dummy data
        List<Tariff> tariffs = dummyDataService.makeTariffs();

        // Find the tariff by ID
        Optional<Tariff> tariffOpt = tariffs.stream()
                .filter(tariff -> tariff.getId().equals(tariffId))
                .findFirst();

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
        
        if (tariffOpt.isPresent()) {
            result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
            result.setStatusMessage("Success");
            result.setData(tariffOpt.get());
        } else {
            result.setStatusCode(OcpiResponseStatusCode.INSUFFICIENT_INFORMATION);
            result.setStatusMessage("Tariff not found");
            result.setData(null);
        }

        return result;
    }

    // #endregion
    // #region COMMANDS

    @PostMapping("/commands/START_SESSION")
    @Operation(summary = "5-1. commands - start session", description = "CPO에 원격 충전 시작 명령을 전송")
    public ApiResponseObject<CommandResponseForMb> startSession(HttpServletRequest httpRequest, @RequestBody StartSessionForMb request) {

        // 요청 URL 가져오기
        String requestUrl = httpRequest.getRequestURL().toString();

        String trackId = logService.controllerLogStart(requestUrl, request);

        ApiResponseObject<CommandResponseForMb> result = new ApiResponseObject<>();
        CommandResponseForMb resultData = new CommandResponseForMb();

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        String emspContractId = request.getEmspContractId();
        String evseUid = request.getEvseUid();
        String locationId = request.getLocationId();
        String connectorId = request.getConnectorId();

        try {
            ServiceResult<String> serviceResult = cpoService.validateContract(emspContractId, trackId);

            if (serviceResult.getSuccess()) {
                resultData.setResult(CommandResponseType.ACCEPTED);
                resultData.setLocationId(locationId);
                resultData.setEvseUid(evseUid);
                if (connectorId != null) {
                    resultData.setConnectorId(connectorId);
                }
                result.setData(resultData);
                result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
                result.setStatusMessage("Request received, processing initiated.");
            } else {
                resultData.setResult(CommandResponseType.UNKNOWN_SESSION);
                resultData.setLocationId(locationId);
                resultData.setEvseUid(evseUid);
                if (connectorId != null) {
                    resultData.setConnectorId(connectorId);
                }
                result.setStatusCode(OcpiResponseStatusCode.UNKNOWN_EMSP_CONTRACT_ID);
                result.setStatusMessage(serviceResult.getErrorMessage());
            }
        } catch (Exception e) {
            resultData.setResult(CommandResponseType.NOT_SUPPORTED);
            resultData.setLocationId(locationId);
            resultData.setEvseUid(evseUid);
            if (connectorId != null) {
                resultData.setConnectorId(connectorId);
            }
            result.setStatusCode(OcpiResponseStatusCode.INVALID_PARAMETER);
            result.setStatusMessage("Failure: Unknown error.");
            System.out.println(e.toString());
        }

        logService.controllerLogEnd(trackId, result.getStatusCode().equals(OcpiResponseStatusCode.SUCCESS), result.getStatusCode().toString(), result.getStatusMessage());

        result.setData(resultData);
        return result;
    }


    //     @PostMapping("/cert/ocsp-response-msg")
    // @Operation(summary = "1-3. OCSP 메시지 수신", description = """
    //         EVSE(CSMS) -> **MSP -> eMSP** -> ChargLink -> eMSP -> MSP -> EVSE(CSMS) <br><br>
    //         ChargLink : /cert/ocsp-response-msg 으로 OCSP 응답 메시지 요청 <br><br>
    //         응답으로 수신한 ocspResMessage를 MSP로 다시 보내준다.
    //         """)
    // public PncApiResponseObject getOcspResponseMessage(HttpServletRequest httpRequest, @RequestBody PncReqBodyOCSPMessage request) {

    //     // 요청 URL 가져오기
    //     String requestUrl = httpRequest.getRequestURL().toString();

    //     String trackId = logService.controllerLogStart(requestUrl, request);

    //     ServiceResult<OcspResponse> serviceResult = certificateService.ocspCertCheck(request, trackId);

    //     PncApiResponseObject response = new PncApiResponseObject();
    //     if (serviceResult.isFail()) {
    //         response.setCode(Integer.toString(serviceResult.getErrorCode()));
    //         response.setMessage(serviceResult.getErrorMessage());
    //         response.setResult(PncResponseResult.FAIL);
    //     } else {
    //         response.setCode("200");
    //         response.setMessage("OK");

    //         response.setResult(PncResponseResult.SUCCESS);

    //         response.setData(Optional.ofNullable(serviceResult.get()));
    //     }


    //    logService.controllerLogEnd(trackId, serviceResult.getSuccess(), response.getCode(), response.getData().orElse(null));
    //    return response;
    // }

    @PostMapping("/commands/STOP_SESSION")
    @Operation(summary = "5-2. commands - stop session", description = "CPO에 원격 충전 종료 명령을 전송")
    public ApiResponseObject<CommandResponseForMb> stopSession(@RequestBody StopSession request) {

        ApiResponseObject<CommandResponseForMb> result = new ApiResponseObject<>();
        CommandResponseForMb resultData = new CommandResponseForMb();

        resultData.setResult(CommandResponseType.ACCEPTED);
        resultData.setLocationId("PI-200006");
        resultData.setEvseUid("PI-200006-2111");
        result.setData(resultData);
        result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        result.setStatusMessage("Request received, processing initiated.");

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        return result;
    }

    // #endregion
    // #region TOKENS (폐기됨)

    // @GetMapping("/tokens/{country_code}/{party_id}/{token_uid}")
    // @Operation(summary = "5-1. tokens - get token", description = "토큰을 가져온다.")
    // @Parameters({
    //     @Parameter(name = "type", description = "AD_HOC_USER, APP_USER, OTHER, RFID", example = "RFID"),
    // })
    // public ApiResponseObjectList<Token> getToken(
    //     @PathVariable @Parameter(description = "국가코드", example = "KR") String country_code,
    //     @PathVariable @Parameter(description = "Party ID(Provider ID)", example = "CEV") String party_id,
    //     @PathVariable @Parameter(description = "token_uid ", example = "uid1234567890") String token_uid) {

    //     ApiResponseObjectList<Token> result = new ApiResponseObjectList<>();

    //     result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
    //     result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
    //     result.setStatusMessage("Success");        

    //     return result;
    // }

    // @PutMapping("/tokens/{country_code}/{party_id}/{token_uid}")
    // @Operation(summary = "5-2. tokens - put token", description = "새/업데이트 된 토큰 개체를 저장한다.")
    // @Parameters({
    //     @Parameter(name = "type", description = "AD_HOC_USER, APP_USER, OTHER, RFID", example = "RFID"),
    // })
    // public ApiResponseObject putToken(
    //     @PathVariable @Parameter(description = "국가코드", example = "KR") String country_code,
    //     @PathVariable @Parameter(description = "Party ID(Provider ID)", example = "CEV") String party_id,
    //     @PathVariable @Parameter(description = "token_uid ", example = "uid1234567890") String token_uid,
    //     @RequestBody Token request) {

    //     ApiResponseObject result = new ApiResponseObject<>();

    //     result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
    //     result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
    //     result.setStatusMessage("Success");        

    //     return result;
    // }

    // @PatchMapping("/tokens/{country_code}/{party_id}/{token_uid}")
    // @Operation(summary = "5-3. tokens - patch token", description = "토큰 개체 내 일부 업데이트")
    // @Parameters({
    //     @Parameter(name = "type", description = "AD_HOC_USER, APP_USER, OTHER, RFID", example = "RFID"),
    // })
    // public ApiResponseObject patchToken(
    //     @PathVariable @Parameter(description = "국가코드", example = "KR") String country_code,
    //     @PathVariable @Parameter(description = "Party ID(Provider ID)", example = "CEV") String party_id,
    //     @PathVariable @Parameter(description = "token_uid ", example = "uid1234567890") String token_uid,
    //     @RequestBody Token request) {

    //     ApiResponseObject result = new ApiResponseObject<>();

    //     result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
    //     result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
    //     result.setStatusMessage("Success");        

    //     return result;
    // }

    // #endregion

    // #region OCPI 표준 commands (폐기됨)

    // @PostMapping("/commands/START_SESSION")
    // @Operation(summary = "6-1. commands - start session", description = "CPO에 원격 충전 시작 명령을 전송")
    // public ApiResponseObject<CommandResponse> startSession(@RequestBody StartSession request) {

    //     return new ApiResponseObject<CommandResponse>();
    // }

    // @PostMapping("/commands/STOP_SESSION")
    // @Operation(summary = "6-2. commands - stop session", description = "CPO에 원격 충전 종료 명령을 전송")
    // public ApiResponseObject<CommandResponse> stopSession(@RequestBody StopSession request) {

    //     return new ApiResponseObject<CommandResponse>();
    // }

    // @PostMapping("/commands/RESERVE_NOW")
    // @Operation(summary = "6-3. commands - reverse session", description = "CPO에 원격 충전 예약 요청 명령을 전송")
    // public ApiResponseObject<CommandResponse> reserveSession(@RequestBody ReserveNow request) {

    //     return new ApiResponseObject<CommandResponse>();
    // }

    // @PostMapping("/commands/CANCEL_RESERVATION")
    // @Operation(summary = "6-4. commands - cancel reservation session", description = "CPO에 원격 충전 예약 취소 요청 명령을 전송")
    // public ApiResponseObject<CommandResponse> cancelReservationSession(@RequestBody CancelReservation request) {

    //     return new ApiResponseObject<CommandResponse>();
    // }

    // #endregion
    // #region OCPI 표준 SESSIONS (폐기됨)

    // @GetMapping("/sessions")
    // @Operation(summary = "2-1. sessions - get sessions", description = "{date_from}에서 {date_to} 사이에 마지막으로 업데이트된 충전 세션을 가져온다")
    // @Parameters({
    //     @Parameter(name = "date_from", description = "날짜/시간(포함) 이후 last_updated 있는 개체만 반환", example = "2024-06-01T06:12:31.401Z"),
    //     @Parameter(name = "date_to", description = "이 날짜/시간(제외) 까지 last_updated 있는 개체만 반환", example = "2024-06-04T06:12:31.401Z"),
    //     @Parameter(name = "offset", description = "반환된 첫 번째 개체의 오프셋", example = "0"),
    //     @Parameter(name = "limit", description = "GET할 최대 개체 수", example = "0"),
    // })
    // public ApiResponseObjectList<Session> getSessions(
    //     @RequestParam String date_from,
    //     @RequestParam String date_to,
    //     @RequestParam Integer offset,
    //     @RequestParam Integer limit) {

    //     ApiResponseObjectList<Session> result = new ApiResponseObjectList<>();

    //     result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
    //     result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
    //     result.setStatusMessage("Success");      

    //     return result;
    // }

    // @PutMapping("/sessions/{session_id}/charging_preferences")
    // @Operation(summary = "2-2. sessions - update session", description = "특정 충전 세션에 대한 운전자의 충전 기본 설정을 설정/업데이트")
    // public ApiResponseString putSession(
    //     @PathVariable @Parameter(description = "검색할 충전 개체의 session_id", example = "session123") String session_id,
    //     @RequestBody ChargingPreferences chargingPreferences) {

    //     ApiResponseString result = new ApiResponseString();

    //     result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
    //     result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
    //     result.setStatusMessage("Success");  
    //     result.setData("ACCEPTED");      

    //     return result;
    // }

        // #endregion

}
