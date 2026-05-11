package com.tlu.hrm.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tlu.hrm.dto.request.StaffDto;

public interface StaffService {
    List<StaffDto> getAllStaffs();
    Optional<StaffDto> getStaffById(UUID id);
    StaffDto saveStaff(StaffDto staffDto);
    void deleteStaff(UUID id);
    boolean existsById(UUID id);
    String generateStaffCode();
}
