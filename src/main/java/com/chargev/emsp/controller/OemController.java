package com.chargev.emsp.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.model.dto.oem.EmspAccount;
import com.chargev.emsp.model.dto.oem.EmspAccountModify;
import com.chargev.emsp.model.dto.oem.EmspAccountRegistration;
import com.chargev.emsp.model.dto.oem.EmspContract;
import com.chargev.emsp.model.dto.oem.EmspContractRequest;
import com.chargev.emsp.model.dto.oem.EmspRfidCard;
import com.chargev.emsp.model.dto.oem.EmspRfidCardModifyRequest;
import com.chargev.emsp.model.dto.oem.EmspRfidCardRequest;
import com.chargev.emsp.model.dto.response.ApiResponseObject;
import com.chargev.emsp.model.dto.response.ApiResponseObjectList;
import com.chargev.emsp.model.dto.response.ApiResponseString;
import com.chargev.emsp.model.dto.response.OcpiResponseStatusCode;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.formatter.DateTimeFormatterService;
import com.chargev.emsp.service.log.ControllerLogService;
import com.chargev.emsp.service.oem.AccountService;
import com.chargev.emsp.service.oem.ContractService;
import com.chargev.emsp.service.oem.RegistrationService;
import com.chargev.emsp.service.oem.RfidService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("{version}/oem")
@Validated
@RequiredArgsConstructor
public class OemController {

    private final RegistrationService registrationService;
    private final ControllerLogService logService;
    private final DateTimeFormatterService dateTimeFormatterService;
    private final AccountService accountService;
    private final ContractService contractService;
    private final RfidService rfidService;

    @PostMapping("/registrations")
    @Operation(summary = "1-1. eMSP 회원 등록", description = "OEM 회원 정보와 매칭되는 eMSP 회원 등록(생성)")
    public ApiResponseObject<EmspAccount> registerAccount(HttpServletRequest httpRequest,
                                                            @Valid @RequestBody EmspAccountRegistration request,
                                                            BindingResult bindingResult) 
    {
        // 로그 작성 시작
        String requestUrl = httpRequest.getRequestURL().toString();
        // TODO: 생성한 trackId를 checkpoint log로 사용할 수 있도록 service내부로 전달하는 것으로 추후 수정
        String trackId = logService.controllerLogStart(requestUrl, request);

        ApiResponseObject<EmspAccount> response = new ApiResponseObject<>();
        ServiceResult<EmspAccount> result = new ServiceResult<>();
        boolean apiResult = false;
        
        response.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        try {
            System.out.println("Starting account registration process");
            apiResult = registrationService.registerAccount(request, result);
            System.out.println("Account registration result: " + apiResult);
    
            // 추가적인 로그를 통해 result 객체의 상태를 확인
            System.out.println("ServiceResult success: " + result.getSuccess());
            System.out.println("ServiceResult error message: " + result.getErrorMessage());
        }
        // 트랜잭션의 가장 마지막 쓰기 단계에서 생긴 오류는 각 service result로 잡아낼 수 없으므로 catch에서 잡는다.
        catch (Exception ex) {
            System.out.println(ex.toString());
            // response.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
            // response.setStatusMessage("서버 오류로 인한 계정 생성 실패");
            // response.setData(null);

            // return response;
        }

        if (result.getSuccess() && apiResult) {
            response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
            response.setStatusMessage(OcpiResponseStatusCode.SUCCESS.toString());
            response.setData(result.get());
        } else {
            response.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
            response.setStatusMessage(result.getErrorMessage());
            response.setData(null);
        }

        logService.controllerLogEnd(trackId, apiResult, result.getErrorMessage());
        return response;
    }

    @PatchMapping("/accounts/{emsp_account_key}")
    @Operation(summary = "1-2. eMSP 회원 상태 변경", description = "eMSP 회원 정보 변경")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    public ApiResponseObject<EmspAccount> patchAccount(HttpServletRequest httpRequest,
                                                        @PathVariable("emsp_account_key") String emspAccountKey,
                                                        @Valid @RequestBody EmspAccountModify request) {
        // eMSP 회원 변경 프로세스를 진행한다.
        String requestUrl = httpRequest.getRequestURL().toString();
        String trackId = logService.controllerLogStart(requestUrl, emspAccountKey);

        // 응답바디 생성
        ApiResponseObject<EmspAccount> response = new ApiResponseObject<>();
        response.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        try {
            ServiceResult<EmspAccount> result = accountService.modifyAccountById(emspAccountKey, request);

            if (result.getSuccess()) {
                response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
                response.setStatusMessage(OcpiResponseStatusCode.SUCCESS.toString());
                response.setData(result.get());
            } else {
                response.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
                response.setStatusMessage(result.getErrorMessage());
                response.setData(null);
            }
        } catch (Exception e) {
            response.setStatusCode(OcpiResponseStatusCode.SERVER_ERROR);
            response.setStatusMessage(e.toString());
            response.setData(null);
            System.out.println(e.toString());
        }

        logService.controllerLogEnd(trackId, response.getStatusCode().equals(OcpiResponseStatusCode.SUCCESS), response.getStatusCode().toString());
        return response;
    }

