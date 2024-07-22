package com.chargev.emsp.service.oem;

import java.sql.SQLException;

import com.chargev.emsp.model.dto.oem.EmspAccount;
import com.chargev.emsp.model.dto.oem.EmspAccountRegistration;
import com.chargev.emsp.service.ServiceResult;

public interface RegistrationService {
    boolean registerAccount(EmspAccountRegistration registration, ServiceResult<EmspAccount> result);
}
