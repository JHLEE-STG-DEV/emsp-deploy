package com.chargev.emsp.service.oem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chargev.emsp.entity.oem.EmspAccountEntity;
import com.chargev.emsp.entity.oem.EmspContractEntity;
import com.chargev.emsp.model.dto.oem.EmspAccount;
import com.chargev.emsp.model.dto.oem.EmspAccountModify;
import com.chargev.emsp.model.dto.oem.EmspContract;
import com.chargev.emsp.model.dto.oem.OemAccount;
import com.chargev.emsp.model.dto.oem.OemAccountAddress;
import com.chargev.emsp.repository.oem.EmspAccountRepository;
import com.chargev.emsp.repository.oem.EmspContractRepository;
import com.chargev.emsp.repository.oem.EmspRfidRepository;
import com.chargev.emsp.model.dto.oem.EmspStatus;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.oem.OemServiceUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final EmspAccountRepository emspAccountRepository;
    private final EmspContractRepository emspContractRepository;
    private final EmspRfidRepository emspRfidRepository;
    private final ContractService contractService;
    private final OemServiceUtils oemServiceUtils;

    @Override
    public boolean isCiamIdExists(String ciamId) {
        return emspAccountRepository.existsByCiamId(ciamId);
    }

    @Override
    @Transactional
    public ServiceResult<EmspAccount> createAccount(OemAccount oemAccount) throws IllegalArgumentException {
        ServiceResult<EmspAccount> result = new ServiceResult<>();

        Optional<EmspAccountEntity> existingAccountOpt = emspAccountRepository.findByCiamId(oemAccount.getCiamId());
        if (existingAccountOpt.isPresent() && existingAccountOpt.get().getAccountStatus() == 1) {
            throw new IllegalArgumentException("이미 가입된 ME 회원입니다.");
        }

        OemAccountAddress address = oemAccount.getAddress();
        EmspAccountEntity account = new EmspAccountEntity();

        // EMSP_ACCOUNT_KEY를 GUID로 생성
        account.setEmspAccountKey(UUID.randomUUID().toString().replace("-", ""));
        account.setAccountStatus(1); // 정상 상태로 설정

        account.setCiamId(oemAccount.getCiamId());
        account.setName(oemAccount.getName());
        account.setEmail(oemAccount.getEmail());
        account.setMobileNumber(oemAccount.getMobileNumber());
        account.setZipCode(address.getZipCode());
        account.setStreet(address.getStreet());
        account.setHouseNumber(address.getHouseNumber());
        account.setCity(address.getCity());
        account.setCountry(address.getCountry());

        account = emspAccountRepository.save(account);

        // Create the EmspAccount DTO for response
        EmspAccount emspAccount = new EmspAccount();
        OemAccountAddress emspAddress = new OemAccountAddress();

        emspAccount.setEmspAccountKey(account.getEmspAccountKey());
        emspAccount.setAccountStatus(EmspStatus.ACTIVE);
        emspAccount.setName(account.getName());
        emspAccount.setEmail(account.getEmail());
        emspAccount.setMobileNumber(account.getMobileNumber());

        emspAddress.setZipCode(account.getZipCode());
        emspAddress.setStreet(account.getStreet());
        emspAddress.setHouseNumber(account.getHouseNumber());
        emspAddress.setCity(account.getCity());
        emspAddress.setCountry(account.getCountry());
        emspAccount.setAddress(emspAddress);

        result.succeed(emspAccount);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<EmspAccount> getAccountById(String emspAccountKey) {
        ServiceResult<EmspAccount> result = new ServiceResult<>();
        // account와 함께 status가 1인 경우만 조회
        Optional<EmspAccountEntity> accountOpt = emspAccountRepository.findByEmspAccountKeyAndAccountStatus(emspAccountKey, 1);
        if (!accountOpt.isPresent()) {
            result.fail(404, "계정을 찾을 수 없습니다.");
            return result;
        }

        EmspAccountEntity account = accountOpt.get();
        EmspAccount emspAccount = new EmspAccount();
        emspAccount.setEmspAccountKey(account.getEmspAccountKey());
        emspAccount.setAccountStatus(EmspStatus.ACTIVE);
        emspAccount.setName(account.getName());
        emspAccount.setEmail(account.getEmail());
        emspAccount.setMobileNumber(account.getMobileNumber());

        OemAccountAddress emspAddress = new OemAccountAddress();
        emspAddress.setZipCode(account.getZipCode());
        emspAddress.setStreet(account.getStreet());
        emspAddress.setHouseNumber(account.getHouseNumber());
        emspAddress.setCity(account.getCity());
        emspAddress.setCountry(account.getCountry());
        emspAccount.setAddress(emspAddress);

        // 계약 가져오기
        ServiceResult<List<EmspContract>> contractResult = contractService.getContractsByAccountKey(emspAccountKey);
        emspAccount.setContracts(contractResult.get());

        result.succeed(emspAccount);
        return result;
    }


    @Override
    @Transactional(readOnly = true)
    public ServiceResult<EmspAccount> modifyAccountById(String emspAccountKey, EmspAccountModify newInfo) {
        ServiceResult<EmspAccount> result = new ServiceResult<>();
        Optional<EmspAccountEntity> accountOpt = emspAccountRepository.findById(emspAccountKey);
        if (!accountOpt.isPresent()) {
            result.fail(404, "계정을 찾을 수 없습니다.");
            return result;
        }

        // 유효성 검증 및 업데이트 로직
        EmspAccountEntity existingAccount = accountOpt.get();
        if (existingAccount == null) {
            result.fail(404, "계정을 찾을 수 없습니다.");
            return result;
        }

        if (newInfo.getName() != null && !newInfo.getName().isEmpty()) {
            existingAccount.setName(newInfo.getName());
        }
        if (newInfo.getMobileNumber() != null && !newInfo.getMobileNumber().isEmpty()) {
            existingAccount.setMobileNumber(newInfo.getMobileNumber());
        }
        if (newInfo.getEmail() != null && !newInfo.getEmail().isEmpty()) {
            existingAccount.setEmail(newInfo.getEmail());
        }
        if (newInfo.getAddress() != null) {
            OemAccountAddress address = newInfo.getAddress();
            if (address.getZipCode() != null && !address.getZipCode().isEmpty()) {
                existingAccount.setZipCode(address.getZipCode());
            }
            if (address.getCity() != null && !address.getCity().isEmpty()) {
                existingAccount.setCity(address.getCity());
            }
            if (address.getCountry() != null && !address.getCountry().isEmpty()) {
                existingAccount.setCountry(address.getCountry());
            }
            if (address.getHouseNumber() != null && !address.getHouseNumber().isEmpty()) {
                existingAccount.setHouseNumber(address.getHouseNumber());
            }
            if (address.getStreet() != null && !address.getStreet().isEmpty()) {
                existingAccount.setStreet(address.getStreet());
            }
        }

        EmspAccountEntity updatedAccount = emspAccountRepository.save(existingAccount);

        EmspAccount emspAccount = new EmspAccount();
        emspAccount.setEmspAccountKey(updatedAccount.getEmspAccountKey());
        emspAccount.setAccountStatus(oemServiceUtils.getEmspStatusEnumFromInt(updatedAccount.getAccountStatus()));
        emspAccount.setName(updatedAccount.getName());
        emspAccount.setEmail(updatedAccount.getEmail());
        emspAccount.setMobileNumber(updatedAccount.getMobileNumber());

        OemAccountAddress emspAddress = new OemAccountAddress();
        emspAddress.setZipCode(updatedAccount.getZipCode());
        emspAddress.setStreet(updatedAccount.getStreet());
        emspAddress.setHouseNumber(updatedAccount.getHouseNumber());
        emspAddress.setCity(updatedAccount.getCity());
        emspAddress.setCountry(updatedAccount.getCountry());
        emspAccount.setAddress(emspAddress);

        // 계약 가져오기
        ServiceResult<List<EmspContract>> contractResult = contractService.getContractsByAccountKey(updatedAccount.getEmspAccountKey());
        emspAccount.setContracts(contractResult.get());

        result.succeed(emspAccount);
        return result;
    }

    @Override
    @Transactional
    public ServiceResult<String> deleteAccountById(String emspAccountKey) {
        ServiceResult<String> result = new ServiceResult<>();
        Optional<EmspAccountEntity> accountOpt = emspAccountRepository.findById(emspAccountKey);
        if (!accountOpt.isPresent()) {
            result.fail(404, "계정을 찾을 수 없습니다.");
            return result;
        }

        EmspAccountEntity existingAccount = accountOpt.get();

        // 1. 유효한 계약이 남아있다면 거절

        List<EmspContractEntity> contracts = emspContractRepository.findByAccountAndContractStatus(existingAccount, 1);
        if (!contracts.isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("유효한 계약이 남아있어 회원 탈퇴가 불가능합니다");
            return result;
        }

        // 2. 유효한 계약이 없다면 Locked? Terminated?
        // 우선 Locked로 둔다. (뒷단의 Termination 로직 다시 확인 필요)

        existingAccount.setAccountStatus(2);
        existingAccount.setAccountStatusReason("IN_TERMINATION");
        emspAccountRepository.save(existingAccount);
        result.succeed("OK");
        return result;

        // TODO : 실제로는 이대로 반환할게 아니라, Termination에 필요한 프로세스가 돌아갸아 한다.
    }

}
