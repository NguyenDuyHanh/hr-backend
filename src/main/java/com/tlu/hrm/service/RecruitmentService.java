package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.RecruitmentDto;
import com.tlu.hrm.dto.search.SearchRecruitmentDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface RecruitmentService {
    Page<RecruitmentDto> pagingRecruitment(SearchRecruitmentDto searchDto);
    RecruitmentDto getById(UUID id);
    RecruitmentDto saveRecruitment(RecruitmentDto dto);
    void deleteRecruitment(UUID id);
    void deleteMultipleRecruitment(List<UUID> ids);
    boolean isValidCode(RecruitmentDto dto);
    String generateCode();
}
