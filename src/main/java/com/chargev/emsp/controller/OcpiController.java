package com.chargev.emsp.controller;

import com.chargev.emsp.model.dto.response.ApiResponseObject;
import com.chargev.emsp.model.dto.response.ApiResponseObjectList;
import com.chargev.emsp.model.dto.response.ApiResponseString;
import com.chargev.emsp.model.dto.ocpi.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

import org.apache.hc.core5.http.HttpStatus;

@RestController
@Slf4j
@RequestMapping("{version}/ocpi")
@Validated
@RequiredArgsConstructor
public class OcpiController {
    // @GetMapping(path ="credentials")
    // public ResponseEntity<Credentials> getCredentials(@PathVariable String version,  @RequestHeader(value = "Authorization", required=false) String headerStr) {
    //     if(headerStr == null) {
    //         headerStr = "";
    //     }
    //     headerStr = headerStr.replace("Token ", "");
    //     headerStr = headerStr.replace("token ", "");
    //     // decode base64 headerStr to string
        
    //     byte[] tokenByte = Base64.getDecoder().decode(headerStr);
    //     String token = new String(tokenByte);

    //     Credentials credentials = new Credentials();
    //     credentials.setToken("");

    //     HttpHeaders responseHeaders = new HttpHeaders();
    //     return new ResponseEntity<>(null, responseHeaders, HttpStatus.SC_UNAUTHORIZED);
    // }

    // @GetMapping(path ="test2")
    // public ResponseEntity<Credentials> testCredentials(@PathVariable String version,  @RequestHeader(value = "Authorization", required=true) String headerStr) {
    //     if(headerStr == null) {
    //         headerStr = "";
    //     }
    //     headerStr = headerStr.replace("Token ", "");
    //     headerStr = headerStr.replace("token ", "");
    //     // decode base64 headerStr to string
        
    //     // byte[] tokenByte = Base64.getDecoder().decode(headerStr);
    //     // String token = new String(tokenByte);

    //     Credentials credentials = new Credentials();
    //     credentials.setToken("");

    //     HttpHeaders responseHeaders = new HttpHeaders();
    //     return new ResponseEntity<>(null, responseHeaders, HttpStatus.SC_UNAUTHORIZED);
    // }


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

