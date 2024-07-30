package com.chargev.emsp.service.oem;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.chargev.emsp.entity.oem.EmspAccountEntity;
import com.chargev.emsp.entity.oem.EmspContractEntity;
import com.chargev.emsp.entity.oem.EmspContractHistoryEntity;
import com.chargev.emsp.entity.oem.EmspRfidEntity;
import com.chargev.emsp.model.dto.oem.EmspKafkaRfid;
import com.chargev.emsp.model.dto.oem.EmspStatus;
import com.chargev.emsp.repository.oem.EmspAccountRepository;
import com.chargev.emsp.repository.oem.EmspContractHistoryRepository;
import com.chargev.emsp.repository.oem.EmspContractRepository;
import com.chargev.emsp.repository.oem.EmspRfidRepository;
import com.chargev.emsp.service.kafka.KafkaEmspManageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OemServiceUtils {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final EmspContractRepository emspContractRepository;
    private final EmspRfidRepository emspRfidRepository;
    private final EmspAccountRepository emspAccountRepository;
    private final EmspContractHistoryRepository emspContractHistoryRepository;
    private final KafkaEmspManageService kafkaEmspManageService;

        //#region Utils - lock/unlock
    @Transactional
    public EmspContractEntity lockContract(String contractId, String reason) {
        EmspContractEntity contractToTerminate = findContractByIdAndStatus(contractId, 1,2);
        contractToTerminate.setContractStatus(2);
        contractToTerminate.setContractStatusReason(reason);
        emspContractRepository.save(contractToTerminate);

        // TODO: Lock된 Contract는 Terminate 프로세스에 태워야 한다.
        return contractToTerminate;
    }

    @Transactional
    public EmspContractEntity unlockContract(String contractId, String reason) {
        EmspContractEntity contractToTerminate = findContractByIdAndStatus(contractId, 2);
        contractToTerminate.setContractStatus(1);
        contractToTerminate.setContractStatusReason(reason);
        emspContractRepository.save(contractToTerminate);

        return contractToTerminate;
    }

    @Transactional
    public EmspRfidEntity lockRfid(String contractId, String reason) {
        if (emspRfidRepository.existsByContractIdAndStatus(contractId, 2)) {
            EmspRfidEntity lockedRfid = findRfidByContractIdAndStatus(contractId, 2);
            lockedRfid.setStatus(3);
            emspRfidRepository.save(lockedRfid);
        }

        EmspRfidEntity rfid = findRfidByContractIdAndStatus(contractId, 1);
        rfid.setStatus(2);
        rfid.setStatusReason(reason);
        emspRfidRepository.save(rfid);

        EmspContractEntity contract = findContractById(contractId);
        contract.setRfidStatus(2);
        contract.setRfidStatusReason(reason);
        emspContractRepository.save(contract);

        sendRfidModifyAlert(contract, "UNAVAILABLE", reason);

        return rfid;
    }

    @Transactional
    public EmspRfidEntity unlockRfid(String contractId, String reason) {
        EmspRfidEntity rfid = findRfidByContractIdAndStatus(contractId, 2);
        rfid.setStatus(1);
        rfid.setStatusReason(null); // active일때는 reason을 공통적으로 비워놓는게 나을듯하다.
        emspRfidRepository.save(rfid);

        EmspContractEntity contract = findContractById(contractId);
        contract.setRfidStatus(1);
        contract.setRfidStatusReason(null); // active일때는 reason을 공통적으로 비워놓는게 나을듯하다.
        emspContractRepository.save(contract);

        sendRfidModifyAlert(contract, "AVAILABLE", reason);

        return rfid;
    }

    @Transactional
    public EmspRfidEntity terminateRfid(String contractId, String reason) {
        EmspRfidEntity rfid = findRfidByContractIdAndStatus(contractId, 1,2);
        rfid.setStatus(3);
        rfid.setStatusReason(reason);
        emspRfidRepository.save(rfid);

        EmspContractEntity contract = findContractById(contractId);
        contract.setRfidStatus(3);
        contract.setRfidStatusReason(reason);
        emspContractRepository.save(contract);

        sendRfidModifyAlert(contract, "UNAVAILABLE", reason);

        return rfid;
    }
    //#endregion

    //#region Utils - gets from repo
    public EmspRfidEntity findRfidByContractIdAndStatus(String contractId, Integer... statuses) {
        List<Integer> statusList = Arrays.asList(statuses);
        return emspRfidRepository.findByContractIdAndStatusIn(contractId, statusList)
                .orElseThrow(() -> new IllegalArgumentException("RFID를 찾을 수 없습니다."));
    }

    public EmspRfidEntity findRfidByRfNum(String rfNum) {
        return emspRfidRepository.findByRfNum(rfNum)
                .orElseThrow(() -> new IllegalArgumentException("입력한 번호와 일치하는 RFID를 찾을 수 없습니다. 카드 번호를 다시 확인해주세요."));
    }

    public EmspAccountEntity findAccountByKeyAndAccountStatus(String emspAccountKey, Integer... statuses) {
        List<Integer> statusList = Arrays.asList(statuses);
        return emspAccountRepository.findByEmspAccountKeyAndAccountStatusIn(emspAccountKey, statusList)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
    }

    public EmspContractEntity findContractByAccountAndIdAndStatus(EmspAccountEntity emspAccount, String contractId, Integer... statuses) {
        List<Integer> statusList = Arrays.asList(statuses);
        return emspContractRepository.findByAccountAndContractIdAndContractStatusIn(emspAccount, contractId, statusList)
                .orElseThrow(() -> new IllegalArgumentException("계약을 찾을 수 없습니다."));
    }

    public EmspContractEntity findContractByIdAndStatus(String contractId, Integer... statuses) {
        List<Integer> statusList = Arrays.asList(statuses);
        return emspContractRepository.findByContractIdAndContractStatusIn(contractId, statusList)
                .orElseThrow(() -> new IllegalArgumentException("계약을 찾을 수 없습니다."));
    }

    public EmspContractEntity findContractById(String contractId) {
        return emspContractRepository.findByContractId(contractId)
                .orElseThrow(() -> new IllegalArgumentException("계약을 찾을 수 없습니다."));
    }

    public EmspContractEntity findContractByRfidNumAndRfidStatusAndContractStatus(String rfNum, Integer rfidStatus, Integer contractStatus) {
        return emspContractRepository.findByRfidNumAndRfidStatusAndContractStatus(rfNum, rfidStatus, contractStatus)
                .orElseThrow(() -> new IllegalArgumentException("유효한 RFID를 찾을 수 없습니다."));
    }

    public EmspContractEntity findContractByContractIdAndContractStatusAndRfidStatus(String contractId, Integer contractStatus, Integer rfidStatus) {
        return emspContractRepository.findByContractIdAndContractStatusAndRfidStatus(contractId, contractStatus, rfidStatus)
                .orElseThrow(() -> new IllegalArgumentException("유효한 계약을 찾을 수 없습니다."));
    }
    //#endregion

    @Transactional
    public void createHistoryRecord(EmspContractEntity contract, String reason) {
        EmspContractHistoryEntity historyRecord = new EmspContractHistoryEntity();

        for (Field field : EmspContractEntity.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Field historyField = EmspContractHistoryEntity.class.getDeclaredField(field.getName());
                historyField.setAccessible(true);
                historyField.set(historyRecord, field.get(contract));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // 필드가 없으면 무시
            }
        }

        historyRecord.setContractHistoryId(UUID.randomUUID().toString().replace("-", ""));
        // historyRecord.setContractId(contract.getContractId());
        historyRecord.setAccountId(contract.getAccount().getEmspAccountKey());
        historyRecord.setLastUpdatedAt(new Date());
        historyRecord.setUpdateReason(reason);

        emspContractHistoryRepository.save(historyRecord);
    }

    //#region Utils - formatting
    public String formatDate(Date date) {
        return DATE_FORMATTER.format(date);
    }

    public Date parseDate(String dateString) throws ParseException {
        return DATE_FORMATTER.parse(dateString);
    }
    //#endregion

    public void sendRfidModifyAlert(EmspContractEntity contract, String status, String reason) {
        EmspKafkaRfid kafkaObject = new EmspKafkaRfid();
        kafkaObject.setOemCode("BENZ");
        kafkaObject.setRfId(contract.getRfidNum());
        kafkaObject.setRfIdStatus(status);
        kafkaObject.setRequestDate(formatDate(new Date()));

        kafkaEmspManageService.sendRfidModify(kafkaObject, reason);
    }

    public EmspStatus getEmspStatusEnumFromInt(Integer status) {
        return switch (status) {
            case 1 -> EmspStatus.ACTIVE;
            case 2 -> EmspStatus.LOCKED;
            case 3 -> EmspStatus.TERMINATED;
            default -> null;
        };
    }
}
