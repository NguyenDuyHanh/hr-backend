package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.BankDto;
import com.tlu.hrm.dto.search.SearchDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface BankService {
    Page<BankDto> pagingBanks(SearchDto searchDto);
    List<BankDto> getAllBanks();
    BankDto getById(UUID id);
    BankDto saveBank(BankDto dto);
    void deleteBank(UUID id);
    boolean isValidCode(BankDto dto);
}