        return new ApiResponseObjectList<>();
    }

    @GetMapping("/locations/{location_id}")
    @Operation(summary = "1-2. locations - Get location by ID", description = "Retrieve details of a specific location by its ID.")
    public ApiResponseObject<Location> getLocationById(
            @PathVariable @Parameter(description = "ID of the location", example = "123") String locationId) {

        return new ApiResponseObject<>();
    }

    @GetMapping("/locations/{location_id}/{evse_uid}")
    @Operation(summary = "1-3. locations - Get EVSE by ID", description = "Retrieve details of a specific EVSE by its UID within a location.")
    public ApiResponseObject<EVSE> getEvseById(
            @PathVariable @Parameter(description = "ID of the location", example = "123") String locationId,
            @PathVariable @Parameter(description = "UID of the EVSE", example = "evse123") String evseUid) {

        return new ApiResponseObject<>();
    }

    @GetMapping("/locations/{location_id}/{evse_uid}/{connector_id}")
    @Operation(summary = "1-4. locations - Get connector by ID", description = "Retrieve details of a specific connector by its ID within an EVSE.")
    public ApiResponseObject<Connector> getConnectorById(
            @PathVariable @Parameter(description = "검색할 위치 개체의 location_id", example = "123") String locationId,
            @PathVariable @Parameter(description = "검색할 EVSE의 evse_uid", example = "evse123") String evseUid,
            @PathVariable @Parameter(description = "검색할 커넥터의 connector_id ", example = "connector456") String connectorId) {

        return new ApiResponseObject<>();
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

        return new ApiResponseObjectList<>();
    }

    @PutMapping("/sessions/{session_id}/charging_preferences")
    @Operation(summary = "2-2. sessions - update session", description = "특정 충전 세션에 대한 운전자의 충전 기본 설정을 설정/업데이트")
    public ApiResponseString putSession(
            @PathVariable @Parameter(description = "검색할 충전 개체의 session_id", example = "session123") String session_id,
            @RequestBody ChargingPreferences chargingPreferences) {

        return new ApiResponseString();
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

        return new ApiResponseObjectList<>();
    }

    @GetMapping("/tariffs")
    @Operation(summary = "3-1. tariffs - get tariffs", description = "{date_from}에서 {date_to} 사이에 마지막으로 업데이트된 CDR을 가져온다")
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

        return new ApiResponseObjectList<>();
    }

    @GetMapping("/tokens/{country_code}/{party_id}/{token_uid}")
    @Operation(summary = "4-1. tokens - get token", description = "토큰을 가져온다.")
    @Parameters({
        @Parameter(name = "type", description = "AD_HOC_USER, APP_USER, OTHER, RFID", example = "RFID"),
    })
    public ApiResponseObjectList<Tariff> getToken(
        @PathVariable @Parameter(description = "국가코드", example = "KR") String country_code,
        @PathVariable @Parameter(description = "Party ID(Provider ID)", example = "pv123") String party_id,
        @PathVariable @Parameter(description = "token_uid ", example = "uid1234567890") String token_uid) {

        return new ApiResponseObjectList<>();
    }

    @PutMapping("/tokens/{country_code}/{party_id}/{token_uid}")
    @Operation(summary = "4-2. tokens - put token", description = "새/업데이트 된 토큰 개체를 저장한다.")
    @Parameters({
        @Parameter(name = "type", description = "AD_HOC_USER, APP_USER, OTHER, RFID", example = "RFID"),
    })
    public ApiResponseObject putToken(
        @PathVariable @Parameter(description = "국가코드", example = "KR") String country_code,
        @PathVariable @Parameter(description = "Party ID(Provider ID)", example = "pv123") String party_id,
        @PathVariable @Parameter(description = "token_uid ", example = "uid1234567890") String token_uid,
        @RequestBody Token request) {

        return new ApiResponseObject();
    }

    @PatchMapping("/tokens/{country_code}/{party_id}/{token_uid}")
    @Operation(summary = "4-3. tokens - patch token", description = "토큰 개체 내 일부 업데이트")
    @Parameters({
        @Parameter(name = "type", description = "AD_HOC_USER, APP_USER, OTHER, RFID", example = "RFID"),
    })
    public ApiResponseObject patchToken(
        @PathVariable @Parameter(description = "국가코드", example = "KR") String country_code,
        @PathVariable @Parameter(description = "Party ID(Provider ID)", example = "pv123") String party_id,
        @PathVariable @Parameter(description = "token_uid ", example = "uid1234567890") String token_uid,
        @RequestBody Token request) {

        return new ApiResponseObject();
    }

    @PostMapping("/commands/START_SESSION")
    @Operation(summary = "5-1. commands - start session", description = "CPO에 원격 충전 시작 명령을 전송")
    public ApiResponseObject<CommandResponse> startSession(@RequestBody StartSession request) {

        return new ApiResponseObject<CommandResponse>();
    }

    @PostMapping("/commands/STOP_SESSION")
    @Operation(summary = "5-2. commands - stop session", description = "CPO에 원격 충전 종료 명령을 전송")
    public ApiResponseObject<CommandResponse> stopSession(@RequestBody StopSession request) {

        return new ApiResponseObject<CommandResponse>();
    }

    @PostMapping("/commands/RESERVE_NOW")
    @Operation(summary = "5-3. commands - reverse session", description = "CPO에 원격 충전 예약 요청 명령을 전송")
    public ApiResponseObject<CommandResponse> reserveSession(@RequestBody ReserveNow request) {

        return new ApiResponseObject<CommandResponse>();
    }

    @PostMapping("/commands/CANCEL_RESERVATION")
    @Operation(summary = "5-4. commands - cancel reservation session", description = "CPO에 원격 충전 예약 취소 요청 명령을 전송")
    public ApiResponseObject<CommandResponse> cancelReservationSession(@RequestBody CancelReservation request) {

        return new ApiResponseObject<CommandResponse>();
    }
}
