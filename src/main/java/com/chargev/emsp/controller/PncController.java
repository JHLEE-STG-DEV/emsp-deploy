package com.chargev.emsp.controller;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.chargev.emsp.model.dto.pnc.ContractInfo;
import com.chargev.emsp.model.dto.pnc.PncReqBodyAuthorize;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractInfo;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractSuspension;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueContract;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeContractCert;
import com.chargev.emsp.model.dto.response.KpipApiResponse;
import com.chargev.emsp.model.dto.response.PncApiResponse;
import com.chargev.emsp.model.dto.response.PncApiResponseObject;
import com.chargev.emsp.model.dto.response.PncResponseResult;
import com.chargev.emsp.service.CertificateService;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.log.CheckpointReference;
import com.chargev.emsp.service.log.PncLogService;
import com.chargev.logger.LogHelper;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@RestController
@Slf4j
//@RequestMapping("{version}/pnc")
@Validated
@RequiredArgsConstructor
public class PncController {
    private final CertificateService certificateService;
    private final PncLogService logService;
    private final LogHelper logHelper;
    

    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");
    // KEPCO가 사용하는 API
    @PostMapping("/provisioning/suspension")
    @Operation(
        summary = "0. OEM 프로비저닝 변동으로 인한 계약 삭제",
        description = """
                      OEM -> **ChargeLink -> eMSP** -> kafka <br><br>
                      kafka : [MSG-EMSP-PNC-CONTRACT] 로 변경된 계약 정보 전송
                      """
    )
    public KpipApiResponse suspension(@RequestBody PncReqBodyContractSuspension request) {
        String endpoint= "/pnc/provisioning/suspension";
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerEnterLog(endpoint));
        }

        String trackId = logService.pncLogStart("/pnc/provisioning/suspension", request);

        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerTrackLog(endpoint, trackId));
        }
        // OEM 프로비저닝 변경으로 생성된 기존 계약을 삭제(만료)처리해야 하는 상황을 KEPCO가 알려줄 때 호출함.
        // 들어온 body의 pcid에 해당하는 계약을 삭제(만료) 시키고 그에 따른 응답을 반환한다.

        // 해당 api만 다른 response 형식을 사용 (KPIP와 통일)
        KpipApiResponse response = new KpipApiResponse();


        ServiceResult<String> result = certificateService.suspension(request, trackId);

        if(result.getSuccess()) {
            response.setResultCode("OK");
            response.setResultMsg("Success");
            if (apiLogger.isInfoEnabled()) {
                apiLogger.info(logHelper.controllerResponseLog(endpoint, trackId, "OK"));
            }
            logService.pncLogFinish(trackId, "OK");
        } else {
            response.setResultCode("FAIL");
            response.setResultMsg(result.getErrorMessage());
            if (apiLogger.isWarnEnabled()) {
                apiLogger.warn(logHelper.controllerResponseLog(endpoint, trackId, "FAIL"));
            }
            logService.pncLogFail(trackId, "FAIL", result.getErrorMessage());
        }

        return response;
    }

    @PostMapping("/cert/issuance")
    @Operation(
        summary = "1-1. EVSE 및 CSMS 인증서 발급/갱신",
        description = """
                      EVSE(CSMS) -> **MSP -> eMSP** -> ChargLink -> eMSP -> kafka <br><br>
                      ChargLink : /cert/issuance 로 인증서 발급 요청 전송 <br><br>
                      kafka : [MSG-EMSP-EVSE-CERTIFICATE] 로 발급된 인증서 정보 전송
                      """
    )
    public PncApiResponse issueCert(@RequestBody PncReqBodyIssueCert request) {
        String endpoint= "/pnc/cert/issuance";
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerEnterLog(endpoint));
        }
        String trackId = logService.pncLogStart("/pnc/cert/issuance", request);
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerTrackLog(endpoint, trackId));
        }

        CompletableFuture<ServiceResult<String>> future = certificateService.issueCertificate(request, trackId);
        
        // 비동기 작업의 결과를 처리
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerBackgroundLog(endpoint, trackId));
        }
        future.thenAccept(result -> {
            if (result.getSuccess()) {
                // 성공 처리 로직
                System.out.println("Certificate issued successfully");
            } else {
                // 실패 처리 로직
                System.err.println("Failed to issue certificate: " + result.getErrorMessage());
            }
            for(CheckpointReference ref : result.getCheckpoints()){      
                logService.pncLogCheckpoint(trackId, ref);
            }
        });

        logService.pncLogFinish(trackId, "202");
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerResponseLog(endpoint, trackId, "202"));
        }
        // 응답은 서비스와 무관하게 즉시 반환한다 (202 : 수신함)
        return createAsyncResponse();
    }

    @PostMapping("/cert/revoke")
    @Operation(
        summary = "1-2. EVSE 인증서 폐기",
        description = """
                      EVSE(CSMS) -> **MSP -> eMSP** -> ChargLink -> eMSP -> kafka <br><br>
                      ChargLink : /cert/revocation 으로 인증서 폐기 요청 전송 <br><br>
                      kafka : [MSG-EMSP-EVSE-CERTIFICATE] 로 인증서 폐기 상태 업데이트
                      """
    )
    public PncApiResponse revokeCert(@RequestBody PncReqBodyRevokeCert request) {
        String endpoint= "/pnc/cert/revoke";
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerEnterLog(endpoint));
        }
        String trackId = logService.pncLogStart("/pnc/cert/revoke", request);
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerTrackLog(endpoint, trackId));
        }


        CompletableFuture<ServiceResult<String>> future = certificateService.revokeCertificate(request, trackId);

        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerBackgroundLog(endpoint, trackId));
        }
        // 비동기 작업의 결과를 처리
        future.thenAccept(result -> {
            if (result.getSuccess()) {
                // 성공 처리 로직
                System.out.println("Certificate revoked successfully");
            } else {
                // 실패 처리 로직
                System.err.println("Failed to revoke certificate: " + result.getErrorMessage());
            }
            for(CheckpointReference ref : result.getCheckpoints()){      
                logService.pncLogCheckpoint(trackId, ref);
            }
        });

        // 응답은 서비스와 무관하게 즉시 반환한다 (202 : 수신함)
        logService.pncLogFinish(trackId, "202");
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerResponseLog(endpoint, trackId, "202"));
        }
        return createAsyncResponse();
    }

    @PostMapping("/contract/issue")
    @Operation(
        summary = "2-1. PNC 계약",
        description = """
                      **OEM(APP) -> eMSP** -> ChargLink -> eMSP -> kafka <br><br>
                      ChargLink : /contract/issue 로 계약 인증서 발급 요청 전송 <br><br>
                      kafka : [MSG-EMSP-PNC-CONTRACT] 로 인증서 계약 인증서 정보 전송
                      """
    )
    public PncApiResponse issueContract(@RequestBody PncReqBodyIssueContract request) {
        String endpoint= "/pnc/contract/issue";
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerEnterLog(endpoint));
        }

        String trackId = logService.pncLogStart("/pnc/contract/issue", request);
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerTrackLog(endpoint, trackId));
        }

        CompletableFuture<ServiceResult<String>> future = certificateService.issueContract(request, trackId);

        // 비동기 작업의 결과를 처리
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerBackgroundLog(endpoint, trackId));
        }
        future.thenAccept(result -> {
            if (result.getSuccess()) {
                // 성공 처리 로직
                System.out.println("Contract certificate issued successfully");
            } else {
                // 실패 처리 로직
                System.err.println("Failed to issue contract certificate: " + result.getErrorMessage());
            }
            for(CheckpointReference ref : result.getCheckpoints()){      
                logService.pncLogCheckpoint(trackId, ref);
            }
        });
        
        // 응답은 서비스와 무관하게 즉시 반환한다 (202 : 수신함)
        logService.pncLogFinish(trackId, "202");
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerResponseLog(endpoint, trackId, "202"));
        }
        return createAsyncResponse();
    }

    @PostMapping("/contract/revoke")
    @Operation(
        summary = "2-2. PNC 계약 해지",
        description = """
                      **OEM(APP) -> eMSP** -> ChargLink -> eMSP -> kafka <br><br>
                      ChargeLink : /contract/revocation 으로 계약 인증서 폐기 요청 전송 <br><br>
                      kafka : [MSG-EMSP-PNC-CONTRACT] 로 변경된 계약 정보 전송
                      """
    )
    public PncApiResponse revokeContract(@RequestBody PncReqBodyRevokeContractCert request) {
        String endpoint= "/pnc/contract/revoke";
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerEnterLog(endpoint));
        }

        String trackId = logService.pncLogStart("/pnc/contract/revoke", request);
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerTrackLog(endpoint, trackId));
        }


        CompletableFuture<ServiceResult<String>> future = certificateService.revokeContract(request, trackId);


        // 비동기 작업의 결과를 처리
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerBackgroundLog(endpoint, trackId));
        }
        future.thenAccept(result -> {
            if (result.getSuccess()) {
                // 성공 처리 로직
                System.out.println("Contract certificate revoked successfully");
            } else {
                // 실패 처리 로직
                System.err.println("Failed to revoke contract certificate: " + result.getErrorMessage());
            }
            for(CheckpointReference ref : result.getCheckpoints()){      
                logService.pncLogCheckpoint(trackId, ref);
            }
        });

        // 응답은 서비스와 무관하게 즉시 반환한다 (202 : 수신함)
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerResponseLog(endpoint, trackId, "202"));
        }
        logService.pncLogFinish(trackId, "202");
        return createAsyncResponse();
    }

    @PostMapping("/contract/info")
    @Operation(
        summary = "2-3. PNC 가입 정보 확인",
        description = """
                      **OEM(APP) -> eMSP** -> ChargLink -> eMSP -> OEM(APP) <br><br>
                      ChargeLink : /pnc-auth/authorize-account 로 account 유효성을 검증한다.
                      """
    )
    public PncApiResponseObject getContractInfo(@RequestBody PncReqBodyContractInfo request) {
        String endpoint= "/pnc/contract/info";
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerEnterLog(endpoint));
        }
        String trackId = logService.pncLogStart("/pnc/contract/info", request);
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerTrackLog(endpoint, trackId));
        }


        PncApiResponseObject response = new PncApiResponseObject();
        ContractInfo contractInfo = new ContractInfo();

        ServiceResult<ContractInfo> result = certificateService.getContractInfo(request, trackId);

        for(CheckpointReference ref : result.getCheckpoints()){      
            logService.pncLogCheckpoint(trackId, ref);
        }
        if(result.getSuccess()) {
            response.setResult(PncResponseResult.SUCCESS);
            response.setCode("200");
            logService.pncLogFinish(trackId, "200");
            response.setMessage("Success");
            ContractInfo data = result.getData().orElse(null); // Optional에서 값을 추출
            response.setData(Optional.ofNullable(data));
            if (apiLogger.isInfoEnabled()) {
                if(data == null){
                    apiLogger.info(logHelper.controllerResponseLog(endpoint, trackId, "200"));
         
                }else{
                    apiLogger.info(logHelper.controllerResponseLog(endpoint, trackId, "200", data));
         
                }
            }
        } else {
            response.setResult(PncResponseResult.FAIL);
            response.setCode("500");
            logService.pncLogFail(trackId, "500", result.getErrorMessage());
            response.setMessage("No matching member information found.");
            if (apiLogger.isWarnEnabled()) {
                apiLogger.warn(logHelper.controllerResponseLog(endpoint, trackId, "500"));
            }
        }

        // contractInfo.setEmaid("KRCEVCA5347850");
        // contractInfo.setContractStartDt("2024-07-06T03:38:46Z");
        // contractInfo.setContractEndDt("2024-07-06T03:38:46Z");

        return response;
    }

    @PostMapping("/authorize")
    @Operation(
        summary = "3. PNC 충전 인증",
        description = """
                      EVSE -> **MSP -> eMSP** -> ChargLink -> eMSP -> MSP <br><br>
                      ChargeLink : /pnc-auth/authorize-account 로 account 유효성을 검증한다.
                      """
    )
    public PncApiResponse pncAuthorize(@RequestBody PncReqBodyAuthorize request) {
        String endpoint= "/pnc/authorize";
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerEnterLog(endpoint));
        }
        String trackId = logService.pncLogStart("/pnc/authorize", request);
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info(logHelper.controllerTrackLog(endpoint, trackId));
        }


        PncApiResponse response = new PncApiResponse();


        ServiceResult<String> result = certificateService.pncAuthorize(request, trackId);

        for(CheckpointReference ref : result.getCheckpoints()){      
            logService.pncLogCheckpoint(trackId, ref);
        }
        if(result.getSuccess()) {
            response.setResult(PncResponseResult.SUCCESS);
            response.setCode("200");
            response.setMessage("Success");
            logService.pncLogFinish(trackId, "200");
            if (apiLogger.isInfoEnabled()) {
                apiLogger.info(logHelper.controllerResponseLog(endpoint, trackId, "200"));
            }
        } else {
            response.setResult(PncResponseResult.FAIL);
            response.setCode("500");
            response.setMessage(result.getErrorMessage());
            logService.pncLogFail(trackId, "500", result.getErrorMessage());
            if (apiLogger.isWarnEnabled()) {
                apiLogger.warn(logHelper.controllerResponseLog(endpoint, trackId, "500"));
            }
        }

        return response;
    }

    private PncApiResponse createAsyncResponse() {
        PncApiResponse response = new PncApiResponse();
        response.setResult(PncResponseResult.SUCCESS);
        response.setCode("202");
        response.setMessage("Request received, processing initiated.");
        return response;
    }
    
}
