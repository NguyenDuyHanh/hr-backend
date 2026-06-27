package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.PositionDto;
import com.tlu.hrm.dto.search.SearchDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface PositionService {
    Page<PositionDto> pagingPositions(SearchDto searchDto);
    List<PositionDto> getAllPositions();
    PositionDto getById(UUID id);
    PositionDto savePosition(PositionDto dto);
    void deletePosition(UUID id);
    void deleteMultiple(List<UUID> ids);
    boolean isValidCode(PositionDto dto);
    String generateCode();
}
