package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.EthnicDto;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.model.Ethnic;
import com.tlu.hrm.repository.EthnicRepository;
import com.tlu.hrm.service.EthnicService;
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
public class EthnicServiceImpl implements EthnicService {

    @Autowired
    private EthnicRepository ethnicRepository;

    @Override
    public Page<EthnicDto> pagingEthnics(SearchDto searchDto) {
        int pageIndex = searchDto != null && searchDto.getPageIndex() > 0 ? searchDto.getPageIndex() - 1 : 0;
        int pageSize = searchDto != null && searchDto.getPageSize() > 0 ? searchDto.getPageSize() : 10;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        String keyword = searchDto != null && searchDto.getKeyword() != null ? searchDto.getKeyword().trim() : "";
        Page<Ethnic> pageResult;
        if (!keyword.isEmpty()) {
            pageResult = ethnicRepository.findByIsDeletedFalseAndNameContainingIgnoreCaseOrIsDeletedFalseAndCodeContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            pageResult = ethnicRepository.findAll(pageable);
        }
        return pageResult.map(EthnicDto::new);
    }

    @Override
    public List<EthnicDto> getAllEthnics() {
        return ethnicRepository.findByIsDeletedFalseOrderByNameAsc()
                .stream().map(EthnicDto::new).collect(Collectors.toList());
    }

    @Override
    public EthnicDto getById(UUID id) {
        Ethnic ethnic = ethnicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Dân tộc với ID: " + id));
        return new EthnicDto(ethnic);
    }

    @Override
    public EthnicDto saveEthnic(EthnicDto dto) {
        Ethnic ethnic;
        if (dto.getId() != null) {
            ethnic = ethnicRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Dân tộc với ID: " + dto.getId()));
        } else {
            ethnic = new Ethnic();
        }
        ethnic.setCode(dto.getCode());
        ethnic.setName(dto.getName());
        ethnic.setDescription(dto.getDescription());
        ethnic.setIsDeleted(false);
        Ethnic saved = ethnicRepository.save(ethnic);
        return new EthnicDto(saved);
    }

    @Override
    public void deleteEthnic(UUID id) {
        Ethnic ethnic = ethnicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Dân tộc với ID: " + id));
        ethnic.setIsDeleted(true);
        ethnicRepository.save(ethnic);
    }

    @Override
    public boolean isValidCode(EthnicDto dto) {
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            return false;
        }
        Optional<Ethnic> existing = ethnicRepository.findByCode(dto.getCode().trim());
        if (existing.isPresent()) {
            return dto.getId() != null && dto.getId().equals(existing.get().getId());
        }
        return true;
    }
}
