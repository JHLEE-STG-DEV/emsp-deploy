package com.chargev.emsp.service.oem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chargev.emsp.entity.oem.Account;
import com.chargev.emsp.entity.oem.OemContract;
import com.chargev.emsp.entity.oem.RFID;
import com.chargev.emsp.model.dto.oem.EmspAccount;
import com.chargev.emsp.model.dto.oem.EmspAccountModify;
import com.chargev.emsp.model.dto.oem.EmspContract;
import com.chargev.emsp.model.dto.oem.EmspRfidCard;
import com.chargev.emsp.model.dto.oem.EmspServicePackage;
import com.chargev.emsp.model.dto.oem.OemAccount;
import com.chargev.emsp.model.dto.oem.OemAccountAddress;
import com.chargev.emsp.model.dto.oem.OemPaymentInfo;
import com.chargev.emsp.model.dto.oem.OemVehicle;
import com.chargev.emsp.repository.oem.AccountRepository;
import com.chargev.emsp.repository.oem.OemContractRepository;
import com.chargev.emsp.repository.oem.RFIDRepository;
import com.chargev.emsp.service.ServiceResult;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final OemContractRepository contractRepository;
    private final RFIDRepository rfidRepository;

    public AccountServiceImpl(AccountRepository accountRepository, OemContractRepository contractRepository, RFIDRepository rfidRepository) {
        this.accountRepository = accountRepository;
        this.contractRepository = contractRepository;
        this.rfidRepository = rfidRepository;
    }

    @Override
    public boolean isCiamIdExists(String ciamId) {
        return accountRepository.existsByCiamId(ciamId);
    }

    @Override
    @Transactional
    public ServiceResult<EmspAccount> createAccount(OemAccount oemAccount) throws IllegalArgumentException {
        ServiceResult<EmspAccount> result = new ServiceResult<>();

        Optional<Account> existingAccountOpt = accountRepository.findByCiamId(oemAccount.getCiamId());
        if (existingAccountOpt.isPresent() && existingAccountOpt.get().getAccountStatus() == 1) {
            throw new IllegalArgumentException("이미 가입된 ME 회원입니다.");
        }

        OemAccountAddress address = oemAccount.getAddress();
        Account account = new Account();

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

        account = accountRepository.save(account);

        // Create the EmspAccount DTO for response
        EmspAccount emspAccount = new EmspAccount();
        OemAccountAddress emspAddress = new OemAccountAddress();

        emspAccount.setEmspAccountKey(account.getEmspAccountKey());
        emspAccount.setAccountStatus(account.getAccountStatus() == 1 ? "Active" : "Inactive");
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
        Optional<Account> accountOpt = accountRepository.findByEmspAccountKeyAndAccountStatus(emspAccountKey, 1);
        if (!accountOpt.isPresent()) {
            result.fail(404, "계정을 찾을 수 없습니다.");
            return result;
        }

        Account account = accountOpt.get();
        EmspAccount emspAccount = new EmspAccount();
        emspAccount.setEmspAccountKey(account.getEmspAccountKey());
        emspAccount.setAccountStatus(account.getAccountStatus() == 1 ? "Active" : "Inactive");
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

        // 계약 정보 조회
        List<OemContract> contracts = contractRepository.findByAccount(account);
        List<EmspContract> emspContracts = contracts.stream().map(contract -> {
            EmspContract emspContract = new EmspContract();
            emspContract.setContractId(String.valueOf(contract.getContractId()));
            emspContract.setContractStatus(contract.getContractStatus() == 1 ? "Active" : "Inactive");
            emspContract.setContractStartDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getContractStartDate()));
            emspContract.setContractEndDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getPackageExpirationDate()));

            OemVehicle vehicle = new OemVehicle();
            vehicle.setVin(contract.getVin());
            vehicle.setVehicleType(contract.getVehicleType());
            vehicle.setModelName(contract.getModeName());
            emspContract.setVehicle(vehicle);

            EmspServicePackage servicePackageDTO = new EmspServicePackage();
            servicePackageDTO.setId(contract.getPackageId());
            servicePackageDTO.setName(contract.getPackageName());
            servicePackageDTO.setExpirationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getPackageExpirationDate()));
            emspContract.setServicePackage(servicePackageDTO);

            OemPaymentInfo paymentInfo = new OemPaymentInfo();
            paymentInfo.setAssetId(contract.getPaymentAssetId());
            paymentInfo.setExpirationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getPaymentExpirationDate()));
            emspContract.setPayment(paymentInfo);

            // RFID 정보 조회
            List<RFID> rfids = rfidRepository.findByOemContractContractIdAndStatus(contract.getContractId(), 1);
            if (!rfids.isEmpty()) {
                RFID rfid = rfids.get(0); // 상태가 1인 첫 번째 RFID 가져오기
                EmspRfidCard emspRfidCard = new EmspRfidCard();
                emspRfidCard.setCardId(rfid.getId());
                emspRfidCard.setCardNumber(rfid.getRfNum());
                emspRfidCard.setStatus(rfid.getStatus() == 1 ? "Active" : "Inactive");
                emspRfidCard.setIssuedAt(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rfid.getIssuedAt()));
                emspContract.setRfidCard(emspRfidCard);
            }

            return emspContract;
        }).collect(Collectors.toList());

        emspAccount.setContracts(emspContracts);

        result.succeed(emspAccount);
        return result;
    }


    @Override
    @Transactional(readOnly = true)
    public ServiceResult<EmspAccount> modifyAccountById(String emspAccountKey, EmspAccountModify newInfo) {
        ServiceResult<EmspAccount> result = new ServiceResult<>();
        Optional<Account> accountOpt = accountRepository.findById(emspAccountKey);
        if (!accountOpt.isPresent()) {
            result.fail(404, "계정을 찾을 수 없습니다.");
            return result;
        }

        // 유효성 검증 및 업데이트 로직
        Account existingAccount = accountOpt.get();
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

        Account updatedAccount = accountRepository.save(existingAccount);

        EmspAccount emspAccount = new EmspAccount();
        emspAccount.setEmspAccountKey(updatedAccount.getEmspAccountKey());
        emspAccount.setAccountStatus(updatedAccount.getAccountStatus() == 1 ? "Active" : "Inactive");
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

        // 계약 정보 조회
        List<OemContract> contracts = contractRepository.findByAccount(updatedAccount);
        List<EmspContract> emspContracts = contracts.stream().map(contract -> {
            EmspContract emspContract = new EmspContract();
            emspContract.setContractId(String.valueOf(contract.getContractId()));
            emspContract.setContractStatus(contract.getContractStatus() == 1 ? "Active" : "Inactive");
            emspContract.setContractStartDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getContractStartDate()));
            emspContract.setContractEndDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getPackageExpirationDate()));

            OemVehicle vehicle = new OemVehicle();
            vehicle.setVin(contract.getVin());
            vehicle.setVehicleType(contract.getVehicleType());
            vehicle.setModelName(contract.getModeName());
            emspContract.setVehicle(vehicle);

            EmspServicePackage servicePackageDTO = new EmspServicePackage();
            servicePackageDTO.setId(contract.getPackageId());
            servicePackageDTO.setName(contract.getPackageName());
            servicePackageDTO.setExpirationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getPackageExpirationDate()));
            emspContract.setServicePackage(servicePackageDTO);

            OemPaymentInfo paymentInfo = new OemPaymentInfo();
            paymentInfo.setAssetId(contract.getPaymentAssetId());
            paymentInfo.setExpirationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getPaymentExpirationDate()));
            emspContract.setPayment(paymentInfo);

            // RFID 정보 조회
            List<RFID> rfids = rfidRepository.findByOemContractContractIdAndStatus(contract.getContractId(), 1);
            if (!rfids.isEmpty()) {
                RFID rfid = rfids.get(0); // 상태가 1인 첫 번째 RFID 가져오기
                EmspRfidCard emspRfidCard = new EmspRfidCard();
                emspRfidCard.setCardId(rfid.getId());
                emspRfidCard.setCardNumber(rfid.getRfNum());
                emspRfidCard.setStatus(rfid.getStatus() == 1 ? "Active" : "Inactive");
                emspRfidCard.setIssuedAt(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rfid.getIssuedAt()));
                emspContract.setRfidCard(emspRfidCard);
            }

            return emspContract;
        }).collect(Collectors.toList());

        emspAccount.setContracts(emspContracts);

        result.succeed(emspAccount);
        return result;
    }

    @Override
    @Transactional
    public ServiceResult<Void> deleteAccountById(String emspAccountKey) {
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
            result.setErrorMessage("유효한 계약이 남아있어 회원 탈퇴가 불가능합니다");
            return result;
        }

        existingAccount.setAccountStatus(-1);
        accountRepository.save(existingAccount);
        result.setSuccess(true);
        return result;
    }

}
