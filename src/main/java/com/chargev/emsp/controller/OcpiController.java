package com.chargev.emsp.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.model.dto.ocpi.Cdr;
import com.chargev.emsp.model.dto.ocpi.CdrStatistics;
import com.chargev.emsp.model.dto.ocpi.ChargingPreferences;
import com.chargev.emsp.model.dto.ocpi.CommandResponseForMb;
import com.chargev.emsp.model.dto.ocpi.CommandResponseType;
import com.chargev.emsp.model.dto.ocpi.Connector;
import com.chargev.emsp.model.dto.ocpi.EVSE;
import com.chargev.emsp.model.dto.ocpi.Location;
import com.chargev.emsp.model.dto.ocpi.Session;
import com.chargev.emsp.model.dto.ocpi.StartSessionForMb;
import com.chargev.emsp.model.dto.ocpi.Tariff;
import com.chargev.emsp.model.dto.ocpi.Token;
import com.chargev.emsp.model.dto.response.ApiResponseObject;
import com.chargev.emsp.model.dto.response.ApiResponseObjectList;
import com.chargev.emsp.model.dto.response.ApiResponseString;
import com.chargev.emsp.model.dto.response.OcpiResponseStatusCode;
import com.chargev.emsp.service.formatter.DateTimeFormatterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("{version}/ocpi")
@Validated
@RequiredArgsConstructor
public class OcpiController {
    private final DateTimeFormatterService dateTimeFormatterService;

    @GetMapping("/locations")
    @Operation(summary = "1-1. locations - locations list", description = "{date_from}에서 {date_to} 사이에 마지막으로 업데이트된 위치 목록을 가져온다.")
    @Parameters({
        @Parameter(name = "date_from", description = "날짜/시간(포함) 이후 last_updated 있는 개체만 반환", example = "2024-06-01T06:12:31.401Z"),
        @Parameter(name = "date_to", description = "이 날짜/시간(제외) 까지 last_updated 있는 개체만 반환", example = "2024-06-04T06:12:31.401Z"),
        @Parameter(name = "offset", description = "반환된 첫 번째 개체의 오프셋", example = "0"),
        @Parameter(name = "limit", description = "GET할 최대 개체 수", example = "0"),
    })
    public ApiResponseObjectList<Location> locations(
            @RequestParam String date_from,
            @RequestParam String date_to,
            @RequestParam Integer offset,
            @RequestParam Integer limit) {

            ApiResponseObjectList<Location> result = new ApiResponseObjectList<>();

            result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
            result.setStatusMessage("Success");      
    
            return result;
    }

    @GetMapping("/locations/{location_id}")
    @Operation(summary = "1-2. locations - Get location by ID", description = "Retrieve details of a specific location by its ID.")
    public ApiResponseObject<Location> getLocationById(
            @PathVariable @Parameter(description = "ID of the location", example = "123") String locationId) {

            ApiResponseObject<Location> result = new ApiResponseObject<>();

            result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
            result.setStatusMessage("Success");      
    
            return result;
    }

    @GetMapping("/locations/{location_id}/{evse_uid}")
    @Operation(summary = "1-3. locations - Get EVSE by ID", description = "Retrieve details of a specific EVSE by its UID within a location.")
    public ApiResponseObject<EVSE> getEvseById(
            @PathVariable @Parameter(description = "ID of the location", example = "123") String locationId,
            @PathVariable @Parameter(description = "UID of the EVSE", example = "evse123") String evseUid) {

            ApiResponseObject<EVSE> result = new ApiResponseObject<>();

            result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
            result.setStatusMessage("Success");      
    
            return result;
    }

    @GetMapping("/locations/{location_id}/{evse_uid}/{connector_id}")
    @Operation(summary = "1-4. locations - Get connector by ID", description = "Retrieve details of a specific connector by its ID within an EVSE.")
    public ApiResponseObject<Connector> getConnectorById(
            @PathVariable @Parameter(description = "검색할 위치 개체의 location_id", example = "123") String locationId,
            @PathVariable @Parameter(description = "검색할 EVSE의 evse_uid", example = "evse123") String evseUid,
            @PathVariable @Parameter(description = "검색할 커넥터의 connector_id ", example = "connector456") String connectorId) {

            ApiResponseObject<Connector> result = new ApiResponseObject<>();

            result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
            result.setStatusMessage("Success");      
    
            return result;
    }

    @GetMapping("/sessions")
    @Operation(summary = "2-1. sessions - get sessions", description = "{date_from}에서 {date_to} 사이에 마지막으로 업데이트된 충전 세션을 가져온다")
    @Parameters({
        @Parameter(name = "date_from", description = "날짜/시간(포함) 이후 last_updated 있는 개체만 반환", example = "2024-06-01T06:12:31.401Z"),
        @Parameter(name = "date_to", description = "이 날짜/시간(제외) 까지 last_updated 있는 개체만 반환", example = "2024-06-04T06:12:31.401Z"),
        @Parameter(name = "offset", description = "반환된 첫 번째 개체의 오프셋", example = "0"),
        @Parameter(name = "limit", description = "GET할 최대 개체 수", example = "0"),
    })
    public ApiResponseObjectList<Session> getSessions(
        @RequestParam String date_from,
        @RequestParam String date_to,
        @RequestParam Integer offset,
        @RequestParam Integer limit) {

        ApiResponseObjectList<Session> result = new ApiResponseObjectList<>();

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
        result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        result.setStatusMessage("Success");      

        return result;
    }