    @DeleteMapping("/accounts/{emsp_account_key}")
    @Operation(summary = "1-3. eMSP 회원 탈퇴(삭제)", description = "eMSP 회원 정보 삭제")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    public ApiResponseString deleteAccount(HttpServletRequest httpRequest, @PathVariable("emsp_account_key") String emspAccountKey) {

        // eMSP 회원 삭제 프로세스를 진행한다.
        String requestUrl = httpRequest.getRequestURL().toString();
        String trackId = logService.controllerLogStart(requestUrl, emspAccountKey);

        // 응답바디 생성
        ApiResponseString response = new ApiResponseString();
        response.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        try {
            ServiceResult<Void> result = accountService.deleteAccountById(emspAccountKey);

            if (result.getSuccess()) {
                response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
                response.setStatusMessage(OcpiResponseStatusCode.SUCCESS.toString());
                response.setData("Deleted");
            } else {
                response.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
                response.setStatusMessage(result.getErrorMessage());
                response.setData(null);
            }
        } catch (Exception e) {
            response.setStatusCode(OcpiResponseStatusCode.SERVER_ERROR);
            response.setStatusMessage(e.toString());
            response.setData(null);
            System.out.println(e.toString());
        }

        logService.controllerLogEnd(trackId, response.getStatusCode().equals(OcpiResponseStatusCode.SUCCESS), response.getStatusCode().toString());

        return response;
    }

    @GetMapping("/accounts/{emsp_account_key}")
    @Operation(summary = "1-4. eMSP 회원 조회", description = "eMSP 회원 정보 조회")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    public ApiResponseObject<EmspAccount> getAccount(HttpServletRequest httpRequest, @PathVariable("emsp_account_key") String emspAccountKey) {
        String requestUrl = httpRequest.getRequestURL().toString();
        String trackId = logService.controllerLogStart(requestUrl, emspAccountKey);

        ApiResponseObject<EmspAccount> response = new ApiResponseObject<>();
        response.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        try {
            ServiceResult<EmspAccount> result = accountService.getAccountById(emspAccountKey);

            if (result.getSuccess()) {
                response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
                response.setStatusMessage(OcpiResponseStatusCode.SUCCESS.toString());
                response.setData(result.get());
            } else {
                response.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
                response.setStatusMessage(result.getErrorMessage());
                response.setData(null);
            }
        } catch (Exception e) {
            response.setStatusCode(OcpiResponseStatusCode.SERVER_ERROR);
            response.setStatusMessage(e.toString());
            response.setData(null);
            System.out.println(e.toString());
        }

        logService.controllerLogEnd(trackId, response.getStatusCode().equals(OcpiResponseStatusCode.SUCCESS), response.getStatusCode().toString());
        return response;
    }

    @PostMapping("/accounts/{emsp_account_key}/contracts")
    @Operation(summary = "2-1. 계약 등록", description = "입력 vin이 유효한 Contract를 가지고 있을 경우, 기존 contract 삭제 후 생성")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    public ApiResponseObject<EmspContract> postContract(HttpServletRequest httpRequest,
                                                @PathVariable("emsp_account_key") String emspAccountKey,
                                                @Valid @RequestBody EmspContractRequest contractRequest) {

        String requestUrl = httpRequest.getRequestURL().toString();
        String trackId = logService.controllerLogStart(requestUrl, emspAccountKey);

        ApiResponseObject<EmspContract> response = new ApiResponseObject<>();
        response.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        try {
            ServiceResult<EmspContract> result = contractService.createContract(emspAccountKey, contractRequest);

            if (result.getSuccess()) {
                response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
                response.setStatusMessage(OcpiResponseStatusCode.SUCCESS.toString());
                response.setData(result.get());
            } else {
                response.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
                response.setStatusMessage(result.getErrorMessage());
                response.setData(null);
            }
        } catch (Exception e) {
            response.setStatusCode(OcpiResponseStatusCode.SERVER_ERROR);
            response.setStatusMessage(e.toString());
            response.setData(null);
            System.out.println(e.toString());
        }

        logService.controllerLogEnd(trackId, response.getStatusCode().equals(OcpiResponseStatusCode.SUCCESS), response.getStatusCode().toString());
        return response;
    }

