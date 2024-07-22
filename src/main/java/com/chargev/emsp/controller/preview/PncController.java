package com.chargev.emsp.controller.preview;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.model.dto.pnc.CheckRequest;
import com.chargev.emsp.model.dto.pnc.ContractInfo;
import com.chargev.emsp.model.dto.pnc.ContractStatus;
import com.chargev.emsp.model.dto.pnc.OcspResponse;
import com.chargev.emsp.model.dto.pnc.PncReqBodyAuthorize;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractInfo;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractSuspension;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueContract;
import com.chargev.emsp.model.dto.pnc.PncReqBodyOCSPMessage;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeContractCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyVerifyOcsp;
import com.chargev.emsp.model.dto.response.KpipApiResponse;
import com.chargev.emsp.model.dto.response.PncApiResponse;
import com.chargev.emsp.model.dto.response.PncApiResponseObject;
import com.chargev.emsp.model.dto.response.PncResponseResult;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.log.ControllerLogService;
import com.chargev.emsp.service.preview.PreviewCertificationService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
// @RequestMapping("{version}/test/pnc")
@RequestMapping("{version}/pnc")
@Validated
@RequiredArgsConstructor
public class PncController {
    private final ControllerLogService logService;
    private final PreviewCertificationService certificateService;
    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");

    // KEPCO가 사용하는 API
    @PostMapping("/provisioning/suspension")
    @Operation(summary = "0. OEM 프로비저닝 변동으로 인한 계약 삭제", description = """
            OEM -> **ChargeLink -> eMSP** -> kafka <br><br>
            kafka : [MSG-EMSP-PNC-CONTRACT] 로 변경된 계약 정보 전송
            """)
    public KpipApiResponse suspension(HttpServletRequest httpRequest,
            @RequestBody PncReqBodyContractSuspension request) {

        // 요청 URL 가져오기
        String requestUrl = httpRequest.getRequestURL().toString();

        String trackId = logService.controllerLogStart(requestUrl, request);

        // OEM 프로비저닝 변경으로 생성된 기존 계약을 삭제(만료)처리해야 하는 상황을 KEPCO가 알려줄 때 호출함.
        // 들어온 body의 pcid에 해당하는 계약을 삭제(만료) 시키고 그에 따른 응답을 반환한다.

        // 해당 api만 다른 response 형식을 사용 (KPIP와 통일)
        KpipApiResponse response = new KpipApiResponse();

        ServiceResult<String> result = certificateService.suspension(request, trackId);

        if (result.getSuccess()) {
            response.setResultCode("OK");
            response.setResultMsg("Success");
        } else {
            response.setResultCode("FAIL");
            response.setResultMsg(result.getErrorMessage());
        }

        logService.controllerLogEnd(trackId, result.getSuccess(), response.getResultCode());
        return response;
    }

    @PostMapping("/cert/issuance")
    @Operation(summary = "1-1. EVSE 및 CSMS 인증서 발급/갱신", description = """
            EVSE(CSMS) -> **MSP -> eMSP** -> ChargLink -> eMSP -> kafka <br><br>
            ChargLink : /cert/issuance 로 인증서 발급 요청 전송 <br><br>
            kafka : [MSG-EMSP-EVSE-CERTIFICATE] 로 발급된 인증서 정보 전송<br><br>
            Result가 SUCCESS인 경우, 폴링시 사용할 고유id를 Message로 리턴. 이 id는 leaf 인증서에 대응.
            """)
    public PncApiResponse issueCert(HttpServletRequest httpRequest, @RequestBody PncReqBodyIssueCert request) {

        // 요청 URL 가져오기
        String requestUrl = httpRequest.getRequestURL().toString();
        String trackId = logService.controllerLogStart(requestUrl, request);

        ServiceResult<String> serviceResult = certificateService.issueCertificate(request, trackId);

        PncApiResponse response = new PncApiResponse();
        buildAsyncBaseResponse(serviceResult, response);

        logService.controllerLogEnd(trackId, serviceResult.getSuccess(), response.getCode());

        return response;
    }

