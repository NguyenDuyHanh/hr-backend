package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.RoleDto;
import com.tlu.hrm.dto.search.SearchDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    Page<RoleDto> pagingRoles(SearchDto searchDto);
    List<RoleDto> getAllRoles();
    RoleDto getById(UUID id);
    RoleDto saveRole(RoleDto dto);
    void deleteRole(UUID id);
    void deleteMultiple(List<UUID> ids);
    boolean isValidName(RoleDto dto);
}
