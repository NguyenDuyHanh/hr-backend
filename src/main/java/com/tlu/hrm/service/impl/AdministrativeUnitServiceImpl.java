package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.AdministrativeUnitDto;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.model.AdministrativeUnit;
import com.tlu.hrm.repository.AdministrativeUnitRepository;
import com.tlu.hrm.service.AdministrativeUnitService;
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
public class AdministrativeUnitServiceImpl implements AdministrativeUnitService {

    @Autowired
    private AdministrativeUnitRepository administrativeUnitRepository;

    @Override
    public Page<AdministrativeUnitDto> pagingAdministrativeUnits(SearchDto searchDto) {
        int pageIndex = searchDto != null && searchDto.getPageIndex() > 0 ? searchDto.getPageIndex() - 1 : 0;
        int pageSize = searchDto != null && searchDto.getPageSize() > 0 ? searchDto.getPageSize() : 10;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        String keyword = searchDto != null && searchDto.getKeyword() != null ? searchDto.getKeyword().trim() : "";
        Integer level = searchDto != null ? searchDto.getLevel() : null;

        Page<AdministrativeUnit> pageResult = administrativeUnitRepository.searchUnits(keyword, level, pageable);
        return pageResult.map(AdministrativeUnitDto::new);
    }

    @Override
    public List<AdministrativeUnitDto> getAllProvinces() {
        return administrativeUnitRepository.findByLevelAndIsDeletedFalseOrderByNameAsc(1)
                .stream().map(AdministrativeUnitDto::new).collect(Collectors.toList());
    }

    @Override
    public List<AdministrativeUnitDto> getChildrenByParentCode(String parentCode) {
        return administrativeUnitRepository.findByParentCodeAndIsDeletedFalseOrderByNameAsc(parentCode)
                .stream().map(AdministrativeUnitDto::new).collect(Collectors.toList());
    }

    @Override
    public AdministrativeUnitDto getById(UUID id) {
        AdministrativeUnit unit = administrativeUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Đơn vị hành chính với ID: " + id));
        return new AdministrativeUnitDto(unit);
    }

    @Override
    public AdministrativeUnitDto saveAdministrativeUnit(AdministrativeUnitDto dto) {
        AdministrativeUnit unit;
        if (dto.getId() != null) {
            unit = administrativeUnitRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Đơn vị hành chính với ID: " + dto.getId()));
        } else {
            unit = new AdministrativeUnit();
        }
        unit.setCode(dto.getCode());
        unit.setName(dto.getName());
        unit.setCodename(dto.getCodename());
        unit.setDivisionType(dto.getDivisionType());
        unit.setShortCodename(dto.getShortCodename());
        unit.setPhoneCode(dto.getPhoneCode());
        unit.setLevel(dto.getLevel() != null ? dto.getLevel() : 1);
        unit.setParentCode(dto.getParentCode());
        unit.setIsDeleted(false);

        if (dto.getParentId() != null) {
            administrativeUnitRepository.findById(dto.getParentId()).ifPresent(unit::setParent);
        }

        AdministrativeUnit saved = administrativeUnitRepository.save(unit);
        return new AdministrativeUnitDto(saved);
    }

    @Override
    public void deleteAdministrativeUnit(UUID id) {
        AdministrativeUnit unit = administrativeUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Đơn vị hành chính với ID: " + id));
        unit.setIsDeleted(true);
        administrativeUnitRepository.save(unit);
    }

    @Override
    public boolean isValidCode(AdministrativeUnitDto dto) {
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            return false;
        }
        Integer level = dto.getLevel() != null ? dto.getLevel() : 1;
        Optional<AdministrativeUnit> existing = administrativeUnitRepository.findByCodeAndLevel(dto.getCode().trim(), level);
        if (existing.isPresent()) {
            return dto.getId() != null && dto.getId().equals(existing.get().getId());
        }
        return true;
    }
}
