package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.AnnouncementDto;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.enums.AnnouncementCategory;
import com.tlu.hrm.enums.AnnouncementStatus;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface AnnouncementService {
    Page<AnnouncementDto> search(String username, String keyword, AnnouncementCategory category, AnnouncementStatus status, UUID deptId, SearchDto searchDto);
    AnnouncementDto saveOrUpdate(AnnouncementDto dto);
    AnnouncementDto getById(UUID id);
    void delete(UUID id);
    String generateAnnouncementCode();
}
