package com.tlu.hrm.service;

import com.tlu.hrm.model.Period;

import com.tlu.hrm.dto.request.PeriodDto;
import com.tlu.hrm.dto.search.PeriodSearchRequest;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.UUID;

public interface PeriodService {
    Period createPeriod(PeriodDto dto);
    Period updatePeriod(UUID id, PeriodDto dto);
    List<Period> getAllPeriods();
    void deletePeriod(UUID periodId);
    Page<Period> getPeriods(PeriodSearchRequest request);
}
