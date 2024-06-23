package com.chargev.emsp.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.model.dto.pnc.Authorities;
import com.chargev.emsp.model.dto.pnc.CertStatus;
import com.chargev.emsp.model.dto.pnc.CertificateHashData;
import com.chargev.emsp.model.dto.pnc.EvseCertificate;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyEmaid;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyIssueContractCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyRevokeCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyVerifyContractCert;
import com.chargev.emsp.model.dto.pnc.PncContract;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractSuspension;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueContract;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeCert;
import com.chargev.emsp.model.dto.response.KpipApiResponse;
import com.chargev.emsp.service.cryptography.CertificateService;
import com.chargev.emsp.service.http.KpipApiService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("{version}/pnc")
@Validated
@RequiredArgsConstructor
public class PncController {
    // 의존성 주입으로 코드 변경 처리함 
    private final CertificateService certificateService;
    private final KpipApiService kpipApiService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

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
        // OEM 프로비저닝 변경으로 생성된 기존 계약을 삭제(만료)처리해야 하는 상황을 KEPCO가 알려줄 때 호출함.
        // 들어온 body의 pcid에 해당하는 계약을 삭제(만료) 시키고 그에 따른 응답을 반환한다.

        // 1. 요청바디의 값 확인
        // 1-1. 프로비저닝 변경 사유 ('Update' or 'Delete')
        String reqType = (String) request.getReqType();
        // 1-2. 삭제할 계약 PCID
        String pcid = (String) request.getPcid();

        // 2. 해당 pcid와 매칭되는 계약건 찾아서 TERMINATION 상태로 변경 처리

        // 3. 변경된 계약 정보를 kafka로 전송
        PncContract kafkaObject = new PncContract();

