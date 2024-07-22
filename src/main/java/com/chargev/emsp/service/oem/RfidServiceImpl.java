package com.chargev.emsp.service.oem;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.oem.Account;
import com.chargev.emsp.entity.oem.OemContract;
import com.chargev.emsp.entity.oem.RFID;
import com.chargev.emsp.repository.oem.AccountRepository;
import com.chargev.emsp.repository.oem.OemContractRepository;
import com.chargev.emsp.repository.oem.RFIDRepository;
import com.chargev.emsp.service.ServiceResult;

@Service
public class RfidServiceImpl implements RfidService {
    private final AccountRepository accountRepository;
    private final OemContractRepository contractRepository;
    private final RFIDRepository rfidRepository;

    public RfidServiceImpl(AccountRepository accountRepository, OemContractRepository contractRepository, RFIDRepository rfidRepository) {
        this.accountRepository = accountRepository;
        this.contractRepository = contractRepository;
        this.rfidRepository = rfidRepository;
    }

    @Override
    public ServiceResult<Void> deleteRfidById(String emspAccountKey, String emspContractId, String cardId) {
        ServiceResult<Void> result = new ServiceResult<>();
        Optional<Account> accountOpt = accountRepository.findById(emspAccountKey);
        if (!accountOpt.isPresent()) {
            result.fail(404, "계정을 찾을 수 없습니다.");
            return result;
        }

        Account existingAccount = accountOpt.get();

        List<OemContract> contracts = contractRepository.findByAccountAndContractStatus(existingAccount, 1);
        if (!contracts.isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("유효한 계약을 찾을 수 없습니다.");
            return result;
        }

        // contract에 rfid가 여러개 존재할 수 있다. (rfid 재발급 시 새로운 rfid가 발급되기 때문)
        // 이 중 status가 1인 rfid는 항상 하나여야 하지만, 현재는 로직 상의 에러가 존재할 수 있으니 List를 가져와서 0번째를 확인하는 것으로 하자.
        List<RFID> rfids = rfidRepository.findByOemContractContractIdAndStatus(emspContractId, 1);
        if (rfids.isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("유효한 RFID를 찾을 수 없습니다.");
            return result;
        }
        RFID rfid = rfids.get(0);

        rfid.setStatus(-1);
        rfidRepository.save(rfid);
        result.setSuccess(true);
        return result;
    }
}
