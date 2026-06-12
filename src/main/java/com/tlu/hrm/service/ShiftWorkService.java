package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.ShiftWorkDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ShiftWorkService {
    Page<ShiftWorkDto> getPage(int pageIndex, int pageSize, String keyword);
    List<ShiftWorkDto> getAll();
    ShiftWorkDto saveOrUpdate(ShiftWorkDto dto);
    ShiftWorkDto getById(UUID id);
    boolean delete(UUID id);
    void initDefaultShifts();
}