        // 아래는 KPIP로 보내줘야하는 응답.
        KpipApiResponse response = new KpipApiResponse();
        response.setResultCode("200");
        response.setResultMsg("Success");

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
    public KpipApiResponse issueCert(@RequestBody PncReqBodyIssueCert request) {

        // 1. 요청바디의 값 확인
        // 1-1. csr
        String csr = (String) request.getCertificateSigningRequest();
        // 1-2. 충전기 고유 식별자: KEPCO로 인증서 서명 요청 보낼 때에는 사용되지 않음. kafka쪽으로 돌려줄 때만 사용.
        Long ecKey = (Long) request.getEcKey();
        // 1-3. 요청 주체: "CSMS" / "EVSE"로 들어오고, KEPCO쪽도 같은 형식으로 받으니 그대로 보내주면 됨.
        String issueType = (String) request.getIssueType();
        // 1-4. 요청 종류: "NEW" / "UPDATE"로 들어오고, KEPCO쪽에는 "N" / "R" 로 변경해서 보내줘야 함.
        String certType = (String) request.getCertType();
        // 1-5. 발급 주체: "KEPCO" / "HUBJECT" KEPCO로 인증서 서명 요청 보낼 때에는 사용되지 않음. kafka쪽으로 돌려줄 때만 사용.
        String authorities = (String) request.getAuthorities();

        // 2. KPIP에게 인증서 요청
        KpipReqBodyIssueCert req = new KpipReqBodyIssueCert();
        
        // csr 바로 토스하지 않고 제대로 된 csr인지 검증 필요할 것으로 보임
        // 주요 검증 내용: ECC 서명 알고리즘 사용하였는지, CN 제대로 들어있는지
        // issueType은 certType으로 바꿔서, certType은 flag로 바꿔서 토스
        req.setCertType(issueType);
        req.setCsr(csr);
        req.setFlag(certType.equals("NEW") ? "N" : "R");

        Map<String, Object> result = kpipApiService.issueCert(req);

        String resultCode = (String) result.get("resultCode");
        String resultMsg = (String) result.get("resultMsg");
        String resCertType;
        String leafCert;
        String subCa2;
        // 돌아온 resultCode에 따라 분기
        if(resultCode.equals("OK")) {
            // 발급 성공
            // CSMS/EVSE 준거 그대로 받는 거라서 의미는 없을듯
            resCertType = (String) result.get("certType");
            // Base 64 encoded DER format of EVSE/CSMS 인증서
            leafCert = (String) result.get("leafCert");
            // Base 64 encoded DER format of SubCa2 Cert 
            subCa2 = (String) result.get("subCa2");

            // *** 24-06-18 변경요청사항
            // 요청 자체는 비동기로 처리하여 응답은 실제 인증서 발급과 무관하게 202(수신함)만 보내주는 것으로 변경
            // KEPCO로 요청 보내서 받은 결과(실제 인증서 발급 결과)는 kafka topic MSG-EMSP-EVSE-CERTIFICATE 으로 보내준다.

            EvseCertificate kafkaObject = new EvseCertificate();

            // 요청 시 받은 데이터 그대로 다시 돌려줘야 하는 것들
            kafkaObject.setEcKey(ecKey);
            kafkaObject.setIssueType(issueType);
            kafkaObject.setCertType(certType);
            kafkaObject.setAuthorities(authorities);

            // 받은 Base64 DER Encoded 인증서를 PEM으로 변환하여 넣기
            // TODO 각 스텝에서 실패가 나올 경우, 적절한 오류 처리 필요
            String pemSubCa2 = certificateService.convertToPEM(subCa2);
            String pemLeafCert = certificateService.convertToPEM(leafCert);
            kafkaObject.setCertificateChain(pemSubCa2);
            kafkaObject.setCertificate(pemLeafCert);
            ZonedDateTime zonedExpiredDate = ZonedDateTime.ofInstant(certificateService.getExpiredDate(leafCert).toInstant(), ZoneId.of("UTC"));
            kafkaObject.setExpiredDate(zonedExpiredDate);

            // 이부분이 로직이 아직 없음. 인증서 상태를 NORMAL, EXPIRED, TERMINATION로 전달해야 하는데,
            // KEPCO에서 바로 내려오지는 않아서 발급된 인증서를 다시 한 번 검증해서 보내야할지 (불필요해보여서, 문의해놓음)
            kafkaObject.setCertificateStatus(CertStatus.NORMAL);

            // 이건 그냥 OK -> SUCCESS, FAIL -> FAIL로 받은대로 내려주면 됨.
            kafkaObject.setResult(resultCode.equals("OK") ? "SUCCESS" : "FAIL");

            // 발급 실패 시 실패 메시지 전달하는 부분인데, 그냥 성공여부 상관없이 KEPCO가 보내준 resultMsg 그대로 전달하면 될 것 같음
            kafkaObject.setMessage(resultMsg);

            // 여기에서 이제 만든 kafkaObject를 kafka로 전송
            // kafkaTemplate.send("MSG-EMSP-EVSE-CERTIFICATE", kafkaObject);
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("MSG-EMSP-EVSE-CERTIFICATE", kafkaObject);

            // send 결과. 우선 System.out으로 찍고있음. 추후 전역로거로 관리하거나 별도 로깅 시스템 마련 필요해보임.
            future.whenComplete((resultSend, ex) -> {
                if (ex == null) {
                    System.out.println("Sent message=[" + kafkaObject + 
                        "] with offset=[" + resultSend.getRecordMetadata().offset() + "]");
                } else {
                    System.out.println("Unable to send message=[" 
                        + kafkaObject + "] due to : " + ex.getMessage());
                }
            });
        } else {
            // 발급 실패
        }

        // 아래는 기존에 동기식으로 response를 보내던 부분이라, 변경되어야 함

        // 3. 응답에 따른 후속 처리
        // 3-1. resultMsg Success인지 확인 (KPIP쪽에서 인증서 서명 처리 잘 되었는지)
        // 3-2. 서버 내부의 DB 등에 계약 관련 데이터 업데이트 처리
        // 3-3. KPIP에서 내려준 인증서(DER) 파일을 PEM으로 변환하여 내려보내야함
        KpipApiResponse response = new KpipApiResponse();
        response.setResultCode("200");
        response.setResultMsg(resultMsg);

        return response;
    }

