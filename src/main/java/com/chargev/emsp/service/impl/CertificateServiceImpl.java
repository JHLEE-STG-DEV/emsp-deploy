package com.chargev.emsp.service.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractInfo;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractSuspension;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueContract;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeCert;
import com.chargev.emsp.service.CertificateService;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.cryptography.CertificateConversionService;
import com.chargev.emsp.service.http.KpipApiService;

@Service
public class CertificateServiceImpl implements CertificateService {

    @Autowired
    private KpipApiService kpipApiService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private CertificateConversionService certificateConversionService;

    @Override
    public ServiceResult<Void> suspension(PncReqBodyContractSuspension request) {
        ServiceResult<Void> serviceResult = new ServiceResult<>();

        try {
            // 1. 요청바디의 값 확인
            // 1-1. 프로비저닝 변경 사유 ('Update' or 'Delete')
            String reqType = (String) request.getReqType();
            // 1-2. 삭제할 계약 PCID
            String pcid = (String) request.getPcid();

            // 2. 해당 pcid와 매칭되는 계약건 찾아서 TERMINATION 상태로 변경 처리
            
            // (이 요청이 들어올 때 계약 인증서는 이미 KEPCO에서 폐기가 된 상태임)
            // (그렇다면 push-whitelist는 우리가 해야하는가?)


            // 3. 변경된 계약 정보를 kafka로 전송
            PncContract kafkaObject = new PncContract();
            kafkaObject.setEmaId("TEST_EMA_ID");
            kafkaObject.setMemberKey(1234L);
            kafkaObject.setMemberGroupId("TEST_GROUP_ID");
            kafkaObject.setOemCode("TEST_OEM_CODE");
            kafkaObject.setVin("TEST_VIN");
            // DER이라면 PEM으로 변경하는 CertificateConversionService 사용 필요
            kafkaObject.setCertificate("TEST_CERT");
            kafkaObject.setAuthorities(Authorities.KEPCO);
            kafkaObject.setStatus(CertStatus.DELETED);
            // 인증서 넣으면 만료기한 추출하는 CertificateConversionService 사용 or DB에 저장해뒀다면 꺼내어서 사용
            kafkaObject.setExpiredDate("");
            // 인증서 넣으면 만료기한 추출하는 CertificateConversionService 사용 or DB에 저장해뒀다면 꺼내어서 사용
            kafkaObject.setRequestDate("");

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("MSG-EMSP-PNC-CONTRACT", kafkaObject);

            future.whenComplete((resultSend, ex) -> {
                if (ex == null) {
                    System.out.println("Sent message=[" + kafkaObject + "] with offset=[" + resultSend.getRecordMetadata().offset() + "]");
                } else {
                    System.out.println("Unable to send message=[" + kafkaObject + "] due to : " + ex.getMessage());
                }
            });

            serviceResult.succeed(null);
        } catch (Exception e) {
            serviceResult.fail(500, e.getMessage());
        }

        return serviceResult;
    }

    @Async
    @Override
    public CompletableFuture<ServiceResult<Void>> issueCertificate(PncReqBodyIssueCert request) {
        ServiceResult<Void> serviceResult = new ServiceResult<>();

        try {
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
            KpipReqBodyIssueCert kpipRequest = new KpipReqBodyIssueCert();
            kpipRequest.setCertType(certType);
            kpipRequest.setCsr(csr);
            kpipRequest.setFlag(issueType.equals("NEW") ? "N" : "R");

            Map<String, Object> result = kpipApiService.issueCert(kpipRequest);

            String resultCode = (String) result.get("resultCode");
            String resultMsg = (String) result.get("resultMsg");
            String resCertType;
            String leafCert;
            String subCa2;

            EvseCertificate kafkaObject = new EvseCertificate();
            kafkaObject.setEcKey(ecKey);
            kafkaObject.setIssueType(issueType);
            kafkaObject.setCertType(certType);
            kafkaObject.setAuthorities(authorities);

            // 돌아온 resultCode에 따라 분기
            if(resultCode.equals("OK")) {
                // 발급 성공
                // CSMS/EVSE 준거 그대로 받는 거라서 의미는 없을듯
                resCertType = (String) result.get("certType");
                // Base 64 encoded DER format of EVSE/CSMS 인증서
                leafCert = (String) result.get("leafCert");
                // Base 64 encoded DER format of SubCa2 Cert 
                subCa2 = (String) result.get("subCa2");

                // 받은 Base64 DER Encoded 인증서를 PEM으로 변환하여 넣기
                // TODO 각 스텝에서 실패가 나올 경우, 적절한 오류 처리 필요
                String pemSubCa2 = certificateConversionService.convertToPEM(subCa2);
                String pemLeafCert = certificateConversionService.convertToPEM(leafCert);
                kafkaObject.setCertificateChain(pemSubCa2);
                kafkaObject.setCertificate(pemLeafCert);

                ZonedDateTime zonedExpiredDate = ZonedDateTime.ofInstant(certificateConversionService.getExpiredDate(leafCert).toInstant(), ZoneId.of("UTC"));
                kafkaObject.setExpiredDate(zonedExpiredDate.toString());
                kafkaObject.setCertificateStatus(CertStatus.NORMAL);
                kafkaObject.setResult("SUCCESS");
            } else {
                kafkaObject.setResult("FAIL");
            }

            kafkaObject.setMessage(resultMsg);

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("MSG-EMSP-EVSE-CERTIFICATE", kafkaObject);

            future.whenComplete((resultSend, ex) -> {
                if (ex == null) {
                    System.out.println("Sent message=[" + kafkaObject + "] with offset=[" + resultSend.getRecordMetadata().offset() + "]");
                } else {
                    System.out.println("Unable to send message=[" + kafkaObject + "] due to : " + ex.getMessage());
                }
            });
            serviceResult.succeed(null);
        } catch (Exception e) {
            serviceResult.fail(500, e.getMessage());
        }

        return CompletableFuture.completedFuture(serviceResult);
    }