    @PutMapping("/sessions/{session_id}/charging_preferences")
    @Operation(summary = "2-2. sessions - update session", description = "특정 충전 세션에 대한 운전자의 충전 기본 설정을 설정/업데이트")
    public ApiResponseString putSession(
        @PathVariable @Parameter(description = "검색할 충전 개체의 session_id", example = "session123") String session_id,
        @RequestBody ChargingPreferences chargingPreferences) {

        ApiResponseString result = new ApiResponseString();

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
        result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        result.setStatusMessage("Success");  
        result.setData("ACCEPTED");      

        return result;
    }

    @GetMapping("/cdrs")
    @Operation(summary = "3-1. cdrs - get cdrs", description = "{date_from}에서 {date_to} 사이에 마지막으로 업데이트된 CDR을 가져온다")
    @Parameters({
        @Parameter(name = "date_from", description = "날짜/시간(포함) 이후 last_updated 있는 개체만 반환", example = "2024-06-01T06:12:31.401Z"),
        @Parameter(name = "date_to", description = "이 날짜/시간(제외) 까지 last_updated 있는 개체만 반환", example = "2024-06-04T06:12:31.401Z"),
        @Parameter(name = "offset", description = "반환된 첫 번째 개체의 오프셋", example = "0"),
        @Parameter(name = "limit", description = "GET할 최대 개체 수", example = "0"),
    })
    public ApiResponseObjectList<Cdr> getCdrs(
        @RequestParam String date_from,
        @RequestParam String date_to,
        @RequestParam Integer offset,
        @RequestParam Integer limit) {

        ApiResponseObjectList<Cdr> result = new ApiResponseObjectList<>();

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
        result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        result.setStatusMessage("Success");        

        return result;
    }

    @GetMapping("/cdrs/{cdr_id}")
    @Operation(summary = "3-2. cdrs - get cdr", description = "cdr id로 특정되는 단일 cdr을 가져온다")
    public ApiResponseObject<Cdr> getCdr(
        @PathVariable @Parameter(description = "검색할 개체의 cdr_id", example = "adfc0c32-2c3e-44b7-a552-ea9fc7d4a71e") String cdr_id) {

        ApiResponseObject<Cdr> result = new ApiResponseObject<>();

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
        result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        result.setStatusMessage("Success");        

        return result;
    }

    @GetMapping("/cdrs/statistics")
    @Operation(summary = "3-3. cdrs - Statistics", description = "(date_from & date_to) by aggregating all transactions (CDRs) in this time frame.")
    @Parameters({
        @Parameter(
            name = "contract_ids",
            description = "Optional filter for CDRs by contractID(s) to receive statistics for one or multiple vehicles (contracts)",
            example = "[\"KR*CEV*001ABC*6\", \"KR*CEV*001DEF*2\", \"KR*CEV*005XYZ*6\"]",
            in = ParameterIn.QUERY,
            array = @ArraySchema(schema = @Schema(type = "string"))),
        @Parameter(name = "date_from", description = "날짜/시간(포함) 이후 last_updated 있는 개체만 반환", example = "2024-06-01T06:12:31.401Z"),
        @Parameter(name = "date_to", description = "이 날짜/시간(제외) 까지 last_updated 있는 개체만 반환", example = "2024-06-04T06:12:31.401Z"),
    })
    public ApiResponseObject<CdrStatistics> getCdrStatistics(
        @RequestParam(required = false) List<String> contract_ids,
        @RequestParam(required = false) String date_from,
        @RequestParam(required = false) String date_to
    ) {
        ApiResponseObject<CdrStatistics> result = new ApiResponseObject<>();

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
        result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        result.setStatusMessage("Success");        

        return result;
    }

    @GetMapping("/tariffs")
    @Operation(summary = "4-1. tariffs - get tariffs", description = "{date_from}에서 {date_to} 사이에 마지막으로 업데이트된 CDR을 가져온다")
    @Parameters({
        @Parameter(name = "date_from", description = "날짜/시간(포함) 이후 last_updated 있는 개체만 반환", example = "2024-06-01T06:12:31.401Z"),
        @Parameter(name = "date_to", description = "이 날짜/시간(제외) 까지 last_updated 있는 개체만 반환", example = "2024-06-04T06:12:31.401Z"),
        @Parameter(name = "offset", description = "반환된 첫 번째 개체의 오프셋", example = "0"),
        @Parameter(name = "limit", description = "GET할 최대 개체 수", example = "0"),
    })
    public ApiResponseObjectList<Tariff> getTariffs(
        @RequestParam String date_from,
        @RequestParam String date_to,
        @RequestParam Integer offset,
        @RequestParam Integer limit) {

        ApiResponseObjectList<Tariff> result = new ApiResponseObjectList<>();

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
        result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        result.setStatusMessage("Success");        

        return result;
    }

