package com.chargev.emsp.service.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.chargev.emsp.model.dto.pnc.Authorities;
import com.chargev.emsp.model.dto.pnc.CertStatus;
import com.chargev.emsp.model.dto.pnc.CertificationMeta;
import com.chargev.emsp.model.dto.pnc.ContractInfo;
import com.chargev.emsp.model.dto.pnc.ContractMeta;
import com.chargev.emsp.model.dto.pnc.EvseCertificate;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyEmaid;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyIssueContractCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyPushWhitelistItem;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyRevokeCert;
import com.chargev.emsp.model.dto.pnc.KpipResBodyPushWhitelistItem;
import com.chargev.emsp.model.dto.pnc.PncContract;
import com.chargev.emsp.model.dto.pnc.PncReqBodyAuthorize;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractInfo;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractSuspension;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueContract;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyRevokeContractCert;
import com.chargev.emsp.service.CertificateService;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.cert.CertManageService;
import com.chargev.emsp.service.contract.ContractManageService;
import com.chargev.emsp.service.cryptography.CertificateConversionService;
import com.chargev.emsp.service.formatter.DateTimeFormatterService;
import com.chargev.emsp.service.http.KpipApiService;
import com.chargev.emsp.service.kafka.KafkaManageService;
import com.chargev.emsp.service.log.CheckpointKind;
import com.chargev.emsp.service.log.CheckpointReference;
import com.chargev.emsp.service.log.KafkaLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class CertificateServiceImpl implements CertificateService {

    private final DateTimeFormatterService dateTimeFormatterService;
    private final KpipApiService kpipApiService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CertificateConversionService certificateConversionService;
    private final CertManageService certManageService;
    private final ContractManageService contractManageService;
    private final KafkaManageService kafkaManageServcie;

    private final KafkaLogService kafkaLogService;

    @Override
    public ServiceResult<String> suspension(PncReqBodyContractSuspension request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();

        try {
            // 1. 요청바디의 값 확인
            // 1-1. 프로비저닝 변경 사유 ('Update' or 'Delete')
            String reqType = (String) request.getReqType();
            // 1-2. 삭제할 계약 PCID
            String pcid = (String) request.getPcid();

            // 2. 해당 pcid와 매칭되는 계약건 찾아서 TERMINATION 상태로 변경 처리
            ServiceResult<ContractMeta> queryResult = contractManageService.findContractByPcid(pcid);
            if (!queryResult.getSuccess()) {
                // DB에 없음.
            }

            // 3. push-whitelist로 계약인증서 delete되었음을 알림
            // 3-1. 해당 계약건의 emaId 찾아서 kpipApiService.pushWhitelist() 으로 넘긴다
            String emaId = queryResult.get().getEmaId();
            KpipReqBodyPushWhitelistItem item = new KpipReqBodyPushWhitelistItem();
            item.setType("DELETE");
            item.setEmaid(emaId);
            List<KpipReqBodyPushWhitelistItem> reqList = new ArrayList<>();
            reqList.add(item);

            ServiceResult<Map<String, Object>> pushServiceResult = kpipApiService.pushWhitelist(reqList, trackId);

            serviceResult.getCheckpoints().addAll(pushServiceResult.getCheckpoints());
            if (!pushServiceResult.getSuccess()) {
                serviceResult.fail(500, "Kpip returns error : " + pushServiceResult.getErrorCode());
                return serviceResult;
            }

            // 여기오면 삭제된거라고 생각.
            ServiceResult<ContractMeta> undoResult = contractManageService.undoWhitelistedContractg(emaId);
            if (!undoResult.getSuccess()) {
                // DB상으로 Whitelist를 삭제하지 못함.
            }

            Map<String, Object> pushResult = pushServiceResult.get();

            String pushResultCode = (String) pushResult.get("resultCode");
            String pushResultMsg = (String) pushResult.get("resultMsg");

            // 전체 응답 리스트
            List<KpipResBodyPushWhitelistItem> emaidList = new ArrayList<KpipResBodyPushWhitelistItem>();
            if (pushResult.containsKey("emaidList")) {
                emaidList = (List<KpipResBodyPushWhitelistItem>) pushResult.get("emaidList");
            }

            // 구분해서 담을 리스트
            List<KpipResBodyPushWhitelistItem> successList = new ArrayList<>();
            List<KpipResBodyPushWhitelistItem> errorList = new ArrayList<>();

            // 리스트 순회하며 각 건 별로 push에 성공했는지 실패했는지를 수집한다.
            for (KpipResBodyPushWhitelistItem itm : emaidList) {
                if ("Success".equals(itm.getResultMsg())) {
                    successList.add(itm);
                } else {
                    errorList.add(itm);
                }
            }

            // List 중 일부만 처리 성공 => 이 케이스는 없을 것으로 추측함
            // (한꺼번에 여러 건을 push 할 수 있도록 List 형태로 요청/응답 설계되었으나)
            // (실제로 계약 생성, 폐기 시 해당 단 건에 대해서만 이벤트 기반으로 push 요청을 넣게 되므로)
            // (이 경우에는 언제나 보낼 때에도 List 내부에 단일 객체, 돌아올 때에도 단일 객체만 들어있음)
            // (일부 성공 (resultCode가 SOME인 경우)는 push 실패 케이스를 한꺼번에 모아 배치로 보내는 상황에서만 발생할 수 있음)

            // 3-2. 여기에서 실패 처리, 현재 논의된 방향은 whitelist 업데이트에 실패한 건들만 모아놨다가
            // 하루에 한 번 정도 (배치) 요청을 다시 보내는 방향으로 정리되었음.

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
            // 인증서 넣어서 만료날짜 추출하는 CertificateConversionService 사용? or DB에 저장해뒀다면 꺼내어서 사용?
            kafkaObject.setExpiredDate("");
            // 인증서 넣어서 시작날짜 추출하는 CertificateConversionService 사용? or DB에 저장해뒀다면 꺼내어서 사용?
            kafkaObject.setRequestDate("");

            String topic = "MSG-EMSP-PNC-CONTRACT";

            String kafkaLogId = kafkaLogService.kafkaLogStart(topic, kafkaObject, trackId);
            CheckpointReference kafkaRef = new CheckpointReference(kafkaLogId);
            serviceResult.getCheckpoints().add(kafkaRef);
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, kafkaObject);

            future.whenComplete((resultSend, ex) -> {
                if (ex == null) {
                    kafkaLogService.kafkaFinish(kafkaLogId, resultSend.getRecordMetadata().offset());
                    kafkaRef.setCheckpointKind(CheckpointKind.KAFKA_SEND_SUCCESS);
                    System.out.println("Sent message=[" + kafkaObject + "] with offset=["
                            + resultSend.getRecordMetadata().offset() + "]");
                } else {
                    kafkaLogService.kafakaFail(kafkaLogId, "500", ex.getMessage());
                    kafkaRef.setCheckpointKind(CheckpointKind.KAFKA_SEND_FAIL);
                    System.out.println("Unable to send message=[" + kafkaObject + "] due to : " + ex.getMessage());
                }
            });

            serviceResult.succeed("OK");
        } catch (Exception e) {
            serviceResult.fail(500, e.getMessage());
        }

        return serviceResult;
    }

    @Async
    @Override
    public CompletableFuture<ServiceResult<String>> issueCertificate(PncReqBodyIssueCert request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();

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
            // 1-5. 발급 주체: "KEPCO" / "HUBJECT" KEPCO로 인증서 서명 요청 보낼 때에는 사용되지 않음. kafka쪽으로 돌려줄
            // 때만 사용.
            String authorities = (String) request.getAuthorities();

            // 2. KPIP에게 인증서 요청
            KpipReqBodyIssueCert kpipRequest = new KpipReqBodyIssueCert();
            kpipRequest.setCertType(certType);
            kpipRequest.setCsr(csr);
            kpipRequest.setFlag(issueType.equals("NEW") ? "N" : "R");

            ServiceResult<Map<String, Object>> kpipResult = kpipApiService.issueCert(kpipRequest, trackId);

            serviceResult.getCheckpoints().addAll(kpipResult.getCheckpoints());

            if (!kpipResult.getSuccess()) {
                serviceResult.fail(500, "Kpip returns error : " + kpipResult.getErrorCode());
                return CompletableFuture.completedFuture(serviceResult);
            }

            Map<String, Object> result = kpipResult.get();

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
            if (resultCode.equals("OK")) {
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

                ZonedDateTime zonedExpiredDate = ZonedDateTime
                        .ofInstant(certificateConversionService.getExpiredDate(leafCert).toInstant(), ZoneId.of("UTC"));
                String expiredDateString = dateTimeFormatterService.formatToSimpleStyle(zonedExpiredDate);
                kafkaObject.setExpiredDate(expiredDateString);
                kafkaObject.setCertificateStatus(CertStatus.NORMAL);

                kafkaObject.setResult("SUCCESS");

                // DB에 저장
                // SubCert 저장
                ServiceResult<String> subResult = certManageService.saveSubCert(ecKey, subCa2);

                if (!subResult.getSuccess()) {
                    // db에 저장실패했다 어떻게 처리할 것인가?

                }
                // 만약 Update면 사전의 것들을 밀어야한다.
                ServiceResult<String> leafResult = certManageService.createNewCert(ecKey, subResult.get(), pemLeafCert,
                        !issueType.equals("NEW"));

                if (!subResult.getSuccess()) {
                    // db에 저장실패했다 어떻게 처리할 것인가?

                }
            } else {
                kafkaObject.setResult("FAIL");
            }

            kafkaObject.setMessage(resultMsg);

            String topic = "MSG-EMSP-EVSE-CERTIFICATE";
            String kafkaLogId = kafkaLogService.kafkaLogStart(topic, kafkaObject, trackId);
            CheckpointReference kafkaRef = new CheckpointReference(kafkaLogId);
            serviceResult.getCheckpoints().add(kafkaRef);
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, kafkaObject);

            future.whenComplete((resultSend, ex) -> {
                if (ex == null) {
                    kafkaLogService.kafkaFinish(kafkaLogId, resultSend.getRecordMetadata().offset());
                    kafkaRef.setCheckpointKind(CheckpointKind.KAFKA_SEND_SUCCESS);
                    System.out.println("Sent message=[" + kafkaObject + "] with offset=["
                            + resultSend.getRecordMetadata().offset() + "]");
                } else {
                    kafkaLogService.kafakaFail(kafkaLogId, "500", ex.getMessage());
                    kafkaRef.setCheckpointKind(CheckpointKind.KAFKA_SEND_FAIL);
                    System.out.println("Unable to send message=[" + kafkaObject + "] due to : " + ex.getMessage());
                }
            });
            serviceResult.succeed("OK");
        } catch (Exception e) {
            serviceResult.fail(500, e.getMessage());
        }

        return CompletableFuture.completedFuture(serviceResult);
    }

    @Async
    @Override
    public CompletableFuture<ServiceResult<String>> revokeCertificate(PncReqBodyRevokeCert request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();

        try {
            // 1. 요청바디의 값 확인
            // 1-1. CertificateHashData 에서 쌩 
            String pem = request.getCertificate();
            // 1-2. 충전기 고유 식별자
            Long ecKey = (Long) request.getEcKey();
            // 1-3. 요청 종류 (여기에는 항상 "DELETE"만 들어올 예정이고 KEPCO에 다시 보내줄 필요는 없음. kafka에 보내줄 때
            // 필요함)
            String issueType = (String) request.getIssueType();

            // DB : 요청한 것에 대응되는 것이 DB에 있는지 확인
            ServiceResult<CertificationMeta> certResult = certManageService.findCertByPEM(ecKey, pem);
            if (!certResult.getSuccess()) {
                // DB에 없다. 처리를 안하는것으로 한다.
                serviceResult.fail(404, "No Cert Exists in DB");
                // kafka로 처리할거면 여기서 처리
            }

            // 2. 실제 revoke요청을 KEPCO로 보낸다.
            // KEPCO에 보낼때에는 "인증서 CN"을 보내야 하는데, 받은 것은 "인증서 해시"와 "충전기 고유 식별자"임
            // 해시값, 혹은 충전기 고유 식별자 값 둘 중 하나 (혹은 둘 모두)로 매칭되는 인증서를 찾고 해당 인증서의 CN을 추출하는 작업 필요
            String certCn = "";

            KpipReqBodyRevokeCert req = new KpipReqBodyRevokeCert();
            req.setCertCn(certCn);

            ServiceResult<Map<String, Object>> kpipResult = kpipApiService.revokeCert(req, trackId);

            serviceResult.getCheckpoints().addAll(kpipResult.getCheckpoints());

            if (!kpipResult.getSuccess()) {
                serviceResult.fail(500, "Kpip returns error : " + kpipResult.getErrorCode());
                return CompletableFuture.completedFuture(serviceResult);
            }

            Map<String, Object> result = kpipResult.get();

            String resultCode = (String) result.get("resultCode");
            String resultMsg = (String) result.get("resultMsg");

            // 돌아온 resultCode에 따라 분기
            if (resultCode.equals("OK")) {
                // 인증서 폐기 성공
                // 우리쪽 서버에도 삭제 성공에 따른 로직 처리 (status revoked로)
                // 우리쪽 로직까지 끝난 후 kafka로 변경된 내용 보내줘야 함.
                // DB에서 삭제
                ServiceResult<String> revokeResult = certManageService.revokeCertByCertId(certResult.get().getCertId());
                if (!revokeResult.getSuccess()) {
                    // DB에서 revoke실패함. 어떻게 할 것인가?

                }
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
                String topic = "MSG-EMSP-EVSE-CERTIFICATE";
                String kafkaLogId = kafkaLogService.kafkaLogStart(topic, kafkaObject, trackId);
                CheckpointReference kafkaRef = new CheckpointReference(kafkaLogId);
                serviceResult.getCheckpoints().add(kafkaRef);

                CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, kafkaObject);
                future.whenComplete((resultSend, ex) -> {
                    if (ex == null) {
                        kafkaLogService.kafkaFinish(kafkaLogId, resultSend.getRecordMetadata().offset());
                        kafkaRef.setCheckpointKind(CheckpointKind.KAFKA_SEND_SUCCESS);
                        System.out.println("Sent message=[" + kafkaObject + "] with offset=["
                                + resultSend.getRecordMetadata().offset() + "]");
                    } else {
                        kafkaLogService.kafakaFail(kafkaLogId, "500", ex.getMessage());
                        kafkaRef.setCheckpointKind(CheckpointKind.KAFKA_SEND_FAIL);
                        System.out.println("Unable to send message=[" + kafkaObject + "] due to : " + ex.getMessage());
                    }
                });
                serviceResult.succeed("OK");
            } else {
                // 인증서 폐기 실패에 따른 프로세스 진행
                serviceResult.fail(500, "인증서 폐기 실패");
            }
        } catch (Exception e) {
            serviceResult.fail(500, e.getMessage());
        }
        return CompletableFuture.completedFuture(serviceResult);
    }

    @Async
    @Override
    public CompletableFuture<ServiceResult<String>> issueContract(PncReqBodyIssueContract request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();

        try {
            // 1. 요청바디의 값 확인
            // 1-1. pcid (vin 차대번호)
            String pcid = request.getPcid();
            // 1-2. oemId ("KMK")
            String oemId = request.getOemId();
            // 1-3. memberKey
            Long memberKey = request.getMemberKey();
            // 1-4. memberGroupId
            String memberGroupId = request.getMemberGroupId();
            // 1-5. memberGroupSeq
            Long memberGroupSeq = request.getMemberGroupSeq();

            // 2. 계약번호(emaid) 생성
            ServiceResult<ContractMeta> createEmptyResult = contractManageService.createEmptyContract();
            if (!createEmptyResult.getSuccess()) {
                // emaId 베이스 생성에 실패함.
                // kafka [MSG-EMSP-PNC-CONTRACT]로 실패 전달 (kafka 전달은 성패에 따라 분기하지 않으므로 결과를 수거하지
                // 않는다.)
                kafkaManageServcie.sendContCertFailKafka(null, oemId, pcid, memberKey, memberGroupId, memberGroupSeq,
                        trackId, "eMSP 서버 계약번호 생성에 실패했습니다.");
            }

            // 7자리 난수를 생성 (1000000 이상. 상한은 보장못함.)
            ContractMeta contractMeta = createEmptyResult.get();
            System.out.println("ContractMeta: " + contractMeta);
            int randomNumber = contractMeta.getEmaBaseNumber();
            System.out.println("Retrieved emaBaseNumber: " + randomNumber);
            String emaId = "KR" + "CEV" + "CA" + String.format("%07d", randomNumber);
            System.out.println("Generated emaId: " + emaId);
            // 3. KPIP에게 계약 인증서 생성 요청
            KpipReqBodyIssueContractCert req = new KpipReqBodyIssueContractCert();

            // "New" "Update" "Add" 세가지 선택 가능한데, 신규에 대해서만 사용함
            req.setReqType("New");
            // BMW에서 들어온 pcid 그대로 사용하면 될 것 같음 (차량 vin 번호)
            req.setPcid(pcid);
            // 계약번호. 위에서 생성한 계약번호 사용
            req.setEmaid(emaId);
            // oemID는 "BMW"로 고정하면 되지만 어차피 요청시에도 들어오기 때문에 들어온 값을 그대로 전달하기로 함
            req.setOemid(oemId);
            // 유효기간인데 기본값인 24로 고정해놓는 것으로 작성함
            req.setExpPolicy("24");

            ServiceResult<Map<String, Object>> kpipResult = kpipApiService.issueContractCert(req, trackId);
            serviceResult.getCheckpoints().addAll(kpipResult.getCheckpoints());

            if (!kpipResult.getSuccess()) {
                serviceResult.fail(500, "Kpip returns error : " + kpipResult.getErrorCode());
                // kafka [MSG-EMSP-PNC-CONTRACT]로 실패 전달 (kafka 전달은 성패에 따라 분기하지 않으므로 결과를 수거하지
                // 않는다.)
                kafkaManageServcie.sendContCertFailKafka(emaId, oemId, pcid, memberKey, memberGroupId, memberGroupSeq,
                        trackId, kpipResult.getErrorMessage());
                return CompletableFuture.completedFuture(serviceResult);
            }

            Map<String, Object> result = kpipResult.get();

            String resultCode = (String) result.get("resultCode");
            String resultMsg = (String) result.get("resultMsg");
            String contCert;
            // 돌아온 resultCode에 따라 분기
            if (resultCode.equals("OK")) {
                // 발급 성공
                contCert = (String) result.get("contCert");

                // 1. DB 업데이트
                String contractId = createEmptyResult.get().getContractId();

                ServiceResult<ContractMeta> issueResult = contractManageService.setIssuedContract(contractId, emaId,
                        pcid, oemId, memberKey, memberGroupId, memberGroupSeq,
                        contCert);
                if (!issueResult.getSuccess()) {
                    // 발급된 정보를 저장하는데 실패함.
                    // kafka [MSG-EMSP-PNC-CONTRACT]로 실패 전달 (kafka 전달은 성패에 따라 분기하지 않으므로 결과를 수거하지
                    // 않는다.)
                    kafkaManageServcie.sendContCertFailKafka(emaId, oemId, pcid, memberKey, memberGroupId,
                            memberGroupSeq, trackId, "eMSP 서버에서 발급된 인증서 처리에 실패했습니다.");
                }

                // 2. White-list push
                // 해당 계약건의 emaId 찾아서 kpipApiService.pushWhitelist() 으로 넘긴다
                KpipReqBodyPushWhitelistItem item = new KpipReqBodyPushWhitelistItem();
                item.setType("Add");
                item.setEmaid(emaId);
                List<KpipReqBodyPushWhitelistItem> reqList = new ArrayList<>();
                reqList.add(item);

                ServiceResult<Map<String, Object>> pushServiceResult = kpipApiService.pushWhitelist(reqList, trackId);

                serviceResult.getCheckpoints().addAll(pushServiceResult.getCheckpoints());
                if (!pushServiceResult.getSuccess()) {
                    serviceResult.fail(500, "Kpip returns error : " + pushServiceResult.getErrorCode());
                    return CompletableFuture.completedFuture(serviceResult);
                }

                // 이 타이밍이 화이트리스트 성공한것이라고 가정.
                ServiceResult<ContractMeta> whitelistResult = contractManageService.setWhitelistedContract(contractId);
                if (!whitelistResult.getSuccess()) {
                    // 화이트리스트 등록에 실패했을때 처리

                }
                Map<String, Object> pushResult = pushServiceResult.get();

                String pushResultCode = (String) pushResult.get("resultCode");
                // String pushResultMsg = (String) pushResult.get("resultMsg");

                // 전체 응답 리스트
                // List<KpipResBodyPushWhitelistItem> emaidList = new
                // ArrayList<KpipResBodyPushWhitelistItem>();
                // if (pushResult.containsKey("emaidList")) {
                // emaidList = (List<KpipResBodyPushWhitelistItem>) pushResult.get("emaidList");
                // }

                // // 구분해서 담을 리스트
                // List<KpipResBodyPushWhitelistItem> successList = new ArrayList<>();
                // List<KpipResBodyPushWhitelistItem> errorList = new ArrayList<>();

                // // 리스트 순회하며 각 건 별로 push에 성공했는지 실패했는지를 수집한다.
                // for (KpipResBodyPushWhitelistItem itm : emaidList) {
                // if ("Success".equals(itm.getResultMsg())) {
                // successList.add(itm);
                // } else {
                // errorList.add(itm);
                // }
                // }

                // List 중 일부만 처리 성공 => 이 케이스는 없을 것으로 추측함
                // (한꺼번에 여러 건을 push 할 수 있도록 List 형태로 요청/응답 설계되었으나)
                // (실제로 계약 생성, 폐기 시 해당 단 건에 대해서만 이벤트 기반으로 push 요청을 넣게 되므로)
                // (이 경우에는 언제나 보낼 때에도 List 내부에 단일 객체, 돌아올 때에도 단일 객체만 들어있음)
                // (일부 성공 (resultCode가 SOME인 경우)는 push 실패 케이스를 한꺼번에 모아 배치로 보내는 상황에서만 발생할 수 있음)

                // 2-2. 여기에서 실패 처리, 현재 논의된 방향은 whitelist 업데이트에 실패한 건들만 모아놨다가
                // 하루에 한 번 정도 (배치) 요청을 다시 보내는 방향으로 정리되었음.

                // kafka [MSG-EMSP-PNC-CONTRACT]로 성공 전달 (kafka 전달은 성패에 따라 분기하지 않으므로 결과를 수거하지
                // 않는다.)
                kafkaManageServcie.sendContCertSuccessKafka(emaId, oemId, pcid, memberKey, memberGroupId,
                        memberGroupSeq, contCert, trackId, "NORMAL");

                serviceResult.succeed("OK");
            } else {
                // KEPCO에서 응답이 정상적으로 돌아왔으나, 발급 실패 응답이 돌아온 경우
                serviceResult.fail(500, "발급 실패");
                // kafka [MSG-EMSP-PNC-CONTRACT]로 실패 전달 (kafka 전달은 성패에 따라 분기하지 않으므로 결과를 수거하지
                // 않는다.)
                kafkaManageServcie.sendContCertFailKafka(emaId, oemId, pcid, memberKey, memberGroupId, memberGroupSeq,
                        trackId, resultMsg);
            }
        } catch (Exception e) {
            serviceResult.fail(500, e.getMessage());
        }

        return CompletableFuture.completedFuture(serviceResult);
    }

    @Async
    @Override
    public CompletableFuture<ServiceResult<String>> revokeContract(PncReqBodyRevokeContractCert request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();

        String emaId = request.getEmaId();

        // MSP에서 들어오는 요청 바디와, KEPCO로 넘겨야하는 요청바디 동일하지만 emaId => emaid 로 키 대소문자 변경되어 한 번
        // 바꿔준다.
        KpipReqBodyEmaid req = new KpipReqBodyEmaid();
        req.setEmaid(emaId);

        ServiceResult<ContractMeta> queryResult = contractManageService.getContractByEmaId(emaId);
        if (!queryResult.getSuccess()) {
            // DB에 결과가 없음.
        }

        try {
            ServiceResult<Map<String, Object>> kpipResult = kpipApiService.revokeContractCert(req, trackId);

            serviceResult.getCheckpoints().addAll(kpipResult.getCheckpoints());

            if (!kpipResult.getSuccess()) {
                serviceResult.fail(500, "Kpip returns error : " + kpipResult.getErrorCode());
                return CompletableFuture.completedFuture(serviceResult);
            }

            Map<String, Object> result = kpipResult.get();

            String resultCode = (String) result.get("resultCode");
            String resultMsg = (String) result.get("resultMsg");

            if (resultCode.equals("OK")) {
                // KEPCO쪽 계약인증서 삭제 성공
                // 1. DB에서 해당 항목 업데이트
                String contractId = queryResult.get().getContractId();
                ServiceResult<ContractMeta> revokeResult = contractManageService.revokeContractById(contractId);
                if (!revokeResult.getSuccess()) {
                    // DB의 것이 revoke됨을 반영하는데 실패함.
                }

                // 2. push white-list를 통해 해당 계약건 상태 업데이트
                // 2-1. kpipApiService.pushWhitelist() 으로 넘긴다
                KpipReqBodyPushWhitelistItem item = new KpipReqBodyPushWhitelistItem();
                item.setType("Delete");
                item.setEmaid(emaId);
                List<KpipReqBodyPushWhitelistItem> reqList = new ArrayList<>();
                reqList.add(item);

                ServiceResult<Map<String, Object>> pushServiceResult = kpipApiService.pushWhitelist(reqList, trackId);

                serviceResult.getCheckpoints().addAll(pushServiceResult.getCheckpoints());
                if (!pushServiceResult.getSuccess()) {
                    serviceResult.fail(500, "Kpip returns error : " + pushServiceResult.getErrorCode());
                    return CompletableFuture.completedFuture(serviceResult);
                }
                // 여기서 화이트리스트에서 삭제가 되었다고 가정한다.
                ServiceResult<ContractMeta> undoWhiteResult = contractManageService
                        .undoWhitelistedContractg(contractId);
                if (!revokeResult.getSuccess()) {
                    // DB의 것이 whitelist에서 제거되었다는 것을 반영하는데 실패함.
                }

                Map<String, Object> pushResult = pushServiceResult.get();
                String pushResultCode = (String) pushResult.get("resultCode");
                String pushResultMsg = (String) pushResult.get("resultMsg");

                // 전체 응답 리스트
                List<KpipResBodyPushWhitelistItem> emaidList = new ArrayList<KpipResBodyPushWhitelistItem>();
                if (pushResult.containsKey("emaidList")) {
                    emaidList = (List<KpipResBodyPushWhitelistItem>) pushResult.get("emaidList");
                }

                // 구분해서 담을 리스트
                List<KpipResBodyPushWhitelistItem> successList = new ArrayList<>();
                List<KpipResBodyPushWhitelistItem> errorList = new ArrayList<>();

                // 리스트 순회하며 각 건 별로 push에 성공했는지 실패했는지를 수집한다.
                for (KpipResBodyPushWhitelistItem itm : emaidList) {
                    if ("Success".equals(itm.getResultMsg())) {
                        successList.add(itm);
                    } else {
                        errorList.add(itm);
                    }
                }

                // List 중 일부만 처리 성공 => 이 케이스는 없을 것으로 추측함
                // (한꺼번에 여러 건을 push 할 수 있도록 List 형태로 요청/응답 설계되었으나)
                // (실제로 계약 생성, 폐기 시 해당 단 건에 대해서만 이벤트 기반으로 push 요청을 넣게 되므로)
                // (이 경우에는 언제나 보낼 때에도 List 내부에 단일 객체, 돌아올 때에도 단일 객체만 들어있음)
                // (일부 성공 (resultCode가 SOME인 경우)는 push 실패 케이스를 한꺼번에 모아 배치로 보내는 상황에서만 발생할 수 있음)

                // 2-2. 여기에서 실패 처리, 현재 논의된 방향은 whitelist 업데이트에 실패한 건들만 모아놨다가
                // 하루에 한 번 정도 (배치) 요청을 다시 보내는 방향으로 정리되었음.

                // kafka [MSG-EMSP-PNC-CONTRACT]로 revoke 성공 전달 (kafka 전달은 성패에 따라 분기하지 않으므로 결과를
                // 수거하지 않는다.)
                String oemId = queryResult.get().getOemId();
                String pcid = queryResult.get().getPcid();
                Long memberKey = queryResult.get().getMemberKey();
                String memberGroupId = queryResult.get().getMemberGroupId();
                Long memberGroupSeq = queryResult.get().getMemberGroupSeq();
                kafkaManageServcie.sendContCertSuccessKafka(emaId, oemId, pcid, memberKey, memberGroupId,
                        memberGroupSeq, null, trackId, "DELETED");

                serviceResult.succeed("OK");
            } else {
                // KEPCO쪽 계약인증서 삭제 실패
                // 어쨌든 kakfa로 송신은 해줘야 함
                kafkaManageServcie.sendContCertSuccessKafka(emaId, null, null, null, null, null, null, trackId,
                        "DELETED");
                serviceResult.fail(500, resultMsg);
            }

        } catch (Exception e) {
            serviceResult.fail(500, e.getMessage());
        }

        return CompletableFuture.completedFuture(serviceResult);
    }

    @Override
    public ServiceResult<ContractInfo> getContractInfo(PncReqBodyContractInfo request, String trackId) {
        ServiceResult<ContractInfo> serviceResult = new ServiceResult<>();

        try {

            String pcid = request.getPcid();
            String oemId = request.getOemId();
            Long memberKey = request.getMemberKey();

            // 1. 정보만 찾아준다면 들어온 pcid(vin번호), memberkey(회원번호)로 DB와 매칭해서 아래 정보 찾는다.
            ServiceResult<ContractMeta> findResult = contractManageService.findContractByMetaData(memberKey, pcid,
                    oemId);
            if (!findResult.getSuccess()) {
                // DB에 매칭되는 정보가 없음.
                serviceResult.fail(500, "DB 매칭 실패");
            }
            String contractId = findResult.get().getContractId();
            // 1-1. emaid
            String emaid = findResult.get().getEmaId();
            // 1-2. 계약 시작/종료 일자 (따로 저장중이라면 저장된 정보 가져오고, 저장중이 아니라면 인증서로부터 추출한다)

            // String변환기준과 사용처가 없어서 일단 null로 나오도록 처리함.
            String contractStartDt = findResult.get().getContractStartDtString();
            String contractEndDt = findResult.get().getContractEndDtString();

            // // 2. cert의 유효성까지 검증한다면 KEPCO로 유효성 검증 날린다.
            // // 2-1.들어온 pcid(vin번호), memberkey(회원번호)로 매칭되는 계약의 계약인증서를 찾아낸다.
            // // (만약 PEM으로 보관중이라면 DER로 변환해야 함)
            // ServiceResult<String> fullContResult =
            // contractManageService.getFullContCert(contractId);
            // if(!fullContResult.getSuccess()){
            // // contcert를 가져오는데 실패했다.
            // }
            // String contCert = fullContResult.get();

            // KpipReqBodyVerifyContractCert verifyRequest = new
            // KpipReqBodyVerifyContractCert();
            // verifyRequest.setContCert(contCert);
            // // 2-2. 유효한 계약인지 확인한다.
            // ServiceResult<Map<String, Object>> verifyServiceResult =
            // kpipApiService.verifyContractCert(verifyRequest,
            // trackId);

            // serviceResult.getCheckpoints().addAll(verifyServiceResult.getCheckpoints());

            // if (!verifyServiceResult.getSuccess()) {
            // serviceResult.fail(500, "Kpip returns error : " +
            // verifyServiceResult.getErrorCode());
            // return serviceResult;
            // }

            // Map<String, Object> verifyResult = verifyServiceResult.get();

            // String resultCode = (String) verifyResult.get("resultCode");
            // String resultMsg = (String) verifyResult.get("resultMsg");
            // String status = (String) verifyResult.get("status");

            // if (resultCode.equals("OK")) {
            // // KEPCO쪽 계약인증서 검증 성공
            // // 이후 로직 처리
            // } else {
            // // KEPCO쪽 계약인증서 검증 실패
            // // resultMsg에 따른 분기 필요할 것으로 보임
            // }

            // switch (status) {
            // case "Good":
            // // 검증 성공
            // break;
            // case "Revoked":
            // // 검증 되었으나 만료된 인증서임
            // break;
            // case "Unknowns":
            // // 검증 실패. 알 수 없는 인증서임.
            // break;
            // default:
            // // 기타 경우. (없음)
            // break;
            // }

            // 3. cert의 유효성까지 검증하고자 한다면, 만약 KEPCO의 검증이 실패하는 상황에 대한 시나리오도 필요하다.
            // 이 부분은 pncAuthorize와 동일하며, CRL과 DB의 정보로 유효성을 검증한다.
            // (만약 getContractInfo 전체 프로세스에서 cert 검증까지 같이 하지 않고 단순 계약정보만 반환한다는 시나리오라면, 2번과
            // 3번은 불필요하다.)

            ContractInfo resultData = new ContractInfo();

            resultData.setEmaid(emaid);

            serviceResult.succeed(resultData);
        } catch (Exception e) {
            serviceResult.fail(500, e.getMessage());
        }

        return serviceResult;
    }

    @Override
    public ServiceResult<String> pncAuthorize(PncReqBodyAuthorize request, String trackId) {
        ServiceResult<String> serviceResult = new ServiceResult<>();

        String emaId = request.getEmaId();
        // 아래 정보들은 MSP에서 올라오긴 하는데, 실제 계약인증서 검증 시에는 emaId만 가지고 검증하므로 검증 과정에서는 필요 없다.
        //String contractCertificateHashData = request.getContractCertificateHashData();
        String contractCertificateHashDate = null;
        long ecKey = request.getEcKey();

        // DB에서 체크
        ServiceResult<ContractMeta> queryResult = contractManageService.getContractByEmaId(emaId);
        if (!queryResult.getSuccess()) {
            // DB에 없음.
        }
        String contractId = queryResult.get().getContractId();

        // MSP에서 들어오는 요청 바디와, KEPCO로 넘겨야하는 요청바디 emaId => emaid 로 키 대소문자 변경되어 한 번 바꿔준다.
        KpipReqBodyEmaid req = new KpipReqBodyEmaid();
        req.setEmaid(emaId);

        try {
            // 1. KEPCO로 검증 요청 보내서 결과에 따라 처리한다.
            ServiceResult<Map<String, Object>> kpipServiceResult = kpipApiService.authorizePnc(req, trackId);

            serviceResult.getCheckpoints().addAll(kpipServiceResult.getCheckpoints());

            if (!kpipServiceResult.getSuccess()) {
                serviceResult.fail(500, "Kpip returns error : " + kpipServiceResult.getErrorCode());
                return serviceResult;
            }

            Map<String, Object> result = kpipServiceResult.get();
            String resultCode = (String) result.get("resultCode");
            String resultMsg = (String) result.get("resultMsg");

            if (resultCode.equals("OK")) {
                // 계약인증서 검증 성공
                serviceResult.succeed("OK");
            } else {
                // 처리 실패
                serviceResult.fail(500, resultMsg);
            }

        } catch (Exception e) {
            // 2. KEPCO로 요청 보내는 것 자체에 실패했을 때, 가지고 있는 CRL과 eMSP DB 정보로 우선 유효성을 검증해준다.
            serviceResult.fail(500, e.getMessage());
        }
        if (serviceResult.getErrorCode() == 500) {
            // 기타문제로 실패했을 때, 유효성을 내부에서 검증한다.
            ServiceResult<ContractMeta> authResult = contractManageService.checkAuth(contractId);
            if (authResult.getSuccess()) {
                // 아무튼 성공함.
                serviceResult.succeed("OK");
            } else {
                serviceResult.fail(authResult.getErrorCode(), authResult.getErrorMessage());
            }
        }

        return serviceResult;
    }

}
