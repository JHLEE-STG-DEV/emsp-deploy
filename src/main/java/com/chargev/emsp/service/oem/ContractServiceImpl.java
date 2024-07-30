package com.chargev.emsp.service.oem;


import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;

import com.chargev.emsp.entity.oem.EmspAccountEntity;
import com.chargev.emsp.entity.oem.EmspContractEntity;
import com.chargev.emsp.entity.oem.EmspKeyBase;
import com.chargev.emsp.model.dto.oem.EmspContract;
import com.chargev.emsp.model.dto.oem.EmspContractRequest;
import com.chargev.emsp.model.dto.oem.EmspDriverGroupInfo;
import com.chargev.emsp.model.dto.oem.EmspRfidCard;
import com.chargev.emsp.model.dto.oem.EmspServicePackage;
import com.chargev.emsp.model.dto.oem.EmspStatus;
import com.chargev.emsp.model.dto.oem.OemPaymentInfo;
import com.chargev.emsp.model.dto.oem.OemVehicle;
import com.chargev.emsp.repository.oem.EmspContractRepository;
import com.chargev.emsp.repository.oem.EmspKeyBaseRepository;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.model.dto.oem.EmspContractStatusReason;
import com.chargev.emsp.model.dto.oem.EmspRfidStatusReason;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final EmspContractRepository emspContractRepository;
    private final EmspKeyBaseRepository emspKeyBaseRepository;
    private final OemServiceUtils oemServiceUtils;
    

    //#region Create Contract
    @Override
    @Transactional
    public ServiceResult<EmspContract> createContract(String emspAccountKey, EmspContractRequest contractRequest) {
        ServiceResult<EmspContract> result = new ServiceResult<>();

        try {
            EmspAccountEntity account = oemServiceUtils.findAccountByKeyAndAccountStatus(emspAccountKey, 1);

            OemVehicle vehicle = contractRequest.getVehicle();
            EmspServicePackage servicePackage = contractRequest.getServicePackage();
            OemPaymentInfo paymentInfo = contractRequest.getPaymentInfo();

            handleExistingContract(vehicle.getVin());

            String contractId = generateContractId(emspAccountKey);

            EmspContractEntity contract = createNewContract(contractId, account, vehicle, servicePackage, paymentInfo);
            contract = emspContractRepository.save(contract);

            oemServiceUtils.createHistoryRecord(contract, "CREATED");

            EmspContract emspContract = createEmspContractResponse(contract);

            result.succeed(emspContract);
        } catch(IllegalArgumentException | ParseException e) {
            result.fail(400, e.getMessage());
        } catch (UnexpectedRollbackException e) {
            result.fail(500, "계약 생성 과정에서 서버 오류가 발생했습니다.");
        }

        return result;
    }
    //#endregion

    //#region Modify Contract
    @Override
    @Transactional
    public ServiceResult<EmspContract> modifyContract(String emspAccountKey, String contractId, EmspContract contractRequest) {
        ServiceResult<EmspContract> result = new ServiceResult<>();

        try {

            EmspAccountEntity account = oemServiceUtils.findAccountByKeyAndAccountStatus(emspAccountKey, 1);

            EmspContractEntity contract = oemServiceUtils.findContractByAccountAndIdAndStatus(account, contractId, 1, 2);

            handleContractStatusChange(contract, contractRequest);
            handlePaymentChange(contract, contractRequest);
            handlePackageChange(contract, contractRequest);

            contract = emspContractRepository.save(contract);

            EmspContract emspContract = createEmspContractResponse(contract);

            result.succeed(emspContract);

        } catch (IllegalArgumentException e) {
            result.fail(400, e.getMessage());
        } catch (Exception e) {
            result.fail(500, "알 수 없는 오류가 발생했습니다.");
        }

        return result;
    }
    //#endregion

    //#region Terminate Contract
    @Override
    @Transactional
    public ServiceResult<String> terminateContract(String emspAccountKey, String contractId) {
        ServiceResult<String> result = new ServiceResult<>();

        try {
            EmspAccountEntity account = oemServiceUtils.findAccountByKeyAndAccountStatus(emspAccountKey, 1);

            EmspContractEntity contract = oemServiceUtils.findContractByAccountAndIdAndStatus(account, contractId, 1, 2);

            oemServiceUtils.lockContract(contractId, "IN_TERMINATE");
            if(contract.getRfidStatus() == 1) {
                oemServiceUtils.lockRfid(contractId, "CONTRACT_LOCKED");
            }
            oemServiceUtils.createHistoryRecord(contract, "IN_TERMINATE");

            // result.setSuccess(true);
            result.succeed("OK");

            // TODO : LOCK 성공했으면 TERMINATE 프로세스에 넣어야 함.
        } catch (IllegalArgumentException e) {
            result.fail(400, e.getMessage());
        } catch (Exception e) {
            result.fail(500, "알 수 없는 오류가 발생했습니다.");
        }

        return result;
    }
    //#endregion

    //#region Get Contracts
    @Override
    public ServiceResult<List<EmspContract>> getContractsByAccountKey(String emspAccountKey) {
        ServiceResult<List<EmspContract>> result = new ServiceResult<>();

        try {
            EmspAccountEntity account = oemServiceUtils.findAccountByKeyAndAccountStatus(emspAccountKey, 1);
            List<EmspContractEntity> contracts = emspContractRepository.findByAccountAndContractStatusIn(account, Arrays.asList(1, 2));

            List<EmspContract> emspContracts = contracts.stream()
                    .map(this::createEmspContractResponse)
                    .collect(Collectors.toList());

            result.succeed(emspContracts);
        } catch (IllegalArgumentException e) {
            result.fail(400, e.getMessage());
        } catch (Exception e) {
            result.fail(500, "알 수 없는 오류가 발생했습니다.");
        }

        return result;
    }
    //#endregion

    //#region Get Contract
    @Override
    public ServiceResult<EmspContract> getContract(String emspAccountKey, String contractId) {
        ServiceResult<EmspContract> result = new ServiceResult<>();

        try {
            EmspAccountEntity account = oemServiceUtils.findAccountByKeyAndAccountStatus(emspAccountKey, 1);

            EmspContractEntity contract = oemServiceUtils.findContractByAccountAndIdAndStatus(account, contractId, 1, 2);

            EmspContract emspContract = createEmspContractResponse(contract);

            result.succeed(emspContract);
        } catch (IllegalArgumentException e) {
            result.fail(400, e.getMessage());
        } catch (Exception e) {
            result.fail(500, "알 수 없는 오류가 발생했습니다.");
        }

        return result;
    }
    //#endregion

    //#region Get Driver Group
    @Override
    public ServiceResult<EmspDriverGroupInfo> getDriverGroup(String emspAccountKey, String contractId) {
        ServiceResult<EmspDriverGroupInfo> result = new ServiceResult<>();

        try {
            EmspAccountEntity account = oemServiceUtils.findAccountByKeyAndAccountStatus(emspAccountKey, 1);

            EmspContractEntity contract = oemServiceUtils.findContractByAccountAndIdAndStatus(account, contractId, 1);

            // TODO : 실제로는 Driver Group 매칭 정보를 가져와야 한다.
            // 여기에서는 우선 contract의 package Id를 보고 적당히 반환한다.
            String id = contract.getPackageId().equals("TEST_PACKAGE_ID_1") ? "1" : "2";
            String description = contract.getPackageName();

            EmspDriverGroupInfo driverGroupInfo = new EmspDriverGroupInfo();
            driverGroupInfo.setId(id);
            driverGroupInfo.setDescription(description);

            result.succeed(driverGroupInfo);
        } catch (IllegalArgumentException e) {
            result.fail(400, e.getMessage());
        } catch (Exception e) {
            result.fail(500, "알 수 없는 오류가 발생했습니다.");
        }

        return result;
    }
    //#endregion

    //#region Utils - handlers
    private void handleExistingContract(String vin) {
        Optional<EmspContractEntity> existingContract = emspContractRepository.findByVinAndContractStatus(vin, 1);
        if (existingContract.isPresent()) {
            oemServiceUtils.lockContract(existingContract.get().getContractId(), "IN_TERMINATE");
            if(existingContract.get().getRfidStatus() == 1) {
                oemServiceUtils.lockRfid(existingContract.get().getContractId(), "CONTRACT_LOCKED");
            }
            oemServiceUtils.createHistoryRecord(existingContract.get(), "IN_TERMINATE");
        }
    }

    private void handleContractStatusChange(EmspContractEntity contract, EmspContract contractRequest) {
        boolean isNowActivated = contract.getContractStatus() == 1;
        boolean isNowLocked = contract.getContractStatus() == 2;
        boolean isRequestActivating = contractRequest.getContractStatus().equals(EmspStatus.ACTIVE.toString());
        boolean isRequestLocking = contractRequest.getContractStatus().equals(EmspStatus.LOCKED.toString());

        if (isNowActivated && isRequestLocking) {
            oemServiceUtils.lockContract(contract.getContractId(),EmspContractStatusReason.INACTIVE.toString());
            if(contract.getRfidStatus() == 1) {
                oemServiceUtils.lockRfid(contract.getContractId(),EmspRfidStatusReason.CONTRACT_LOCKED.toString());
            }
            oemServiceUtils.createHistoryRecord(contract, EmspContractStatusReason.INACTIVE.toString());
        } else if (isNowLocked && isRequestActivating) {
            oemServiceUtils.unlockContract(contract.getContractId(), null);
            if(contract.getRfidStatus() == 2) {
                oemServiceUtils.unlockRfid(contract.getContractId(), null);
            }
            oemServiceUtils.createHistoryRecord(contract, "USER_ACTIVE");
        } else if (!isNowActivated && !isNowLocked) {
            throw new IllegalArgumentException("현재 계약 상태를 변경할 수 없는 계약입니다.");
        } else if (!isRequestActivating && !isRequestLocking) {
            throw new IllegalArgumentException("잘못된 계약 상태 변경 요청입니다.");
        }
    }

    private void handlePaymentChange(EmspContractEntity contract, EmspContract contractRequest) {
        if (!contract.getPaymentAssetId().equals(contractRequest.getPayment().getAssetId())) {
            boolean isAssetIdValid = validateAssetId(contractRequest.getPayment().getAssetId());
            if (isAssetIdValid) {
                try {
                    contract.setPaymentAssetId(contractRequest.getPayment().getAssetId());
                    contract.setPaymentExpirationDate(oemServiceUtils.parseDate(contractRequest.getPayment().getExpirationDate()));
                    oemServiceUtils.createHistoryRecord(contract, "ASSET_UPDATED");
                } catch (ParseException e) {
                    throw new IllegalArgumentException("잘못된 Payment Expiration Date 형식입니다.");
                }
            } else {
                throw new IllegalArgumentException("Payment Asset 검증에 실패했습니다.");
            }
        }
    }

    private void handlePackageChange(EmspContractEntity contract, EmspContract contractRequest) {
        if (!contract.getPackageId().equals(contractRequest.getServicePackage().getId())) {
            try {
                contract.setPackageId(contractRequest.getServicePackage().getId());
                contract.setPackageName(contractRequest.getServicePackage().getName());
                contract.setPackageExpirationDate(oemServiceUtils.parseDate(contractRequest.getServicePackage().getExpirationDate()));
                oemServiceUtils.createHistoryRecord(contract, "PACKAGE_UPDATED");
            } catch (ParseException e) {
                throw new IllegalArgumentException("잘못된 Package Expiration Date 형식입니다.");
            }
        }
    }
    //#endregion

    //#region Utils - etc...
    @Transactional
    public String generateContractId(String emspAccountKey) {
        String CountryCode = "KR";
        String ProviderId = "CEV";
        String OemCode = "2"; // BMW면 1, MB는 2인데 알 방법이 없음. 우선 MB로 고정함
        String SystemCode = "1"; // PnC면 1, eMSP면 2

        EmspKeyBase keyBase = new EmspKeyBase();
        keyBase.setEmspAccountKey(emspAccountKey);

        keyBase = emspKeyBaseRepository.save(keyBase);
        Integer keyBaseId = keyBase.getId();

        // 7자리 16진수로 변환
        String sequenceHex = String.format("%07X", keyBaseId); 

        keyBase.setSequenceHex(sequenceHex);
        emspKeyBaseRepository.save(keyBase);

        return CountryCode + ProviderId + OemCode + SystemCode + sequenceHex;
    }

    private boolean validateAssetId(String assetId) {
        // TODO: Implement actual validation logic with MPAY
        return true;
    }
    //#endregion

    //#region Utils - sets to repo
    private EmspContractEntity createNewContract(String contractId, EmspAccountEntity account, OemVehicle vehicle, EmspServicePackage servicePackage, OemPaymentInfo paymentInfo) throws ParseException {
        EmspContractEntity contract = new EmspContractEntity();
        contract.setContractId(contractId);
        contract.setAccount(account);
        contract.setVin(vehicle.getVin());
        contract.setVehicleType(vehicle.getVehicleType());
        contract.setModeName(vehicle.getModelName());
        contract.setPackageId(servicePackage.getId());
        contract.setPackageName(servicePackage.getName());
        contract.setPackageExpirationDate(oemServiceUtils.parseDate(servicePackage.getExpirationDate()));
        contract.setPaymentExpirationDate(oemServiceUtils.parseDate(paymentInfo.getExpirationDate()));
        contract.setPaymentAssetId(paymentInfo.getAssetId());
        contract.setContractStatus(1);
        contract.setContractStartDate(new Date());
        return contract;
    }

    private EmspContract createEmspContractResponse(EmspContractEntity contract) {
        EmspContract emspContract = new EmspContract();
        emspContract.setContractId(String.valueOf(contract.getContractId()));
        emspContract.setContractStatus(oemServiceUtils.getEmspStatusEnumFromInt(contract.getContractStatus()));
        emspContract.setContractStartDate(oemServiceUtils.formatDate(contract.getContractStartDate()));
        emspContract.setContractEndDate(oemServiceUtils.formatDate(contract.getPackageExpirationDate()));

        OemVehicle vehicle = new OemVehicle();
        vehicle.setVin(contract.getVin());
        vehicle.setVehicleType(contract.getVehicleType());
        vehicle.setModelName(contract.getModeName());
        emspContract.setVehicle(vehicle);

        EmspServicePackage servicePackage = new EmspServicePackage();
        servicePackage.setId(contract.getPackageId());
        servicePackage.setName(contract.getPackageName());
        servicePackage.setExpirationDate(oemServiceUtils.formatDate(contract.getPackageExpirationDate()));
        emspContract.setServicePackage(servicePackage);

        OemPaymentInfo paymentInfo = new OemPaymentInfo();
        paymentInfo.setAssetId(contract.getPaymentAssetId());
        paymentInfo.setExpirationDate(oemServiceUtils.formatDate(contract.getPaymentExpirationDate()));
        emspContract.setPayment(paymentInfo);

        EmspRfidCard emspRfidCard = new EmspRfidCard();
        Optional.ofNullable(contract.getRfidStatus())
                .filter(status -> status == 1 || status == 2)
                .ifPresent(status -> {
                    emspRfidCard.setCardId(contract.getRfidId());
                    emspRfidCard.setCardNumber(contract.getRfidNum());
                    emspRfidCard.setStatus(oemServiceUtils.getEmspStatusEnumFromInt(status));
                    emspRfidCard.setRegistrationDate(oemServiceUtils.formatDate(contract.getRfidRegistrationDate()));
                    emspContract.setRfidCard(emspRfidCard);
                });

        return emspContract;
    }
    //#endregion

}
