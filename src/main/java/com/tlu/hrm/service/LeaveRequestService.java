package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.LeaveRequestDto;
import com.tlu.hrm.dto.response.StaffAnnualLeaveBalanceDto;
import com.tlu.hrm.dto.search.LeaveRequestSearchRequest;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface LeaveRequestService {
    Page<LeaveRequestDto> search(LeaveRequestSearchRequest request);

    LeaveRequestDto getById(UUID id);

    LeaveRequestDto create(LeaveRequestDto dto);

    LeaveRequestDto update(UUID id, LeaveRequestDto dto);

    void delete(UUID id);

    LeaveRequestDto approve(UUID id, String rejectReason);

    LeaveRequestDto reject(UUID id, String rejectReason);

    StaffAnnualLeaveBalanceDto getLeaveBalance(UUID staffId, int year);

    Page<StaffAnnualLeaveBalanceDto> getLeaveBalances(com.tlu.hrm.dto.search.SearchDto searchDto, int year);
}
