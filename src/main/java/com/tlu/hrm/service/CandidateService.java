package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.CandidateDto;
import com.tlu.hrm.dto.request.StaffDto;
import com.tlu.hrm.dto.search.SearchCandidateDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface CandidateService {
    Page<CandidateDto> pagingCandidates(SearchCandidateDto searchDto);
    CandidateDto getById(UUID id);
    CandidateDto saveCandidate(CandidateDto dto);
    void deleteCandidate(UUID id);
    void deleteMultiple(List<UUID> ids);
    boolean isValidCode(CandidateDto dto);
    String generateCode();
    Boolean updateStatus(UUID id, Integer status, String refusalReason);
    StaffDto convertToReceivedJob(UUID id);
}
