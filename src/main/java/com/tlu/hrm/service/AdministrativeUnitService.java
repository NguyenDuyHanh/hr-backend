package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.AdministrativeUnitDto;
import com.tlu.hrm.dto.search.SearchDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface AdministrativeUnitService {
    Page<AdministrativeUnitDto> pagingAdministrativeUnits(SearchDto searchDto);
    List<AdministrativeUnitDto> getAllProvinces();
    List<AdministrativeUnitDto> getChildrenByParentCode(String parentCode);
    AdministrativeUnitDto getById(UUID id);
    AdministrativeUnitDto saveAdministrativeUnit(AdministrativeUnitDto dto);
    void deleteAdministrativeUnit(UUID id);
    boolean isValidCode(AdministrativeUnitDto dto);
}