    @PatchMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}")
    @Operation(summary = "2-2. 계약 수정", description = "특정 회원의 특정 계약을 수정한다")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789")
    public ApiResponseObject<EmspContract> patchContract(
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId,
        @RequestBody EmspContract contract) {
        // emsp_account_key 특정되는 회원 emsp_contract_id로 특정되는 계약 단건의 정보를 변경한다.

        ApiResponseObject<EmspContract> response = new ApiResponseObject<>();

        return response;
    }

    @DeleteMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}")
    @Operation(summary = "2-3. 계약 해지(삭제)", description = "특정 회원의 특정 계약을 해지(삭제)한다")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789")
    public ApiResponseObject<EmspContract> deleteContract(
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId) {
        // emsp_account_key 특정되는 회원 emsp_contract_id로 특정되는 계약 단건을 해지(삭제)한다.

        ApiResponseObject<EmspContract> response = new ApiResponseObject<>();

        return response;
    }

    @GetMapping("/accounts/{emsp_account_key}/contracts")
    @Operation(summary = "2-4. 계약 목록 조회", description = "특정 회원의 계약 목록을 조회")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    public ApiResponseObjectList<EmspContract> getContracts(HttpServletRequest httpRequest, @PathVariable("emsp_account_key") String emspAccountKey) {
        // emsp_account_key로 특정되는 회원의 계약 목록을 조회한다.
        String requestUrl = httpRequest.getRequestURL().toString();
        String trackId = logService.controllerLogStart(requestUrl, emspAccountKey);

        ApiResponseObjectList<EmspContract> response = new ApiResponseObjectList<>();
        response.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        try {
            ServiceResult<List<EmspContract>> result = contractService.getContractsByAccountKey(emspAccountKey);

            if (result.getSuccess()) {
                response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
                response.setStatusMessage(OcpiResponseStatusCode.SUCCESS.toString());
                response.setData(result.get());
            } else {
                response.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
                response.setStatusMessage(result.getErrorMessage());
                response.setData(null);
            }
        } catch (Exception e) {
            response.setStatusCode(OcpiResponseStatusCode.SERVER_ERROR);
            response.setStatusMessage(e.toString());
            response.setData(null);
            System.out.println(e.toString());
        }

        logService.controllerLogEnd(trackId, response.getStatusCode().equals(OcpiResponseStatusCode.SUCCESS), response.getStatusCode().toString());
        return response;
    }

    @GetMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}")
    @Operation(summary = "2-5. 계약 조회", description = "특정 회원의 특정 계약을 조회한다.")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789")
    public ApiResponseObject<EmspContract> getContract(
        HttpServletRequest httpRequest,
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId) {
        // emsp_account_key 특정되는 회원 emsp_contract_id로 특정되는 계약 단건을 조회한다.
        String requestUrl = httpRequest.getRequestURL().toString();
        String trackId = logService.controllerLogStart(requestUrl, emspAccountKey);

        ApiResponseObject<EmspContract> response = new ApiResponseObject<>();
        response.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        try {
            ServiceResult<EmspContract> result = contractService.getContract(emspAccountKey, emspContractId);

            if (result.getSuccess()) {
                response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
                response.setStatusMessage(OcpiResponseStatusCode.SUCCESS.toString());
                response.setData(result.get());
            } else {
                response.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
                response.setStatusMessage(result.getErrorMessage());
                response.setData(null);
            }
        } catch (Exception e) {
            response.setStatusCode(OcpiResponseStatusCode.SERVER_ERROR);
            response.setStatusMessage(e.toString());
            response.setData(null);
            System.out.println(e.toString());
        }

        logService.controllerLogEnd(trackId, response.getStatusCode().equals(OcpiResponseStatusCode.SUCCESS), response.getStatusCode().toString());
        return response;
    }

    @GetMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/driver_group")
    @Operation(summary = "2-6. Driver Group 조회", description = "특정 Tariff 적용을 위한 Group 정보. 조회 시점에 해당 eMSP 계약에 1개의 DriverGroup이 있음")
    @Parameters({
        @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789"),
        @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789"),
    })
    public ApiResponseObject<EmspContract> getDriverGroupByContract(
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId) {
        // emsp_account_key 특정되는 회원 emsp_contract_id로 특정되는 DriverGroup을 조회한다.

        ApiResponseObject<EmspContract> response = new ApiResponseObject<>();

        return response;
    }

    @PostMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids/orders")
    @Operation(summary = "3-1. RFID 발급 요청", description = "특정 회원의 특정 계약에 속한 RFID 카드를 생성한다")
    @Parameters({
        @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789"),
        @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789"),
    })
    public ApiResponseObject<EmspRfidCard> postRfids(
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId,
        @RequestBody EmspRfidCardRequest request) {
        // emsp_account_key 특정되는 회원 emsp_contract_id로 특정되는 계약에 RFID를 생성한다.
        // CPO로 부터 RFID 유효성 확인하며, 
        //이상이 없으면 정보를 저장하고 CPO로 RFID 사용을 알린다.

        ApiResponseObject<EmspRfidCard> response = new ApiResponseObject<>();

        return response;
    }

    @GetMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids/order/{issued_id}")
    @Operation(summary = "3-2. RFID 발급 요청 조회", description = "특정 회원의 특정 계약에 속한 RFID 카드를 생성 요청된 내용을 조회한다.")
    @Parameters({
        @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789"),
        @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789"),
        @Parameter(name = "issued_id", description = "RFID Issued Id", example = "123456789"),
    })
    public ApiResponseObject<EmspRfidCard> getRfidIssued(
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId,
        @PathVariable("issued_id") String issuedId,
        @RequestBody EmspRfidCardRequest request) {
        // emsp_account_key 특정되는 회원 emsp_contract_id로 특정되는 계약 건 내에 특정 issued id로 생성 요청된 RFID 정보를 조회한다.

        ApiResponseObject<EmspRfidCard> response = new ApiResponseObject<>();

        return response;
    }

    @PostMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids")
    @Operation(summary = "3-3. RFID 등록 요청", description = "RFID 등록 요청 : Registration 시 RFID 등록 / RFID issued 로 RFID 등록")
    @Parameters({
        @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789"),
        @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789"),
        @Parameter(name = "issued_id", description = "RFID Issued Id", example = "123456789"),
    })
    public ApiResponseObject<EmspRfidCard> patchRfidIssued(
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId,
        @PathVariable("issued_id") String issuedId,
        @RequestBody EmspRfidCardRequest request) {
        // api 확정 전

        ApiResponseObject<EmspRfidCard> response = new ApiResponseObject<>();

        return response;
    }

    @PatchMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids/{card_id}")
    @Operation(summary = "3-4. RFID 상태 변경", description = "특정 회원의 특정 계약에 속한 특정 RFID 카드 정보를 수정")
    @Parameters({
        @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789"),
        @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789"),
        @Parameter(name = "card_id", description = "RFID Card Id", example = "123456789"),
    })
    public ApiResponseObject<EmspRfidCard> patchRfid(
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId,
        @PathVariable("card_id") String cardId,
        @RequestBody EmspRfidCardModifyRequest rfid_card) {
        // emsp_account_key, emsp_contract_id, card_id로 특정되는 RFID 단건의 정보를 수정한다.
        // eMSP RFID 분실신고/취소 요청한다.
        // 분실신고 요청은 정상 상태의 RFID에 신청 가능하다.
        // 분실신고 취소는 분실신고 신청된 RFID에 취소 가능하다.
        // 분실신고된 RFID로 충전 인증이 불가하다.
        // CPO로 분실신고/취소 정보를 전달해야 한다.

        ApiResponseObject<EmspRfidCard> response = new ApiResponseObject<>();

        return response;
    }

    @DeleteMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids/{card_id}")
    @Operation(summary = "3-5. RFID 삭제", description = "특정 회원의 특정 계약에 속한 특정 RFID 카드 정보를 삭제한다.")
    @Parameters({
        @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789"),
        @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789"),
        @Parameter(name = "card_id", description = "RFID Card Id", example = "123456789"),
    })
    public ApiResponseString deleteRfid(
        HttpServletRequest httpRequest,
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId,
        @PathVariable("card_id") String cardId) {
        // emsp_account_key, emsp_contract_id, card_id로 특정되는 RFID 단건의 정보를 삭제한다.
        String requestUrl = httpRequest.getRequestURL().toString();
        String trackId = logService.controllerLogStart(requestUrl, emspAccountKey);

        ApiResponseString response = new ApiResponseString();
        response.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        try {
            ServiceResult<Void> result = rfidService.deleteRfidById(emspAccountKey, emspContractId, cardId);

            if (result.getSuccess()) {
                response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
                response.setStatusMessage(OcpiResponseStatusCode.SUCCESS.toString());
                response.setData("Deleted");
            } else {
                response.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
                response.setStatusMessage(result.getErrorMessage());
                response.setData(null);
            }
        } catch (Exception e) {
            response.setStatusCode(OcpiResponseStatusCode.SERVER_ERROR);
            response.setStatusMessage(e.toString());
            response.setData(null);
            System.out.println(e.toString());
        }

        logService.controllerLogEnd(trackId, response.getStatusCode().equals(OcpiResponseStatusCode.SUCCESS), response.getStatusCode().toString());

        return response;
    }

    @GetMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids")
    @Operation(summary = "3-6. RFID 목록 조회", description = "특정 회원의 특정 계약에 속한 RFID 카드의 목록을 조회한다.")
    @Parameters({
        @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789"),
        @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789"),
    })
    public ApiResponseObjectList<EmspRfidCard> getRfids(
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId) {
        // emsp_account_key 특정되는 회원 emsp_contract_id로 특정되는 RFID 목록을 조회한다.

        ApiResponseObjectList<EmspRfidCard> response = new ApiResponseObjectList<>();

        return response;
    }

    @GetMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids/{card_id}")
    @Operation(summary = "3-5. RFID 조회", description = "특정 회원의 특정 계약에 속한 특정 RFID 카드 단건을 조회한다.")
    @Parameters({
        @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789"),
        @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789"),
        @Parameter(name = "card_id", description = "RFID Card Id", example = "123456789"),
    })

    public ApiResponseObject<EmspRfidCard> getRfid(
        HttpServletRequest httpRequest,
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId,
        @PathVariable("card_id") String cardId) {
        // emsp_account_key, emsp_contract_id, card_id로 특정되는 RFID 단건을 조회한다.

        String requestUrl = httpRequest.getRequestURL().toString();
        String trackId = logService.controllerLogStart(requestUrl, emspAccountKey);

        ApiResponseObject<EmspRfidCard> response = new ApiResponseObject<>();
        response.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));

        try {
            // 계약을 먼저 조회
            ServiceResult<EmspContract> contractResult = contractService.getContract(emspAccountKey, emspContractId);

            if (contractResult.getSuccess()) {
                // 계약에서 RFID 카드 목록을 가져옴
                EmspRfidCard rfidCard = contractResult.get().getRfidCard();
                if(rfidCard.getCardId().equals(cardId) && rfidCard.getStatus().equals("Active")) {
                    response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
                    response.setStatusMessage(OcpiResponseStatusCode.SUCCESS.toString());
                    response.setData(rfidCard);
                } else {
                    response.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
                    response.setStatusMessage("RFID 카드가 존재하지 않거나 상태가 유효하지 않습니다.");
                    response.setData(null);
                }
            } else {
                response.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
                response.setStatusMessage(contractResult.getErrorMessage());
                response.setData(null);
            }
        } catch (Exception e) {
            response.setStatusCode(OcpiResponseStatusCode.SERVER_ERROR);
            response.setStatusMessage(e.toString());
            response.setData(null);
            System.out.println(e.toString());
        }

        logService.controllerLogEnd(trackId, response.getStatusCode().equals(OcpiResponseStatusCode.SUCCESS), response.getStatusCode().toString());
        return response;
    }

        @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseObject<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        ApiResponseObject<Object> response = new ApiResponseObject<>();
        response.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
        response.setStatusCode(OcpiResponseStatusCode.CLIENT_ERROR);
        response.setStatusMessage("요청값 에러: " + ex.getMessage());
        response.setData(null);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // @PatchMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids/issued/{issued_id}")
    // @Operation(summary = "3-4. RFID 발급 수정 요청", description = "특정 회원의 특정 계약에 속한 RFID 카드를 생성 요청된 내용을 수정한다.")
    // @Parameters({
    //     @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789"),
    //     @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789"),
    //     @Parameter(name = "issued_id", description = "RFID Issued Id", example = "123456789"),
    // })
    // public ApiResponseObject<EmspRfidCard> patchRfidIssued(
    //     @PathVariable("emsp_account_key") String emspAccountKey,
    //     @PathVariable("emsp_contract_id") String emspContractId,
    //     @PathVariable("issued_id") String issuedId,
    //     @RequestBody EmspRfidCardRequest request) {
    //     // emsp_account_key 특정되는 회원 emsp_contract_id로 특정되는 계약 건 내에 특정 issued id로 생성 요청된 RFID 정보를 수정한다.

    //     ApiResponseObject<EmspRfidCard> response = new ApiResponseObject<>();

    //     return response;
    // }


}