    @PostMapping("/cert/revoke")
    @Operation(summary = "1-2. EVSE 인증서 폐기", description = """
            EVSE(CSMS) -> **MSP -> eMSP** -> ChargLink -> eMSP -> kafka <br><br>
            ChargLink : /cert/revocation 으로 인증서 폐기 요청 전송 <br><br>
            kafka : [MSG-EMSP-EVSE-CERTIFICATE] 로 인증서 폐기 상태 업데이트<br><br>
            Result가 SUCCESS인 경우, 폴링시 사용할 고유id를 Message로 리턴. 이 id는 leaf 인증서에 대응.
            """)
    public PncApiResponse revokeCert(HttpServletRequest httpRequest, @RequestBody PncReqBodyRevokeCert request) {

        // 요청 URL 가져오기
        String requestUrl = httpRequest.getRequestURL().toString();

        String trackId = logService.controllerLogStart(requestUrl, request);

        ServiceResult<String> serviceResult = certificateService.revokeCertificate(request, trackId);

        // 응답은 서비스와 무관하게 즉시 반환한다 (202 : 수신함)
        PncApiResponse response = new PncApiResponse();
        buildAsyncBaseResponse(serviceResult, response);

        logService.controllerLogEnd(trackId, serviceResult.getSuccess(), response.getCode());

        return response;
    }

    @PostMapping("/cert/ocsp-response-msg")
    @Operation(summary = "1-3. OCSP 메시지 수신", description = """
            EVSE(CSMS) -> **MSP -> eMSP** -> ChargLink -> eMSP -> MSP -> EVSE(CSMS) <br><br>
            ChargLink : /cert/ocsp-response-msg 으로 OCSP 응답 메시지 요청 <br><br>
            응답으로 수신한 ocspResMessage를 MSP로 다시 보내준다.
            """)
    public PncApiResponseObject getOcspResponseMessage(HttpServletRequest httpRequest, @RequestBody PncReqBodyOCSPMessage request) {

        // 요청 URL 가져오기
        String requestUrl = httpRequest.getRequestURL().toString();

        String trackId = logService.controllerLogStart(requestUrl, request);

        ServiceResult<OcspResponse> serviceResult = certificateService.ocspCertCheck(request, trackId);

        PncApiResponseObject response = new PncApiResponseObject();
        if (serviceResult.isFail()) {
            response.setCode(Integer.toString(serviceResult.getErrorCode()));
            response.setMessage(serviceResult.getErrorMessage());
            response.setResult(PncResponseResult.FAIL);
        } else {
            response.setCode("200");
            response.setMessage("OK");

            response.setResult(PncResponseResult.SUCCESS);

            response.setData(Optional.ofNullable(serviceResult.get()));
        }


       logService.controllerLogEnd(trackId, serviceResult.getSuccess(), response.getCode(), response.getData().orElse(null));
       return response;
    }

    @PostMapping("/cert/ocsp-verification")
    @Operation(summary = "1-4. OCSP 유효성 검증", description = """
            EVSE(CSMS) -> **MSP -> eMSP** -> ChargLink -> eMSP -> MSP -> EVSE(CSMS) <br><br>
            ChargLink : /cert/ocsp-response-msg 으로 OCSP 검증 요청 <br><br>
            응답으로 결과를 반환한다.
            """)
    public PncApiResponseObject verifyOcsp(HttpServletRequest httpRequest, @RequestBody PncReqBodyVerifyOcsp request) {

        // 요청 URL 가져오기
        String requestUrl = httpRequest.getRequestURL().toString();
        String trackId = logService.controllerLogStart(requestUrl, request);

        // 차지링크로 토스할 인증서. 만약 pem으로 들어오면 der로 변경 필요. (CertificateConversionService.convertToBase64DER 이용)
        String cert = request.getCert();
        // 차지링크로 토스하는 boolean값. 현재 차지링크에서 미구현된 기능이라 그냥 false로 고정되게 넣거나, 들어온 값 전달하면 됨.
        Boolean nonce = request.getNonce();
        // 요청바디로는 들어오지만, 이걸로 뭘 하거나 차지링크로 보내줄 필요는 없음. (충전기 고유 식별자)
        Long ecKey = request.getEcKey();

        // 들어온 정보 중 cert, nonce만 KpipReqBodyVerifyOcsp 객체로 담아
        // service.http.KpipApiService.verifyOcsp 로 전달하면 됨

        // 응답은 data 내부에 status 항목으로 전달 (차지링크에서 돌아오는 status 그대로 보내주면 됨)
        PncApiResponseObject response = new PncApiResponseObject();

        return response;
    }

