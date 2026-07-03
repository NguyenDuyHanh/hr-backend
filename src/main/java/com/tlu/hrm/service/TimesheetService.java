package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.TimesheetDto;
import com.tlu.hrm.dto.search.TimesheetSearchRequest;
import com.tlu.hrm.enums.TimesheetStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TimesheetService {
    Page<TimesheetDto> search(TimesheetSearchRequest request);
    TimesheetDto getById(UUID id);
    TimesheetDto saveOrUpdate(TimesheetDto dto);
    boolean approve(UUID id, TimesheetStatus status, String note);
    void calculateTimesheet(UUID staffId, LocalDate date);
    List<TimesheetDto> getByStaffAndDateRange(UUID staffId, LocalDate start, LocalDate end);
    byte[] exportTimesheetExcel(TimesheetSearchRequest request);
    void initHolidayTimesheets(UUID staffId, LocalDate start, LocalDate end);
}
