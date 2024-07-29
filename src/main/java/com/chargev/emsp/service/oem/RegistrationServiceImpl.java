package com.chargev.emsp.service.oem;

import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chargev.emsp.entity.oem.EmspAccountEntity;
import com.chargev.emsp.model.dto.oem.EmspAccount;
import com.chargev.emsp.model.dto.oem.EmspAccountRegistration;
import com.chargev.emsp.model.dto.oem.EmspContract;
import com.chargev.emsp.model.dto.oem.EmspContractRequest;
import com.chargev.emsp.model.dto.oem.OemAccount;
import com.chargev.emsp.repository.oem.EmspAccountRepository;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.oem.AccountService;
import com.chargev.emsp.service.oem.ContractService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final EmspAccountRepository emspAccountRepository;
    private final AccountService accountService;
    private final ContractService contractService;

    @Override
    @Transactional
    public boolean registerAccount(EmspAccountRegistration registration, ServiceResult<EmspAccount> result) {
        boolean accountResult = false;
        try {
            OemAccount oemAccount = registration.getAccount();
            EmspContractRequest contractRequest = registration.getContractRequest();

            // Step 1: Check for ciam_id duplication
            Optional<EmspAccountEntity> existingAccountOpt = emspAccountRepository.findByCiamId(oemAccount.getCiamId());
            if (existingAccountOpt.isPresent() && existingAccountOpt.get().getAccountStatus() == 1) {
                throw new IllegalArgumentException("이미 가입된 ME 회원입니다.");
            }

            // Step 2: Create a new account
            ServiceResult<EmspAccount> emspAccountResult = accountService.createAccount(oemAccount);
            if(emspAccountResult.isFail()) {
                result.fail(400, emspAccountResult.getErrorMessage());
                return accountResult;
            }
            EmspAccount emspAccount = emspAccountResult.get();

            // Step 3: Create a new contract under the account
            ServiceResult<EmspContract> emspContractResult = contractService.createContract(emspAccount.getEmspAccountKey(), contractRequest);
            System.out.println("emspContractResult.isFail(): " + emspContractResult.isFail());
            System.out.println("emspContractResult.getErrorMessage(): " + emspContractResult.getErrorMessage());

            if(emspContractResult.isFail()) {
                System.out.println("Contract creation failed: " + emspContractResult.getErrorMessage());
                result.fail(400, emspContractResult.getErrorMessage());
                return accountResult;
            }
            EmspContract emspContract = emspContractResult.get();

            // Step 4: Add the contract to the account's contract list
            try {
                System.out.println("emspAccount: " + emspAccount.toString());
                System.out.println("emspContract: " + emspContract.toString());
                emspAccount.setContracts(Collections.singletonList(emspContract));
            } catch (Exception e) {
                System.out.println("Exception in setting contracts: " + e.toString());
                throw e; // rethrow to be caught by outer catch
            }
            result.succeed(emspAccount);
            accountResult = true;
        } catch (IllegalArgumentException e) {
            result.fail(400, e.getMessage());
            accountResult = false;
        } catch (Exception e) {
            result.fail(400, e.getMessage());
            accountResult = false;
        }
        return accountResult;
    }
}