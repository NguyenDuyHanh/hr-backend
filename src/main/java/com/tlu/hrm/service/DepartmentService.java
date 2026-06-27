package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.DepartmentDto;
import com.tlu.hrm.dto.search.SearchDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {
    Page<DepartmentDto> pagingDepartments(SearchDto searchDto);
    List<DepartmentDto> getAllDepartments();
    DepartmentDto getById(UUID id);
    DepartmentDto saveDepartment(DepartmentDto dto);
    void deleteDepartment(UUID id);
    void deleteMultiple(List<UUID> ids);
    boolean isValidCode(DepartmentDto dto);
    String generateCode();
}
