package com.chargev.emsp.service.oem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chargev.emsp.entity.oem.EmspAccountEntity;
import com.chargev.emsp.entity.oem.EmspContractEntity;
import com.chargev.emsp.entity.oem.EmspRfidEntity;
import com.chargev.emsp.model.dto.oem.EmspRfidCard;
import com.chargev.emsp.model.dto.oem.EmspRfidCardIssuedDetail;
import com.chargev.emsp.model.dto.oem.EmspRfidCardRegistration;
import com.chargev.emsp.model.dto.oem.EmspRfidCardRequest;
import com.chargev.emsp.repository.oem.EmspContractRepository;
import com.chargev.emsp.repository.oem.EmspRfidRepository;
import com.chargev.emsp.service.ServiceResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RfidServiceImpl implements RfidService {
    private final EmspContractRepository emspContractRepository;
    private final EmspRfidRepository emspRfidRepository;
    private final OemServiceUtils oemServiceUtils;

    @Override
    public ServiceResult<EmspRfidCardIssuedDetail> issueRfidCard(EmspRfidCardRequest request, String emspContractId, String trackId) {
        ServiceResult<EmspRfidCardIssuedDetail> result = new ServiceResult<>();
        // TODO : API 정의 필요
        return result;
    }

    @Override
    @Transactional
    public ServiceResult<EmspRfidCard> registerRfid(String emspAccountKey, String emspContractId, EmspRfidCardRegistration request, String trackId) {
        ServiceResult<EmspRfidCard> result = new ServiceResult<>();

        try {
            EmspAccountEntity account = oemServiceUtils.findAccountByKeyAndAccountStatus(emspAccountKey, 1);
            EmspContractEntity contract = oemServiceUtils.findContractByAccountAndIdAndStatus(account, emspContractId, 1);
            if(contract.getRfidStatus() == 1 || contract.getRfidStatus() == 2) {
                EmspRfidEntity rfidActivated = oemServiceUtils.findRfidByContractIdAndStatus(contract.getContractId(), 1);
                if (rfidActivated != null) {
                    result.fail(400, "이미 활성화된 RFID가 존재합니다.");
                    return result;
                }
                EmspRfidEntity rfidLocked = oemServiceUtils.findRfidByContractIdAndStatus(contract.getContractId(), 2);
                if (rfidLocked != null) {
                    // Lock된 RFID는 TERMINATED 처리한다.
                    // Contract 테이블에도 업데이트를 해야할까?
                    // 어차피 Contract 테이블에는 다음 단계에서 새 RFID 카드가 업데이트 되므로 여기에서는 그냥 둔다.
                    rfidLocked.setStatus(3);
                    emspRfidRepository.save(rfidLocked);
                }
            }
    
            // 이제 새 카드를 등록한다.
            // TODO : 0. RFID ORDER 테이블에서 카드 상태를 업데이트 한다. (배송 완료에 대한 TAG는 없지만 이력용으로 임의로 상태 "5"를 적용한다.)
    
            // 1. RFID 테이블에서 카드상태와 contract를 업데이트 한다.
            EmspRfidEntity rfidNew = oemServiceUtils.findRfidByRfNum(request.getRfidCardNumber());
            rfidNew.setStatus(2);
            rfidNew.setRegistrationDate(new Date()); // TODO : 실제로는 0에서 가져온 ORDER TABLE의 정보를 사용해 여기를 채워야 함
            rfidNew.setContractId(contract.getContractId());
            rfidNew.setUpdatedAt(new Date());
            rfidNew = emspRfidRepository.save(rfidNew);
    
            // 2. CONTRACT 테이블에서 카드상태와 contract를 업데이트 한다.
            contract.setRfidId(rfidNew.getId());
            contract.setRfidNum(rfidNew.getRfNum());
            contract.setRfidStatus(rfidNew.getStatus());
            contract.setRfidRegistrationDate(rfidNew.getRegistrationDate());
            contract = emspContractRepository.save(contract);
    
            // 3. CONTRACT 테이블이 업데이트되었으니 이력도 한 번 찍어준다.
            oemServiceUtils.createHistoryRecord(contract, "신규 RFID 등록");
    
            // 4. 응답에 사용될 rfid 객체 생성
            EmspRfidCard emspRfidCard = new EmspRfidCard();
            emspRfidCard.setCardId(rfidNew.getId());
            emspRfidCard.setCardNumber(rfidNew.getRfNum());
            emspRfidCard.setStatus(rfidNew.getStatus() == 1 ? "Active" : "InActive");
            emspRfidCard.setRegistrationDate(oemServiceUtils.formatDate(rfidNew.getRegistrationDate()));
    
            // TODO : 성패여부 확인 필요
            oemServiceUtils.sendRfidModifyAlert(contract, "AVAILABLE", "REGISTRATION");
    
            result.succeed(emspRfidCard);
        } catch (IllegalArgumentException e) {
            result.fail(400, e.getMessage());
        } catch (Exception e) {
            result.fail(500, "서버 에러가 발생했습니다.");
        }

        return result;
    }

    @Override
    @Transactional
    public ServiceResult<EmspRfidCard> modifyRfidStatus(String emspAccountKey, String emspContractId, EmspRfidCard request, String trackId) {
        ServiceResult<EmspRfidCard> result = new ServiceResult<>();

        try {
            EmspAccountEntity account = oemServiceUtils.findAccountByKeyAndAccountStatus(emspAccountKey, 1);
            EmspContractEntity contract = oemServiceUtils.findContractByAccountAndIdAndStatus(account, emspContractId, 1);
            EmspRfidEntity rfid = oemServiceUtils.findRfidByContractIdAndStatus(contract.getContractId(), 1,2);
    
            boolean isActivated = rfid.getStatus() == 1;
            boolean isLocked = rfid.getStatus() == 2;
            boolean isRequestActivating = request.getStatus().equals("Active");
            boolean isRequestLocking = request.getStatus().equals("Inactive");
    
            if(isActivated && isRequestLocking) {
                rfid = oemServiceUtils.lockRfid(emspContractId, request.getReason());
            } else if(isLocked && isRequestActivating) {
                rfid = oemServiceUtils.unlockRfid(emspContractId, request.getReason());
            }
    
            EmspRfidCard emspRfidCard = new EmspRfidCard();
            emspRfidCard.setCardId(rfid.getId());
            emspRfidCard.setCardNumber(rfid.getRfNum());
            emspRfidCard.setStatus(rfid.getStatus() == 1 ? "Active" : "InActive");
            emspRfidCard.setReason(rfid.getStatusReason());
            emspRfidCard.setRegistrationDate(oemServiceUtils.formatDate(rfid.getRegistrationDate()));
    
            result.succeed(emspRfidCard);
        } catch (IllegalArgumentException e) {
            result.fail(400, e.getMessage());
        } catch (Exception e) {
            result.fail(500, "서버 에러가 발생했습니다.");
        }


        return result;
    }

    @Override
    @Transactional
    public ServiceResult<String> deleteRfidById(String emspAccountKey, String emspContractId, String cardId) {
        ServiceResult<String> result = new ServiceResult<>();

        // 기획이 명확하지 않다. 우선은 상태 1,2인 rfid를 상태 3(Terminated)로 변경하는 것으로 하자.

        try {
            EmspAccountEntity account = oemServiceUtils.findAccountByKeyAndAccountStatus(emspAccountKey, 1);
            EmspContractEntity contract = oemServiceUtils.findContractByAccountAndIdAndStatus(account, emspContractId, 1, 2);
            
            oemServiceUtils.terminateRfid(contract.getContractId(), "SYSTEM_RFID_Termination");
    
            result.succeed("OK");
        } catch (IllegalArgumentException e) {
            result.fail(400, e.getMessage());
        } catch (Exception e) {
            result.fail(500, "서버 에러가 발생했습니다.");
        }

        return result;
    }

    @Override
    public ServiceResult<List<EmspRfidCard>> getRfids(String emspAccountKey, String emspContractId, String trackId) {
        ServiceResult<List<EmspRfidCard>> result = new ServiceResult<>();

        try {
            EmspAccountEntity account = oemServiceUtils.findAccountByKeyAndAccountStatus(emspAccountKey, 1);
            EmspContractEntity contract = oemServiceUtils.findContractByAccountAndIdAndStatus(account, emspContractId, 1, 2);
            EmspRfidEntity rfid = oemServiceUtils.findRfidByContractIdAndStatus(contract.getContractId(), 1, 2);
    
            EmspRfidCard emspRfidCard = new EmspRfidCard();
            emspRfidCard.setCardId(rfid.getId());
            emspRfidCard.setCardNumber(rfid.getRfNum());
            emspRfidCard.setStatus(rfid.getStatus() == 1 ? "Active" : "InActive");
            emspRfidCard.setReason(rfid.getStatusReason());
            emspRfidCard.setRegistrationDate(oemServiceUtils.formatDate(rfid.getRegistrationDate()));
    
            List<EmspRfidCard> emspRfidCards = new ArrayList<>();
            emspRfidCards.add(emspRfidCard);
    
            result.succeed(emspRfidCards);
        } catch (IllegalArgumentException e) {
            result.fail(400, e.getMessage());
        } catch (Exception e) {
            result.fail(500, "서버 에러가 발생했습니다.");
        }

        return result;
    };

    @Override
    public ServiceResult<EmspRfidCard> getRfid(String emspAccountKey, String emspContractId, String cardId, String trackId) {
        ServiceResult<EmspRfidCard> result = new ServiceResult<>();

        try {
            EmspAccountEntity account = oemServiceUtils.findAccountByKeyAndAccountStatus(emspAccountKey, 1);
            EmspContractEntity contract = oemServiceUtils.findContractByAccountAndIdAndStatus(account, emspContractId, 1, 2);
            EmspRfidEntity rfid = oemServiceUtils.findRfidByContractIdAndStatus(contract.getContractId(), 1, 2);
    
            EmspRfidCard emspRfidCard = new EmspRfidCard();
            emspRfidCard.setCardId(rfid.getId());
            emspRfidCard.setCardNumber(rfid.getRfNum());
            emspRfidCard.setStatus(rfid.getStatus() == 1 ? "Active" : "InActive");
            emspRfidCard.setReason(rfid.getStatusReason());
            emspRfidCard.setRegistrationDate(oemServiceUtils.formatDate(rfid.getRegistrationDate()));
    
            result.succeed(emspRfidCard);
        } catch (IllegalArgumentException e) {
            result.fail(400, e.getMessage());
        } catch (Exception e) {
            result.fail(500, "서버 에러가 발생했습니다.");
        }

        return result;
    };

}
