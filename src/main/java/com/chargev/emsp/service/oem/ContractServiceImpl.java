package com.chargev.emsp.service.oem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chargev.emsp.entity.oem.Account;
import com.chargev.emsp.entity.oem.OemContract;
import com.chargev.emsp.entity.oem.RFID;
import com.chargev.emsp.model.dto.oem.EmspContract;
import com.chargev.emsp.model.dto.oem.EmspContractRequest;
import com.chargev.emsp.model.dto.oem.EmspRfidCard;
import com.chargev.emsp.model.dto.oem.EmspServicePackage;
import com.chargev.emsp.model.dto.oem.OemPaymentInfo;
import com.chargev.emsp.model.dto.oem.OemVehicle;
import com.chargev.emsp.repository.oem.AccountRepository;
import com.chargev.emsp.repository.oem.OemContractRepository;
import com.chargev.emsp.repository.oem.RFIDRepository;
import com.chargev.emsp.service.ServiceResult;


@Service
public class ContractServiceImpl implements ContractService {
    private final OemContractRepository contractRepository;
    private final AccountRepository accountRepository;
    private final RFIDRepository rfidRepository;

    public ContractServiceImpl(OemContractRepository contractRepository, AccountRepository accountRepository, RFIDRepository rfidRepository) {
        this.contractRepository = contractRepository;
        this.accountRepository = accountRepository;
        this.rfidRepository = rfidRepository;
    }

    @Override
    @Transactional
    public ServiceResult<EmspContract> createContract(String emspAccountKey, EmspContractRequest contractRequest) throws IllegalArgumentException {
        ServiceResult<EmspContract> result = new ServiceResult<>();

        try {
            Optional<Account> accountOpt = accountRepository.findByEmspAccountKeyAndAccountStatus(emspAccountKey, 1);
            if (!accountOpt.isPresent()) {
                throw new IllegalArgumentException("계정을 찾을 수 없습니다.");
            }

            Account account = accountOpt.get();

            OemVehicle vehicle = contractRequest.getVehicle();
            EmspServicePackage servicePackage = contractRequest.getServicePackage();
            OemPaymentInfo paymentInfo = contractRequest.getPaymentInfo();

            // 중복된 VIN이 있는지 살펴본다.
            Optional<OemContract> existingContract = contractRepository.findByVinAndContractStatus(vehicle.getVin(), 1);
            if (existingContract.isPresent()) {
                OemContract contractToTerminate = existingContract.get();
                contractToTerminate.setContractStatus(0);
                contractRepository.save(contractToTerminate);
            }

            // 새로운 계약을 생성한다.
            OemContract contract = new OemContract();
            contract.setContractId(UUID.randomUUID().toString().replace("-", ""));
            contract.setAccount(account);
            contract.setVin(vehicle.getVin());
            contract.setVehicleType(vehicle.getVehicleType());
            contract.setModeName(vehicle.getModelName());
            contract.setPackageId(servicePackage.getId());
            contract.setPackageName(servicePackage.getName());

            try {
                contract.setPackageExpirationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(servicePackage.getExpirationDate()));
                contract.setPaymentExpirationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(paymentInfo.getExpirationDate()));
            } catch (ParseException e) {
                throw new IllegalArgumentException("날짜 변환에 실패했습니다. 패키지/결제 만료일이 올바른 형식이 아닙니다.");
            }

            contract.setPaymentAssetId(paymentInfo.getAssetId());
            contract.setContractStatus(1);
            contract.setContractStartDate(new Date());

            contract = contractRepository.save(contract);

            // 사용 가능한 RFID를 찾아 매칭한다.
            Optional<RFID> unusedRfidOpt = rfidRepository.findFirstByStatus(0);
            if (!unusedRfidOpt.isPresent()) {
                throw new IllegalArgumentException("사용 가능한 RFID 카드가 없습니다.");
            }

            RFID rfid = unusedRfidOpt.get();
            rfid.setStatus(1); // 사용중으로 flag 변경
            rfid.setIssuedAt(new Date());  // contract와 rfid가 매칭되는 시점을 issued at으로 둔다. (임시)
            rfid.setOemContract(contract);
            rfid = rfidRepository.save(rfid);

