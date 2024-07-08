package com.chargev.emsp.service.kafka;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.chargev.emsp.model.dto.pnc.Authorities;
import com.chargev.emsp.model.dto.pnc.CertStatus;
import com.chargev.emsp.model.dto.pnc.EvseCertificate;
import com.chargev.emsp.model.dto.pnc.PncContract;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.cryptography.CertificateConversionService;
import com.chargev.emsp.service.formatter.DateTimeFormatterService;
import com.chargev.emsp.service.http.KpipApiService;
import com.chargev.emsp.service.log.CheckpointKind;
import com.chargev.emsp.service.log.CheckpointReference;
import com.chargev.emsp.service.log.KafkaLogService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaManageServiceImpl implements KafkaManageService {

    private final DateTimeFormatterService dateTimeFormatterService;
    private final KpipApiService kpipApiService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CertificateConversionService certificateConversionService;
    private final KafkaLogService kafkaLogService;
    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");

    private final ObjectMapper objectMapper;

    @Override
    public ServiceResult<String> sendContCertSuccessKafka(String emaId, String oemId, String pcid, Long memberKey,
            String memberGroupId, Long memberGroupSeq, String contCert, String trackId, String type) {

        String topic = "MSG-EMSP-PNC-CONTRACT";
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info("TAG: KAFKA_TRY, TOPIC: {}, Track ID: {}", topic, trackId);
        }

        // 계약인증서 성공 케이스에서 kafka[MSG-EMSP-PNC-CONTRACT]로 전달
        ServiceResult<String> result = new ServiceResult<>();

        PncContract kafkaObject = new PncContract();

        kafkaObject.setEmaId(emaId);
        kafkaObject.setMemberKey(memberKey);
        kafkaObject.setMemberGroupId(memberGroupId);
        kafkaObject.setMemberGroupSeq(memberGroupSeq);
        kafkaObject.setOemCode(oemId);
        kafkaObject.setVin(pcid);
        kafkaObject.setResult("SUCCESS");

        // 여기서부터는 만들어야 하는 값
        try {
            // (1) 받은 Base64 DER Encoded 인증서를 PEM으로 변환
            String pemCert = certificateConversionService.convertToPEM(contCert);
            kafkaObject.setCertificate(pemCert);
            // (2) 받은 contCert로부터 만료날짜 추출
            Date expiredDate = certificateConversionService.getExpiredDate(contCert);
            ZonedDateTime zonedExpiredDate = ZonedDateTime.ofInstant(expiredDate.toInstant(), ZoneId.of("UTC"));
            String expiredDateString = dateTimeFormatterService.formatToSimpleStyle(zonedExpiredDate);
            kafkaObject.setExpiredDate(expiredDateString);
            // (3) 받은 contCert로부터 발급날짜 추출
            Date requestDate = certificateConversionService.getExpiredDate(contCert);
            ZonedDateTime zonedRequestdDate = ZonedDateTime.ofInstant(requestDate.toInstant(), ZoneId.of("UTC"));
            String requestDateString = dateTimeFormatterService.formatToSimpleStyle(zonedRequestdDate);
            kafkaObject.setRequestDate(requestDateString);
        } catch (Exception e) {
            // 이건 kafka 전송 실패가 아니라 값 만들기 실패 케이스이므로 분기가 필요할 것으로 보임
            System.out.println("Fail to make message :" + e.getMessage());
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: KAFKA_CONVERT_CERT, TOPIC: {}, Track ID: {}, Message: {}", topic, trackId,
                        "PEM으로의 전환에 실패하였습니다.");
            }
        }

        // 발급 주체에 대한 별도 논의 없었으므로 "KEPCO"로만 세팅한다.
        kafkaObject.setAuthorities(Authorities.KEPCO);
        // 발급 직후 보내는지, 삭제 직후 보내는지에 따라 다르게 세팅한다.
        if (type != null) {
            if (type.equals("NORMAL")) {
                kafkaObject.setStatus(CertStatus.NORMAL);
            } else if (type.equals("DELETED")) {
                kafkaObject.setStatus(CertStatus.DELETED);
            } else if (type.equals("EXPIRED")) {
                kafkaObject.setStatus(CertStatus.EXPIRED);
            }
        }
        // 여기에서 이제 만든 kafkaObject를 kafka로 전송
        String kafkaLogId = kafkaLogService.kafkaLogStart(topic, kafkaObject, trackId);
        CheckpointReference kafkaRef = new CheckpointReference(kafkaLogId);
        result.getCheckpoints().add(kafkaRef);
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, kafkaObject);
        String bodyJson = null;
        try {
            bodyJson = objectMapper.writeValueAsString(kafkaObject);
        } catch (Exception ex) {
            apiLogger.error("kafka Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info("TAG: KAFKA_SEND, TOPIC: {}, Track ID: {}, Object: {}", topic, trackId, bodyJson);
        }

        future.whenComplete((resultSend, ex) -> {
            if (ex == null) {
                kafkaLogService.kafkaFinish(kafkaLogId, resultSend.getRecordMetadata().offset());
                kafkaRef.setCheckpointKind(CheckpointKind.KAFKA_SEND_SUCCESS);
                System.out.println("Sent message=[" + kafkaObject + "] with offset=["
                        + resultSend.getRecordMetadata().offset() + "]");

                if (apiLogger.isInfoEnabled()) {
                    apiLogger.info("TAG: KAFKA_SENT, TOPIC: {}, Track ID: {}", topic, trackId);
                }

            } else {
                kafkaLogService.kafakaFail(kafkaLogId, "500", ex.getMessage());
                kafkaRef.setCheckpointKind(CheckpointKind.KAFKA_SEND_FAIL);
                System.out.println("Unable to send message=[" + kafkaObject + "] due to : " + ex.getMessage());

                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG: KAFKA_SENT, TOPIC: {}, Track ID: {}, Message: {}", topic, trackId,
                            ex.getMessage());
                }
            }
        });

        result.succeed("OK");
        return result;
    }

    @Override
    public ServiceResult<String> sendContCertFailKafka(String emaId, String oemId, String pcid, Long memberKey,
            String memberGroupId, Long memberGroupSeq, String trackId, String message) {
        // 계약인증서 실패 케이스에서 kafka[MSG-EMSP-PNC-CONTRACT]로 전달
        // 실패는 발급, 재발급, 폐기에서 모두 나올 수 있음
        String topic = "MSG-EMSP-PNC-CONTRACT";
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info("TAG: KAFKA_TRY, TOPIC: {}, Track ID: {}", topic, trackId);
        }

        ServiceResult<String> result = new ServiceResult<>();

        PncContract kafkaObject = new PncContract();

        // 1. 발급/재발급 실패의 경우 => emaId는 없을 수 있고, 나머지는 모두 존재함
        // 2. 폐기 실패의 경우 => emaId만 존재하고, 나머지는 존재하지 않음
        kafkaObject.setEmaId(emaId);
        kafkaObject.setMemberKey(memberKey);
        kafkaObject.setMemberGroupId(memberGroupId);
        kafkaObject.setMemberGroupSeq(memberGroupSeq);
        kafkaObject.setOemCode(oemId);
        kafkaObject.setVin(pcid);
        kafkaObject.setResult("FAIL");
        kafkaObject.setMessage(message);

        // 인증서 관련된 값들은 실패이므로 전부 null로 보낸다.

        // 여기에서 이제 만든 kafkaObject를 kafka로 전송
        String kafkaLogId = kafkaLogService.kafkaLogStart(topic, kafkaObject, trackId);
        CheckpointReference kafkaRef = new CheckpointReference(kafkaLogId);
        result.getCheckpoints().add(kafkaRef);
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, kafkaObject);

        String bodyJson = null;
        try {
            bodyJson = objectMapper.writeValueAsString(kafkaObject);
        } catch (Exception ex) {
            apiLogger.error("kafka Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info("TAG: KAFKA_SEND, TOPIC: {}, Track ID: {}, Object: {}", topic, trackId, bodyJson);
        }

        future.whenComplete((resultSend, ex) -> {
            if (ex == null) {
                kafkaLogService.kafkaFinish(kafkaLogId, resultSend.getRecordMetadata().offset());
                kafkaRef.setCheckpointKind(CheckpointKind.KAFKA_SEND_SUCCESS);
                System.out.println("Sent message=[" + kafkaObject + "] with offset=["
                        + resultSend.getRecordMetadata().offset() + "]");

                if (apiLogger.isInfoEnabled()) {
                    apiLogger.info("TAG: KAFKA_SENT, TOPIC: {}, Track ID: {}", topic, trackId);
                }

            } else {
                kafkaLogService.kafakaFail(kafkaLogId, "500", ex.getMessage());
                kafkaRef.setCheckpointKind(CheckpointKind.KAFKA_SEND_FAIL);
                System.out.println("Unable to send message=[" + kafkaObject + "] due to : " + ex.getMessage());
                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG: KAFKA_SENT, TOPIC: {}, Track ID: {}, Message: {}", topic, trackId,
                            ex.getMessage());
                }
            }
        });

        result.succeed("OK");
        return result;
    }

    @Override
    public void sendCertResult(EvseCertificate kafkaObject, String trackId) {
        String topic = "MSG-EMSP-EVSE-CERTIFICATE";
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info("TAG: KAFKA_TRY, TOPIC: {}, Track ID: {}", topic, trackId);
        }

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, kafkaObject);

        String bodyJson = null;
        try {
            bodyJson = objectMapper.writeValueAsString(kafkaObject);
        } catch (Exception ex) {
            apiLogger.error("kafka Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info("TAG: KAFKA_SEND, TOPIC: {}, Track ID: {}, Object: {}", topic, trackId, bodyJson);
        }
        future.whenComplete((resultSend, ex) -> {
            if (ex == null) {

                if (apiLogger.isInfoEnabled()) {
                    apiLogger.info("TAG: KAFKA_SENT, TOPIC: {}, Track ID: {}", topic, trackId);
                }

            } else {

                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG: KAFKA_SENT, TOPIC: {}, Track ID: {}, Message: {}", topic, trackId,
                            ex.getMessage());
                }
            }
        });

    }

    @Override
    public void sendSuspensionResult(PncContract kafkaObject, String trackId) {
        String topic = "MSG-EMSP-PNC-CONTRACT";
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info("TAG: KAFKA_TRY, TOPIC: {}, Track ID: {}", topic, trackId);
        }

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, kafkaObject);

        String bodyJson = null;
        try {
            bodyJson = objectMapper.writeValueAsString(kafkaObject);
        } catch (Exception ex) {
            apiLogger.error("kafka Body 직렬화 실패.");
        }
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info("TAG: KAFKA_SEND, TOPIC: {}, Track ID: {}, Object: {}", topic, trackId, bodyJson);
        }
        future.whenComplete((resultSend, ex) -> {
            if (ex == null) {

                if (apiLogger.isInfoEnabled()) {
                    apiLogger.info("TAG: KAFKA_SENT, TOPIC: {}, Track ID: {}", topic, trackId);
                }

            } else {

                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG: KAFKA_SENT, TOPIC: {}, Track ID: {}, Message: {}", topic, trackId,
                            ex.getMessage());
                }
            }
        });
    }
}
