package com.chargev.emsp.service.oem;
import com.chargev.emsp.model.dto.oem.EmspAccount;
import com.chargev.emsp.model.dto.oem.EmspAccountModify;
import com.chargev.emsp.model.dto.oem.OemAccount;
import com.chargev.emsp.service.ServiceResult;

public interface AccountService {
    boolean isCiamIdExists(String ciamId);
    ServiceResult<EmspAccount> createAccount(OemAccount oemAccount);
    ServiceResult<EmspAccount> getAccountById(String emspAccountKey);
    ServiceResult<EmspAccount> modifyAccountById(String emspAccountKey, EmspAccountModify updatedAccount);
    ServiceResult<String> deleteAccountById(String emspAccountKey);
}
