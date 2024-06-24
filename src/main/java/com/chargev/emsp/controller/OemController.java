package com.chargev.emsp.controller;

import java.util.ArrayList;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.model.dto.oem.EmspAccount;
import com.chargev.emsp.model.dto.oem.EmspAccountRegistration;
import com.chargev.emsp.model.dto.oem.EmspContract;
import com.chargev.emsp.model.dto.oem.EmspRfidCard;
import com.chargev.emsp.model.dto.oem.EmspRfidCardModifyRequest;
import com.chargev.emsp.model.dto.oem.EmspRfidCardRequest;
import com.chargev.emsp.model.dto.oem.OemVehicleInfo;
import com.chargev.emsp.model.dto.response.ApiResponseObject;
import com.chargev.emsp.model.dto.response.ApiResponseObjectList;
import com.chargev.emsp.model.dto.response.ApiResponseString;
import com.chargev.emsp.model.dto.response.OcpiResponseStatusCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("{version}/oem")
@Validated
@RequiredArgsConstructor
public class OemController {

    @PostMapping("/registrations")
    @Operation(summary = "1-1. eMSP 회원 등록", description = "OEM 회원 정보와 매칭되는 eMSP 회원 등록(생성)")
    public ApiResponseObject<EmspAccount> registerAccount(@RequestBody EmspAccountRegistration request) 
    {
        ApiResponseObject<EmspAccount> response = new ApiResponseObject<>();

        // eMSP 가입 프로세스를 진행한다.
        
        // 응답바디 생성


        // 가입 성공했다면
        response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        response.setStatusMessage(OcpiResponseStatusCode.SUCCESS.toString());
        // 보내줘야 하는 데이터
        EmspAccount emspAccount = new EmspAccount();
        response.setData(emspAccount);

        return response;
    }

    @GetMapping("/accounts/{emsp_account_key}")
    @Operation(summary = "1-2. eMSP 회원 조회)", description = "eMSP 회원 정보 조회")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    public ApiResponseObject<EmspAccount> getAccount(@PathVariable("emsp_account_key") String emspAccountKey) {
        // eMSP 회원 조회 프로세스를 진행한다.

        // 응답바디 생성
        ApiResponseObject<EmspAccount> response = new ApiResponseObject<>();

        // 회원정보 조회 성공했다면
        response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        response.setStatusMessage("Success");
       // 보내줘야 하는 데이터
       EmspAccount emsp_account = new EmspAccount();
       response.setData(emsp_account);

        return response;
    }

    @PatchMapping("/accounts/{emsp_account_key}")
    @Operation(summary = "1-3. eMSP 회원 수정", description = "eMSP 회원 정보 변경")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    public ApiResponseObject<EmspAccount> patchAccount(@PathVariable("emsp_account_key") String emspAccountKey) {
        // eMSP 회원 변경 프로세스를 진행한다.
        // 실제로 변경할 내용은 body로 받아야 할 것 같은데, 아직 설계가 덜 끝난 것 같아서 body부분은 만들어놓지 않았음

        // 응답바디 생성
        ApiResponseObject<EmspAccount> response = new ApiResponseObject<>();

        // 회원정보 변경 성공했다면
        response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        response.setStatusMessage("Success");
        // 보내줘야 하는 데이터 (아직 미정. 가입시와 동일하게 Account 통째로 보내주는 것으로 작성해둠)
        EmspAccount emspAccount = new EmspAccount();
        response.setData(emspAccount);

        return response;
    }

    @DeleteMapping("/accounts/{emsp_account_key}")
    @Operation(summary = "1-4. eMSP 회원 탈퇴(삭제)", description = "eMSP 회원 정보 삭제")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    public ApiResponseString deleteAccount(@PathVariable("emsp_account_key") String emspAccountKey) {

        // eMSP 회원 삭제 프로세스를 진행한다.

        // 응답바디 생성
        ApiResponseString response = new ApiResponseString();

        // 회원정보 삭제 성공했다면
        response.setStatusCode(OcpiResponseStatusCode.SUCCESS);
        response.setStatusMessage("Success");
        response.setData("Deleted");

        return response;
    }

    @GetMapping("/accounts/{emsp_account_key}/contract")
    @Operation(summary = "2-1. 계약 목록 조회", description = "특정 회원의 계약 목록을 조회한다.")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    public ApiResponseObjectList<EmspContract> getContracts(@PathVariable("emsp_account_key") String emspAccountKey) {
        // emsp_account_key로 특정되는 회원의 계약 목록을 조회한다.

        ApiResponseObjectList<EmspContract> response = new ApiResponseObjectList<>();

        return response;
    }