    @Async
    @Override
    public CompletableFuture<ServiceResult<Void>> revokeCertificate(PncReqBodyRevokeCert request) {
        ServiceResult<Void> serviceResult = new ServiceResult<>();
        
        try {
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
            serviceResult.succeed(null);
        } catch (Exception e) {
            serviceResult.fail(500, e.getMessage());
        }
        return CompletableFuture.completedFuture(serviceResult);
    }


    @Async
    @Override
    public CompletableFuture<ServiceResult<Void>>  issueContract(PncReqBodyIssueContract request) {
        ServiceResult<Void> serviceResult = new ServiceResult<>();

        try {
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
                String pemCert = certificateConversionService.convertToPEM(contCert);
                kafkaObject.setCertificate(pemCert);
                // (2) 받은 contCert로부터 만료날짜 추출
                // TODO toString() 의 날짜 적격으로 조정 
                Date expiredDate = certificateConversionService.getExpiredDate(contCert);
                ZonedDateTime zonedExpiredDate = ZonedDateTime.ofInstant(expiredDate.toInstant(), ZoneId.of("UTC"));
                kafkaObject.setExpiredDate(zonedExpiredDate.toString());
                // (3) 받은 contCert로부터 발급날짜 추출
                Date requestDate = certificateConversionService.getExpiredDate(contCert);
                ZonedDateTime zonedRequestdDate = ZonedDateTime.ofInstant(requestDate.toInstant(), ZoneId.of("UTC"));
                kafkaObject.setRequestDate(zonedRequestdDate.toString());
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
            serviceResult.succeed(null);
        } catch (Exception e) {
            serviceResult.fail(500, e.getMessage());
        }

        return CompletableFuture.completedFuture(serviceResult);
    }

    @Async
    @Override
    public CompletableFuture<ServiceResult<Void>>  revokeContract(KpipReqBodyEmaid request) {
        ServiceResult<Void> serviceResult = new ServiceResult<>();
        
        try {
            String resultMsg = kpipApiService.revokeContractCert(request);
            serviceResult.succeed(null);
        } catch (Exception e) {
            serviceResult.fail(500, e.getMessage());
        }

        return CompletableFuture.completedFuture(serviceResult);
    }

    @Override
    public ServiceResult<Void> getContractInfo(PncReqBodyContractInfo request) {
        ServiceResult<Void> serviceResult = new ServiceResult<>();

        try {

            String pcid = request.getPcid();
            String oemId = request.getOemId();
            Long memberKey = request.getMemberKey();
    
            // 들어온 pcid(vin번호), memberkey(회원번호)로 매칭되는 계약을 찾는다.
    
            // 유효한 계약인지 확인한다.
    
    
            KpipReqBodyVerifyContractCert req2 = new KpipReqBodyVerifyContractCert();
            req2.setContCert("contCert");
            String resultMsg2 = kpipApiService.verifyContractCert(req2);
            serviceResult.succeed(null);
        } catch (Exception e) {
            serviceResult.fail(500, e.getMessage());
        }

        return serviceResult;
    }

    @Override
    public ServiceResult<Void> pncAuthorize(KpipReqBodyEmaid request) {
        ServiceResult<Void> serviceResult = new ServiceResult<>();

        try {
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
            serviceResult.succeed(null);
        } catch (Exception e) {
            serviceResult.fail(500, e.getMessage());
        }
        
        return serviceResult;
    }

}
