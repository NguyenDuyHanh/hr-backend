package com.tlu.hrm.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tlu.hrm.dto.request.StaffDto;
import com.tlu.hrm.dto.search.SearchDto;

import org.springframework.data.domain.Page;

public interface StaffService {
    Page<StaffDto> getAllStaffs(SearchDto searchDto);
    List<StaffDto> getAllStaffsUnpaginated();
    Optional<StaffDto> getStaffById(UUID id);
    StaffDto saveStaff(StaffDto staffDto);
    void deleteStaff(UUID id);
    boolean existsById(UUID id);
    String generateStaffCode();
}