    @PostMapping("/accounts/{emsp_account_key}/contracts")
    @Operation(summary = "2-2. 계약 생성", description = "입력 vin이 유효한 Contract를 가지고 있을 경우, Contract 생성")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    public ApiResponseObjectList<EmspContract> postContract(@PathVariable("emsp_account_key") String emspAccountKey,
        @RequestBody OemVehicleInfo vehicleInfo) {
        // emsp_account_key로 특정되는 회원의 계약 목록을 조회한다.

        ApiResponseObjectList<EmspContract> response = new ApiResponseObjectList<>();
        EmspContract conteContarct = new EmspContract();
        response.setData(new ArrayList<>());
        return response;
    }

    @GetMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}")
    @Operation(summary = "2-3. 계약 조회", description = "특정 회원의 특정 계약을 조회한다.")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789")
    public ApiResponseObject<EmspContract> getContract(
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId) {
        // emsp_account_key 특정되는 회원 emsp_contract_id로 특정되는 계약 단건을 조회한다.

        ApiResponseObject<EmspContract> response = new ApiResponseObject<>();

        return response;
    }

    @PatchMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}")
    @Operation(summary = "2-4. 계약 수정", description = "특정 회원의 특정 계약을 수정한다")
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
    @Operation(summary = "2-6. 계약 해지(삭제)", description = "특정 회원의 특정 계약을 해지(삭제)한다")
    @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789")
    @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789")
    public ApiResponseObject<EmspContract> deleteContract(
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId) {
        // emsp_account_key 특정되는 회원 emsp_contract_id로 특정되는 계약 단건을 해지(삭제)한다.

        ApiResponseObject<EmspContract> response = new ApiResponseObject<>();

        return response;
    }

    @GetMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/driver_group")
    @Operation(summary = "2-7. Driver Group 조회", description = "특정 Tariff 적용을 위한 Group 정보. 조회 시점에 해당 eMSP 계약에 1개의 DriverGroup이 있음")
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

    @GetMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids")
    @Operation(summary = "3-1. RFID 목록 조회", description = "특정 회원의 특정 계약에 속한 RFID 카드의 목록을 조회한다.")
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

    @PostMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids/issued")
    @Operation(summary = "3-2. RFID 발급 요청", description = "특정 회원의 특정 계약에 속한 RFID 카드를 생성한다")
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

    @GetMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids/issued/{issued_id}")
    @Operation(summary = "3-3. RFID 발급 요청 조회", description = "특정 회원의 특정 계약에 속한 RFID 카드를 생성 요청된 내용을 조회한다.")
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

    @PatchMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids/issued/{issued_id}")
    @Operation(summary = "3-4. RFID 발급 수정 요청", description = "특정 회원의 특정 계약에 속한 RFID 카드를 생성 요청된 내용을 수정한다.")
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
        // emsp_account_key 특정되는 회원 emsp_contract_id로 특정되는 계약 건 내에 특정 issued id로 생성 요청된 RFID 정보를 수정한다.

        ApiResponseObject<EmspRfidCard> response = new ApiResponseObject<>();

        return response;
    }

    @GetMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids/{card_id}")
    @Operation(summary = "3-3. RFID 조회", description = "특정 회원의 특정 계약에 속한 특정 RFID 카드 단건을 조회한다.")
    @Parameters({
        @Parameter(name = "emsp_account_key", description = "eMSP Account Key", example = "123456789"),
        @Parameter(name = "emsp_contract_id", description = "eMSP Contract Id", example = "123456789"),
        @Parameter(name = "card_id", description = "RFID Card Id", example = "123456789"),
    })
    public ApiResponseObject<EmspRfidCard> getRfid(
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId,
        @PathVariable("card_id") String cardId) {
        // emsp_account_key, emsp_contract_id, card_id로 특정되는 RFID 단건을 조회한다.

        ApiResponseObject<EmspRfidCard> response = new ApiResponseObject<>();

        return response;
    }

    @PatchMapping("/accounts/{emsp_account_key}/contracts/{emsp_contract_id}/rfids/{card_id}")
    @Operation(summary = "3-4. RFID 수정", description = "특정 회원의 특정 계약에 속한 특정 RFID 카드 정보를 수정한다.")
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
        @PathVariable("emsp_account_key") String emspAccountKey,
        @PathVariable("emsp_contract_id") String emspContractId,
        @PathVariable("card_id") String cardId) {
        // emsp_account_key, emsp_contract_id, card_id로 특정되는 RFID 단건의 정보를 삭제한다.

        ApiResponseString response = new ApiResponseString();

        return response;
    }
}