    @PostMapping("/cert/revoke")
    @Operation(
        summary = "1-2. EVSE 인증서 폐기",
        description = """
                      EVSE(CSMS) -> **MSP -> eMSP** -> ChargLink -> eMSP -> kafka <br><br>
                      ChargLink : /cert/revocation 으로 인증서 발급 요청 전송 <br><br>
                      kafka : [MSG-EMSP-EVSE-CERTIFICATE] 로 인증서 폐기 상태 업데이트
                      """
    )
    public KpipApiResponse revokeCert(@RequestBody PncReqBodyRevokeCert request) {

        // 1. 요청바디의 값 확인
        // 1-1. CertificateHashData
        CertificateHashData certHash = request.getCertificateHashData();
        // 1-2. 충전기 고유 식별자
        Long ecKey = (Long) request.getEcKey();
        // 1-3. 요청 종류 (여기에는 항상 "DELETE"만 들어올 예정이고 KEPCO에 다시 보내줄 필요는 없음. kafka에 보내줄 때 필요함)
        String issueType = (String) request.getIssueType();

        // 2. 실제 revoke요청을 KEPCO로 보낸다.
        // KEPCO에 보낼때에는 "인증서 CN"을 보내야 하는데, 받은 것은 "인증서 해시"와 "충전기 고유 식별자"임
        // 해시값, 혹은 충전기 고유 식별자 값 둘 중 하나 (혹은 둘 모두)로 매칭되는 인증서를 찾고 해당 인증서의 CN을 추출하는 작업 필요
        String certCn = "";

        KpipReqBodyRevokeCert req = new KpipReqBodyRevokeCert();
        req.setCertCn(certCn);

        Map<String, Object> result = kpipApiService.revokeCert(req);

        String resultCode = (String) result.get("resultCode");
        String resultMsg = (String) result.get("resultMsg");

        // 돌아온 resultCode에 따라 분기
        if(resultCode.equals("OK")) {
            // 인증서 폐기 성공
            // 우리쪽 서버에도 삭제 성공에 따른 로직 처리 (status revoked로)
            // 우리쪽 로직까지 끝난 후 kafka로 변경된 내용 보내줘야 함.

            EvseCertificate kafkaObject = new EvseCertificate();

            // 요청 시 받은 데이터 그대로 다시 돌려줘야 하는 것들
            kafkaObject.setEcKey(ecKey);
            kafkaObject.setIssueType(issueType);
            kafkaObject.setCertType("EVSE");
            kafkaObject.setAuthorities("KEPCO");
    
            // 인증서 폐기 상황이므로 certificateChain, certificate, expiredDate는 넣지 않는다.
            kafkaObject.setCertificateStatus(CertStatus.DELETED);
    
            // 이건 그냥 OK -> SUCCESS, FAIL -> FAIL로 받은대로 내려주면 됨.
            kafkaObject.setResult("SUCCESS");
    
            // 발급 실패 시 실패 메시지 전달하는 부분인데, 그냥 성공여부 상관없이 KEPCO가 보내준 resultMsg 그대로 전달하면 될 것 같음
            kafkaObject.setMessage(resultMsg);
    
            // 여기에서 이제 만든 kafkaObject를 kafka로 전송
            // (아래 부분은 실제 kafka로 보내는 부분인데, 해당 코드 사용하기 위해서는
            // 의존성 추가, Bean(KafkaTemplate) 구성 등 작업이 선행되어야하므로 현재는 작동하지 않는 코드임.
            // kafkaTemplate.send("MSG-EMSP-EVSE-CERTIFICATE", kafkaObject);

        } else {
            // 인증서 폐기 실패에 따른 프로세스 진행
        }

        // 아래 부분은 당초 동기식으로 설계한 api의 응답 부분인데,
        // 위쪽에서 인증서 폐기 결과를 kafka로 전송하는 것으로 (비동기 식으로) 프로세스가 변경되어
        // 변경 사항에 맞춰 메서드 구성이 바뀌어야 함.
        KpipApiResponse response = new KpipApiResponse();
        response.setResultCode("200");
        response.setResultMsg("Success");

        return response;
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
    public KpipApiResponse issueContract(@RequestBody PncReqBodyIssueContract request) {

        // 1. 요청바디의 값 확인
        // 1-1. pcid (vin 차대번호)
        String pcid = (String) request.getPcid();
        // 1-2. oemId ("BMW")
        String oemId = (String) request.getOemId();
        // 1-3. memberKey
        Long memberKey = (Long) request.getMemberKey();
        // 1-4. memberGroupId
        String memberGroupId = (String) request.getMemberGroupId();
        // 1-5. memberGroupSeq
        Long memberGroupSeq = (Long) request.getMemberGroupSeq();

        // 2. 계약번호(emaid) 생성
        // 원래 3가지 코드 합친 후 뒤에 7자리 난수 붙여야 하는데, 진짜 "랜덤생성" 할지 특정 로직을 부여할지 아직 모르는 상황이라
        // 우선은 난수 생성 기능을 따로 구현하지 않고 숫자 7자리 넣어놓았음 (실제 생성 시 기생성된 ID와 중복을 피해 생성해야 함)
        String emaId = "KR" + "CEV" + "CA" + "0123456";

        // 3. KPIP에게 계약 인증서 생성 요청
        KpipReqBodyIssueContractCert req = new KpipReqBodyIssueContractCert();

        // "New" "Update" "Add" 세가지 선택 가능한데, 신규에 대해서만 사용함
        req.setReqType("New");
        // BMW에서 들어온 pcid 그대로 사용하면 될 것 같음 (차량 vin 번호)
        req.setPcid(pcid);
        // 계약번호. 위에서 생성한 계약번호 사용
        req.setEmaid(emaId);
        // oemID는 "BMW"로 고정하면 되지만 어차피 BMW에서 요청시에도 들어오기 때문에 들어온 값을 그대로 전달하기로 함
        req.setOemid(oemId);
        // 유효기간인데 기본값인 24로 고정해놓는 것으로 작성함
        req.setExpPolicy("24");

        Map<String, Object> result = kpipApiService.issueContractCert(req);

        String resultCode = (String) result.get("resultCode");
        String resultMsg = (String) result.get("resultMsg");
        String contCert;
        // 돌아온 resultCode에 따라 분기
        if(resultCode.equals("OK")) {
            // 발급 성공
            contCert = (String) result.get("contCert");

            // KEPCO로부터 받은 contCert를 DB 업데이트
            // 만약, kafka로 주고받는 PncContract class와 db entity를 통일하여 관리한다면
            // 아래쪽에 kafkaObject 값 생성 이후에 DB 업데이트를 진행하는 것으로 순서를 변경해야 함

            
            // kafka [MSG-EMSP-PNC-CONTRACT]로 전달

            PncContract kafkaObject = new PncContract();

            // 여기서부터는 그냥 있는 값들
            kafkaObject.setEmaId(emaId);
            kafkaObject.setMemberKey(memberKey);
            kafkaObject.setMemberGroupId(memberGroupId);
            kafkaObject.setMemberGroupSeq(memberGroupSeq);
            kafkaObject.setOemCode(oemId);
            kafkaObject.setVin(pcid);

            // 여기서부터는 만들어야 하는 값
            // TODO 각 스텝별 오류 처리 
            // (1) 받은 Base64 DER Encoded 인증서를 PEM으로 변환
            String pemCert = certificateService.convertToPEM(contCert);
            kafkaObject.setCertificate(pemCert);
            // (2) 받은 contCert로부터 만료날짜 추출
            Date expiredDate = certificateService.getExpiredDate(contCert);
            ZonedDateTime zonedExpiredDate = ZonedDateTime.ofInstant(expiredDate.toInstant(), ZoneId.of("UTC"));
            kafkaObject.setExpiredDate(zonedExpiredDate);
            // (3) 받은 contCert로부터 발급날짜 추출
            Date requestDate = certificateService.getExpiredDate(contCert);
            ZonedDateTime zonedRequestdDate = ZonedDateTime.ofInstant(requestDate.toInstant(), ZoneId.of("UTC"));
            kafkaObject.setRequestDate(zonedRequestdDate);
            // 이건 추가 논의 필요해 보이는데 우선 기본값으로 세팅
            kafkaObject.setAuthorities(Authorities.KEPCO);
            kafkaObject.setStatus(CertStatus.NORMAL);
    
            // 여기에서 이제 만든 kafkaObject를 kafka로 전송
            // (아래 부분은 실제 kafka로 보내는 부분인데, 해당 코드 사용하기 위해서는
            // 의존성 추가, Bean(KafkaTemplate) 구성 등 작업이 선행되어야하므로 현재는 작동하지 않는 코드임.
            // kafkaTemplate.send("MSG-EMSP-PNC-CONTRACT", kafkaObject);

        } else {
            // 발급 실패
        }

        // 아래는 기존 동기 방식일 때를 위한 응답이므로 수정 필요
        KpipApiResponse response = new KpipApiResponse();
        response.setResultCode("200");
        response.setResultMsg(resultMsg);

        return response;
    }

    @PostMapping("/contract/revoke")
    @Operation(summary = "2-2. PNC 계약 해지", description = "BMW APP에서 PnC 계약을 해지한다.")
    public KpipApiResponse revokeContract(@RequestBody KpipReqBodyEmaid request) {

        String resultMsg = kpipApiService.revokeContractCert(request);

        // 1. resultMsg Success인지 확인 (KPIP쪽에서 계약인증서 삭제 잘 수행되었는지)
        // 2. 서버 내부의 DB 등에 계약 관련 데이터 업데이트 처리
        // 3. 해지 결과 CPO에게 kafka로 전달
        KpipApiResponse response = new KpipApiResponse();
        response.setResultCode("200");
        response.setResultMsg(resultMsg);

        return response;
    }

    @PostMapping("/authorize")
    @Operation(summary = "2-3. PNC 충전 인증", description = "CPO로부터 PnC 인증요청을 수신한다.")
    public KpipApiResponse pncAuthorize(@RequestBody KpipReqBodyEmaid request) {

        // 1. PEM으로 들어온 인증서, 혹은 ecKey를 가지고 그에 매칭되는 emaid를 찾는다.
        // PnC account 체크
        KpipReqBodyEmaid req1 = new KpipReqBodyEmaid();
        req1.setEmaid("abc");
        String resultMsg1 = kpipApiService.authorizePnc(req1);

        // 2. PEM으로 들어온 인증서를 DER로 인코딩한다.
        // 계약인증서 유효성 확인 (들어온 PEM을 DER로 변경해서 contCert만 보내주면 됨)
        // 이건 필요한지 모르겠음 (위에것만 체크해도 되는건지)
        KpipReqBodyVerifyContractCert req2 = new KpipReqBodyVerifyContractCert();
        req2.setContCert("contCert");
        String resultMsg2 = kpipApiService.verifyContractCert(req2);

        // 결과로 emaid, bid 보내줘야 한다는데 bid 뭔지 확인 필요
        KpipApiResponse response = new KpipApiResponse();
        response.setResultCode("200");
        response.setResultMsg("Success");

        return response;
    }
}
