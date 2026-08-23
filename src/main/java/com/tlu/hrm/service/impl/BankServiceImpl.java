package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.BankDto;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.model.Bank;
import com.tlu.hrm.repository.BankRepository;
import com.tlu.hrm.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BankServiceImpl implements BankService {

    @Autowired
    private BankRepository bankRepository;

    @Override
    public Page<BankDto> pagingBanks(SearchDto searchDto) {
        int pageIndex = searchDto != null && searchDto.getPageIndex() > 0 ? searchDto.getPageIndex() - 1 : 0;
        int pageSize = searchDto != null && searchDto.getPageSize() > 0 ? searchDto.getPageSize() : 10;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        String keyword = searchDto != null && searchDto.getKeyword() != null ? searchDto.getKeyword().trim() : "";
        Page<Bank> pageResult;
        if (!keyword.isEmpty()) {
            pageResult = bankRepository.findByIsDeletedFalseAndNameContainingIgnoreCaseOrIsDeletedFalseAndCodeContainingIgnoreCaseOrIsDeletedFalseAndShortNameContainingIgnoreCase(keyword, keyword, keyword, pageable);
        } else {
            pageResult = bankRepository.findAll(pageable);
        }
        return pageResult.map(BankDto::new);
    }

    @Override
    public List<BankDto> getAllBanks() {
        return bankRepository.findByIsDeletedFalseOrderByNameAsc()
                .stream().map(BankDto::new).collect(Collectors.toList());
    }

    @Override
    public BankDto getById(UUID id) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Ngân hàng với ID: " + id));
        return new BankDto(bank);
    }

    @Override
    public BankDto saveBank(BankDto dto) {
        Bank bank;
        if (dto.getId() != null) {
            bank = bankRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Ngân hàng với ID: " + dto.getId()));
        } else {
            bank = new Bank();
        }
        bank.setCode(dto.getCode());
        bank.setName(dto.getName());
        bank.setShortName(dto.getShortName());
        bank.setBin(dto.getBin());
        bank.setLogo(dto.getLogo());
        bank.setSwiftCode(dto.getSwiftCode());
        bank.setTransferSupported(dto.getTransferSupported() != null ? dto.getTransferSupported() : true);
        bank.setLookupSupported(dto.getLookupSupported() != null ? dto.getLookupSupported() : true);
        bank.setIsDeleted(false);
        Bank saved = bankRepository.save(bank);
        return new BankDto(saved);
    }

    @Override
    public void deleteBank(UUID id) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Ngân hàng với ID: " + id));
        bank.setIsDeleted(true);
        bankRepository.save(bank);
    }

    @Override
    public boolean isValidCode(BankDto dto) {
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            return false;
        }
        Optional<Bank> existing = bankRepository.findByCode(dto.getCode().trim());
        if (existing.isPresent()) {
            return dto.getId() != null && dto.getId().equals(existing.get().getId());
        }
        return true;
    }
}