            // 응답으로 돌려줄 EmspContract 객체를 만든다.
            EmspContract emspContract = new EmspContract();
            emspContract.setContractId(String.valueOf(contract.getContractId()));
            emspContract.setContractStatus(contract.getContractStatus() == 1 ? "Active" : "Inactive");
            emspContract.setContractStartDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getContractStartDate()));
            emspContract.setContractEndDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getPackageExpirationDate()));

            vehicle = new OemVehicle();
            vehicle.setVin(contract.getVin());
            vehicle.setVehicleType(contract.getVehicleType());
            vehicle.setModelName(contract.getModeName());
            emspContract.setVehicle(vehicle);

            servicePackage = new EmspServicePackage();
            servicePackage.setId(contract.getPackageId());
            servicePackage.setName(contract.getPackageName());
            servicePackage.setExpirationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getPackageExpirationDate()));
            emspContract.setServicePackage(servicePackage);

            paymentInfo = new OemPaymentInfo();
            paymentInfo.setAssetId(contract.getPaymentAssetId());
            paymentInfo.setExpirationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getPaymentExpirationDate()));
            emspContract.setPayment(paymentInfo);

            EmspRfidCard emspRfidCard = new EmspRfidCard();
            emspRfidCard.setCardId(rfid.getId());
            emspRfidCard.setCardNumber(rfid.getRfNum());
            emspRfidCard.setStatus(rfid.getStatus() == 1 ? "Active" : "Inactive"); 
            emspRfidCard.setIssuedAt(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rfid.getIssuedAt()));
            emspContract.setRfidCard(emspRfidCard);

            result.succeed(emspContract);
        } catch (IllegalArgumentException e) {
            result.fail(400, e.getMessage());
        } catch (Exception e) {
            result.fail(500, "알 수 없는 오류가 발생했습니다.");
        }

        return result;
    }
    @Override
    public ServiceResult<List<EmspContract>> getContractsByAccountKey(String emspAccountKey) {
        ServiceResult<List<EmspContract>> result = new ServiceResult<>();

        Optional<Account> accountOpt = accountRepository.findByEmspAccountKeyAndAccountStatus(emspAccountKey, 1);
        if (!accountOpt.isPresent()) {
            throw new IllegalArgumentException("계정을 찾을 수 없습니다.");
        }

        List<OemContract> contracts = contractRepository.findByAccountAndContractStatus(accountOpt.get(), 1);

        // 응답으로 돌려줄 List<EmspContract>를 만든다.
        List<EmspContract> emspContracts = new ArrayList<>();
        for(OemContract contract : contracts) {
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

            EmspServicePackage servicePackage = new EmspServicePackage();
            servicePackage.setId(contract.getPackageId());
            servicePackage.setName(contract.getPackageName());
            servicePackage.setExpirationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getPackageExpirationDate()));
            emspContract.setServicePackage(servicePackage);

            OemPaymentInfo paymentInfo = new OemPaymentInfo();
            paymentInfo.setAssetId(contract.getPaymentAssetId());
            paymentInfo.setExpirationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getPaymentExpirationDate()));
            emspContract.setPayment(paymentInfo);

            EmspRfidCard emspRfidCard = new EmspRfidCard();
            List<RFID> rfids = rfidRepository.findByOemContractContractIdAndStatus(contract.getContractId(), 1);
            if (!rfids.isEmpty()) {
                RFID rfid = rfids.get(0); // 상태가 1인 첫 번째 RFID 가져오기
                emspRfidCard.setCardId(rfid.getId());
                emspRfidCard.setCardNumber(rfid.getRfNum());
                emspRfidCard.setStatus(rfid.getStatus() == 1 ? "Active" : "Inactive");
                emspRfidCard.setIssuedAt(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rfid.getIssuedAt()));
                emspContract.setRfidCard(emspRfidCard);
            }
            emspContract.setRfidCard(emspRfidCard);

            emspContracts.add(emspContract);
        }

        result.succeed(emspContracts);
        return result;
    }

    @Override
    public ServiceResult<EmspContract> getContract(String emspAccountKey, String contractId) {
        ServiceResult<EmspContract> result = new ServiceResult<>();

        Optional<Account> accountOpt = accountRepository.findByEmspAccountKeyAndAccountStatus(emspAccountKey, 1);
        if (!accountOpt.isPresent()) {
            throw new IllegalArgumentException("계정을 찾을 수 없습니다.");
        }

        Optional<OemContract> contractOpt = contractRepository.findByContractIdAndContractStatus(contractId, 1);
        if (!contractOpt.isPresent()) {
            throw new IllegalArgumentException("계약을 찾을 수 없습니다.");
        }

        OemContract contract = contractOpt.get();

        // 응답으로 돌려줄 EmspContract 객체를 만든다.
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

        EmspServicePackage servicePackage = new EmspServicePackage();
        servicePackage.setId(contract.getPackageId());
        servicePackage.setName(contract.getPackageName());
        servicePackage.setExpirationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getPackageExpirationDate()));
        emspContract.setServicePackage(servicePackage);

        OemPaymentInfo paymentInfo = new OemPaymentInfo();
        paymentInfo.setAssetId(contract.getPaymentAssetId());
        paymentInfo.setExpirationDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(contract.getPaymentExpirationDate()));
        emspContract.setPayment(paymentInfo);

        EmspRfidCard emspRfidCard = new EmspRfidCard();
        List<RFID> rfids = rfidRepository.findByOemContractContractIdAndStatus(contract.getContractId(), 1);
        if (!rfids.isEmpty()) {
            RFID rfid = rfids.get(0); // 상태가 1인 첫 번째 RFID 가져오기
            emspRfidCard.setCardId(rfid.getId());
            emspRfidCard.setCardNumber(rfid.getRfNum());
            emspRfidCard.setStatus(rfid.getStatus() == 1 ? "Active" : "Inactive");
            emspRfidCard.setIssuedAt(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(rfid.getIssuedAt()));
            emspContract.setRfidCard(emspRfidCard);
        }
        emspContract.setRfidCard(emspRfidCard);

        result.succeed(emspContract);

        return result;
    }

}
