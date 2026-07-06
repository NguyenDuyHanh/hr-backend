package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.HolidayDto;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.tlu.hrm.dto.search.HolidaySearchRequest;

public interface HolidayService {
    Page<HolidayDto> getPage(HolidaySearchRequest request);
    List<HolidayDto> getAll();
    List<HolidayDto> getByYear(Integer year);
    HolidayDto saveOrUpdate(HolidayDto dto);
    HolidayDto getById(UUID id);
    boolean delete(UUID id);
    boolean isHoliday(LocalDate date);
    List<HolidayDto> getHolidaysInRange(LocalDate start, LocalDate end);
}