    @GetMapping("/tokens/{country_code}/{party_id}/{token_uid}")
    @Operation(summary = "5-1. tokens - get token", description = "토큰을 가져온다.")
    @Parameters({
        @Parameter(name = "type", description = "AD_HOC_USER, APP_USER, OTHER, RFID", example = "RFID"),
    })
    public ApiResponseObjectList<Token> getToken(
        @PathVariable @Parameter(description = "국가코드", example = "KR") String country_code,
        @PathVariable @Parameter(description = "Party ID(Provider ID)", example = "CEV") String party_id,
        @PathVariable @Parameter(description = "token_uid ", example = "uid1234567890") String token_uid) {

        ApiResponseObjectList<Token> result = new ApiResponseObjectList<>();

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
        result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        result.setStatusMessage("Success");        

        return result;
    }

    @PutMapping("/tokens/{country_code}/{party_id}/{token_uid}")
    @Operation(summary = "5-2. tokens - put token", description = "새/업데이트 된 토큰 개체를 저장한다.")
    @Parameters({
        @Parameter(name = "type", description = "AD_HOC_USER, APP_USER, OTHER, RFID", example = "RFID"),
    })
    public ApiResponseObject putToken(
        @PathVariable @Parameter(description = "국가코드", example = "KR") String country_code,
        @PathVariable @Parameter(description = "Party ID(Provider ID)", example = "CEV") String party_id,
        @PathVariable @Parameter(description = "token_uid ", example = "uid1234567890") String token_uid,
        @RequestBody Token request) {

        ApiResponseObject result = new ApiResponseObject<>();

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
        result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        result.setStatusMessage("Success");        

        return result;
    }

    @PatchMapping("/tokens/{country_code}/{party_id}/{token_uid}")
    @Operation(summary = "5-3. tokens - patch token", description = "토큰 개체 내 일부 업데이트")
    @Parameters({
        @Parameter(name = "type", description = "AD_HOC_USER, APP_USER, OTHER, RFID", example = "RFID"),
    })
    public ApiResponseObject patchToken(
        @PathVariable @Parameter(description = "국가코드", example = "KR") String country_code,
        @PathVariable @Parameter(description = "Party ID(Provider ID)", example = "CEV") String party_id,
        @PathVariable @Parameter(description = "token_uid ", example = "uid1234567890") String token_uid,
        @RequestBody Token request) {

        ApiResponseObject result = new ApiResponseObject<>();

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
        result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        result.setStatusMessage("Success");        

        return result;
    }

    @PostMapping("/commands/START_SESSION")
    @Operation(summary = "6-1. commands - start session", description = "CPO에 원격 충전 시작 명령을 전송")
    public ApiResponseObject<CommandResponseForMb> startSession(@RequestBody StartSessionForMb request) {

        ApiResponseObject<CommandResponseForMb> result = new ApiResponseObject<>();
        CommandResponseForMb resultData = new CommandResponseForMb();

        result.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        String emspContractId = request.getEmsp_contract_id();
        String evseUid = request.getEvse_uid();
        String locationId = request.getLocation_id();
        String connectorId = request.getConnector_id();

        boolean isValidContractId = emspContractId.equals("KRCEVAA1234567");
        boolean isValidEvseUid = evseUid.equals("PI-200006-2111");
        boolean isValidLocationId = locationId.equals("PI-200006");

        if (isValidContractId && isValidEvseUid && isValidLocationId) {
            // 모두 성공
            resultData.setResult(CommandResponseType.ACCEPTED);
            resultData.setLocation_id("PI-200006");
            resultData.setEvse_uid("PI-200006-2111");
            if (connectorId != null) {
                resultData.setConnector_id(connectorId);
            }
            result.setData(resultData);
            result.setStatusCode(OcpiResponseStatusCode.SUCCESS);
            result.setStatusMessage("Request received, processing initiated.");
        } else {
            // 실패 처리
            if (!isValidContractId) {
                resultData.setResult(CommandResponseType.UNKNOWN_SESSION);
                result.setStatusCode(OcpiResponseStatusCode.UNKNOWN_EMSP_CONTRACT_ID);
                result.setStatusMessage("Failure: Invalid eMSP Contract ID.");
            } else if (!isValidEvseUid || !isValidLocationId) {
                resultData.setResult(CommandResponseType.REJECTED);
                result.setStatusCode(OcpiResponseStatusCode.ID_MISMATCH);
                result.setStatusMessage("Failure: Invalid EVSE or Location ID.");
            } else {
                resultData.setResult(CommandResponseType.NOT_SUPPORTED);
                result.setStatusCode(OcpiResponseStatusCode.INVALID_PARAMETER);
                result.setStatusMessage("Failure: Unknown error.");
            }
        }

        result.setData(resultData);
        return result;
    }

    // 아래가 OCPI 표준 "commands" 에 해당하는 end-point인데,
    // MB에서 표준과 다른 방식으로 사용하고 싶다고 하여 우선 표준으로 만들어 둔 부분은 주석 처리로 남겨두고 별도 MB용 엔드포인트 작성함

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
}