    @PostMapping("/contract/issue")
    @Operation(summary = "2-1. PNC 계약", description = """
            **OEM(APP) -> eMSP** -> ChargLink -> eMSP -> kafka <br><br>
            ChargLink : /contract/issue 로 계약 인증서 발급 요청 전송 <br><br>
            kafka : [MSG-EMSP-PNC-CONTRACT] 로 인증서 계약 인증서 정보 전송
            Result가 SUCCESS인 경우, 폴링시 사용할 고유id를 Message로 리턴.
            """)
    public PncApiResponse issueContract(HttpServletRequest httpRequest, @RequestBody PncReqBodyIssueContract request) {
        // 요청 URL 가져오기
        String requestUrl = httpRequest.getRequestURL().toString();
        String trackId = logService.controllerLogStart(requestUrl, request);

        ServiceResult<String> serviceResult = certificateService.issueContract(request, trackId);

        // 응답은 서비스와 무관하게 즉시 반환한다 (202 : 수신함)
        PncApiResponse response = new PncApiResponse();
        buildAsyncBaseResponse(serviceResult, response);

        logService.controllerLogEnd(trackId, serviceResult.getSuccess(), response.getCode());

        return response;
    }

    @PostMapping("/contract/issue-check")
    @Operation(summary = "2-1A. PNC 계약 요청결과 확인", description = """
            /contract/issue 요청에 대한 정보를 조회.<br><br>
            리턴의 result값은 조회과정에서의 상태를 나타내며, 진행중인 절차 완료의 정보는 data 내부를 확인.<br><br>
            래퍼클래스의 result가 fail인 경우 인증서의 상태와 상관없이 조회과정에서 생긴 에러이며, 값을 조회하는 데 성공한 경우에는 데이터 내의 Status의 값으로 확인할 수 있음. 
            """)
    public PncApiResponseObject issueCheck(HttpServletRequest httpRequest, @RequestBody CheckRequest request) {
        // 요청 URL 가져오기
        String requestUrl = httpRequest.getRequestURL().toString();
        String trackId = logService.controllerLogStart(requestUrl, request);
        ServiceResult<ContractStatus> serviceResult = certificateService.getContractStatus(request.getId(), trackId);
        PncApiResponseObject response = new PncApiResponseObject();
        if (serviceResult.isFail()) {
            response.setCode(Integer.toString(serviceResult.getErrorCode()));
            response.setMessage(serviceResult.getErrorMessage());
            response.setResult(PncResponseResult.FAIL);
        } else {
            response.setCode("200");
            response.setMessage("OK");

            response.setResult(PncResponseResult.SUCCESS);
            response.setData(Optional.ofNullable(serviceResult.get()));
        }


        logService.controllerLogEnd(trackId, serviceResult.getSuccess(), response.getCode(), response.getData().orElse(null));
        return response;
    }

    @PostMapping("/contract/revoke")
    @Operation(summary = "2-2. PNC 계약 해지", description = """
            **OEM(APP) -> eMSP** -> ChargLink -> eMSP -> kafka <br><br>
            ChargeLink : /contract/revocation 으로 계약 인증서 폐기 요청 전송 <br><br>
            kafka : [MSG-EMSP-PNC-CONTRACT] 로 변경된 계약 정보 전송
            Result가 SUCCESS인 경우, 폴링시 사용할 고유id를 Message로 리턴.
            """)
    public PncApiResponse revokeContract(HttpServletRequest httpRequest,
            @RequestBody PncReqBodyRevokeContractCert request) {
        // 요청 URL 가져오기
        String requestUrl = httpRequest.getRequestURL().toString();

        String trackId = logService.controllerLogStart(requestUrl, request);

        ServiceResult<String> serviceResult = certificateService.revokeContract(request, trackId);

        // 응답은 서비스와 무관하게 즉시 반환한다 (202 : 수신함)
        PncApiResponse response = new PncApiResponse();
        buildAsyncBaseResponse(serviceResult, response);

        logService.controllerLogEnd(trackId, serviceResult.getSuccess(), response.getCode());

        return response;
    }

