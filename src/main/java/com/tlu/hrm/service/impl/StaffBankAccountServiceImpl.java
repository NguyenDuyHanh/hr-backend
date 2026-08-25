package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.StaffBankAccountDto;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.model.Bank;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.StaffBankAccount;
import com.tlu.hrm.repository.BankRepository;
import com.tlu.hrm.repository.StaffBankAccountRepository;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.service.StaffBankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class StaffBankAccountServiceImpl implements StaffBankAccountService {

    @Autowired
    private StaffBankAccountRepository staffBankAccountRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private BankRepository bankRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StaffBankAccountDto> getBankAccountsByStaffId(UUID staffId) {
        return staffBankAccountRepository.findByStaffIdAndIsDeletedFalse(staffId).stream()
                .map(StaffBankAccountDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public StaffBankAccountDto createBankAccount(StaffBankAccountDto dto) {
        Staff staff = staffRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));

        Bank bank = bankRepository.findById(dto.getBankId())
                .orElseThrow(() -> new ResourceNotFoundException("Ngân hàng không tồn tại"));

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            resetDefaultAccounts(staff.getId(), null);
        }

        StaffBankAccount account = new StaffBankAccount();
        account.setStaff(staff);
        account.setBank(bank);
        account.setAccountNumber(dto.getAccountNumber());
        account.setAccountName(dto.getAccountName());
        account.setBranchName(dto.getBranchName());
        account.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : true);
        account.setNote(dto.getNote());

        StaffBankAccount saved = staffBankAccountRepository.save(account);
        return new StaffBankAccountDto(saved);
    }

    @Override
    public StaffBankAccountDto updateBankAccount(UUID id, StaffBankAccountDto dto) {
        StaffBankAccount account = staffBankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản ngân hàng không tồn tại"));

        Bank bank = bankRepository.findById(dto.getBankId())
                .orElseThrow(() -> new ResourceNotFoundException("Ngân hàng không tồn tại"));

        if (Boolean.TRUE.equals(dto.getIsDefault()) && !Boolean.TRUE.equals(account.getIsDefault())) {
            resetDefaultAccounts(account.getStaff().getId(), id);
        }

        account.setBank(bank);
        account.setAccountNumber(dto.getAccountNumber());
        account.setAccountName(dto.getAccountName());
        account.setBranchName(dto.getBranchName());
        account.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        account.setNote(dto.getNote());

        StaffBankAccount saved = staffBankAccountRepository.save(account);
        return new StaffBankAccountDto(saved);
    }

    @Override
    public StaffBankAccountDto setDefaultAccount(UUID id) {
        StaffBankAccount account = staffBankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản ngân hàng không tồn tại"));

        List<StaffBankAccount> existing = staffBankAccountRepository.findByStaffIdAndIsDeletedFalse(account.getStaff().getId());
        for (StaffBankAccount acc : existing) {
            acc.setIsDefault(acc.getId().equals(id));
            staffBankAccountRepository.save(acc);
        }

        return new StaffBankAccountDto(account);
    }

    @Override
    public void deleteBankAccount(UUID id) {
        StaffBankAccount account = staffBankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản ngân hàng không tồn tại"));

        account.setIsDeleted(true);
        staffBankAccountRepository.save(account);
    }

    private void resetDefaultAccounts(UUID staffId, UUID excludeId) {
        List<StaffBankAccount> existing = staffBankAccountRepository.findByStaffIdAndIsDeletedFalse(staffId);
        for (StaffBankAccount acc : existing) {
            if (excludeId == null || !acc.getId().equals(excludeId)) {
                acc.setIsDefault(false);
                staffBankAccountRepository.save(acc);
            }
        }
    }
}
