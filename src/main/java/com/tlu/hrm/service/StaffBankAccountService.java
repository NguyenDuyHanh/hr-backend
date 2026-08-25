package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.StaffBankAccountDto;

import java.util.List;
import java.util.UUID;

public interface StaffBankAccountService {
    List<StaffBankAccountDto> getBankAccountsByStaffId(UUID staffId);
    StaffBankAccountDto createBankAccount(StaffBankAccountDto dto);
    StaffBankAccountDto updateBankAccount(UUID id, StaffBankAccountDto dto);
    StaffBankAccountDto setDefaultAccount(UUID id);
    void deleteBankAccount(UUID id);
}