    // TODO 조회하는거 하면서, Status 및 에러메세지도 재정립
    @PostMapping("/contract/revoke-check")
    @Operation(summary = "2-2A. PNC 해지 요청결과 확인", description = """
            /contract/issue 요청에 대한 정보를 조회.<br><br>
            리턴의 result값은 조회과정에서의 상태를 나타내며, 진행중인 절차 완료의 정보는 data 내부를 확인.<br><br>
            /contract/issue-check와 동일하게 동작하며 Status가 2인지 여부로 확인할 수 있음. 
            """)
    public PncApiResponseObject revokeCheck(HttpServletRequest httpRequest, @RequestBody CheckRequest request) {
        // 요청 URL 가져오기
        String requestUrl = httpRequest.getRequestURL().toString();
        String trackId = logService.controllerLogStart(requestUrl, request);

         ServiceResult<ContractStatus> serviceResult = certificateService.getContractStatus(request.getId(), trackId);
         PncApiResponseObject response = new PncApiResponseObject();
         if (serviceResult.isFail()) {
             response.setCode(Integer.toString(serviceResult.getErrorCode()));
             response.setMessage(serviceResult.getErrorMessage());
             response.setResult(PncResponseResult.FAIL);
         } else {
             response.setCode("200");
             response.setMessage("OK");
 
             response.setResult(PncResponseResult.SUCCESS);
             response.setData(Optional.ofNullable(serviceResult.get()));
         }
 

        logService.controllerLogEnd(trackId, serviceResult.getSuccess(), response.getCode(), response.getData().orElse(null));
        return response;
    }

    @PostMapping("/contract/info")
    @Operation(summary = "2-3. PNC 가입 정보 확인", description = """
            **OEM(APP) -> eMSP** -> ChargLink -> eMSP -> OEM(APP) <br><br>
            ChargeLink : /pnc-auth/authorize-account 로 account 유효성을 검증한다.
            """)
    public PncApiResponseObject getContractInfo(HttpServletRequest httpRequest,
            @RequestBody PncReqBodyContractInfo request) {
        // 요청 URL 가져오기
        String requestUrl = httpRequest.getRequestURL().toString();

        String trackId = logService.controllerLogStart(requestUrl, request);

        PncApiResponseObject response = new PncApiResponseObject();

        ServiceResult<ContractInfo> result = certificateService.getContractInfo(request, trackId);

        if (result.getSuccess()) {
            response.setResult(PncResponseResult.SUCCESS);
            response.setCode("200");
            response.setMessage("Success");
            ContractInfo data = result.getData().orElse(null); // Optional에서 값을 추출
            response.setData(Optional.ofNullable(data));
        } else {
            response.setResult(PncResponseResult.FAIL);
            response.setCode(Integer.toString(result.getErrorCode()));
            response.setMessage(result.getErrorMessage());
        }
        logService.controllerLogEnd(trackId, result.getSuccess(), response.getCode(), response.getData().orElse(null));

        return response;
    }

    @PostMapping("/authorize")
    @Operation(summary = "3. PNC 충전 인증", description = """
            EVSE -> **MSP -> eMSP** -> ChargLink -> eMSP -> MSP <br><br>
            ChargeLink : /pnc-auth/authorize-account 로 account 유효성을 검증한다.
            """)
    public PncApiResponse pncAuthorize(HttpServletRequest httpRequest, @RequestBody PncReqBodyAuthorize request) {
        // 요청 URL 가져오기
        String requestUrl = httpRequest.getRequestURL().toString();

        String trackId = logService.controllerLogStart(requestUrl, request);

        PncApiResponse response = new PncApiResponse();

        ServiceResult<String> result = certificateService.pncAuthorize(request, trackId);

        if (result.getSuccess()) {
            response.setResult(PncResponseResult.SUCCESS);
            response.setCode("200");
            response.setMessage("Success");
        } else {
            response.setResult(PncResponseResult.FAIL);
            response.setCode(Integer.toString(result.getErrorCode()));
            response.setMessage(result.getErrorMessage());
        }
        logService.controllerLogEnd(trackId, result.getSuccess(), response.getCode());

        return response;
    }

    private boolean buildAsyncBaseResponse(ServiceResult<String> serviceResult, PncApiResponse apiResponse) {
        if (apiResponse == null)
            apiResponse = new PncApiResponse();
        if (serviceResult.getSuccess()) {
            apiResponse.setResult(PncResponseResult.SUCCESS);
            apiResponse.setCode("202");
            apiResponse.setMessage(serviceResult.get());
        } else {
            apiResponse.setResult(PncResponseResult.FAIL);
            apiResponse.setCode(Integer.toString(serviceResult.getErrorCode()));
            apiResponse.setMessage(serviceResult.getErrorMessage());
        }
        return serviceResult.getSuccess();
    }
}
