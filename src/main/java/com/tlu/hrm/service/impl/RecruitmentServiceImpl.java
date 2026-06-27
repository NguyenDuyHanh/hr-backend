package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.RecruitmentDto;
import com.tlu.hrm.dto.search.SearchRecruitmentDto;
import com.tlu.hrm.enums.RecruitmentStatus;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.model.Recruitment;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.repository.RecruitmentRepository;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.service.RecruitmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecruitmentServiceImpl implements RecruitmentService {

    @Autowired
    private RecruitmentRepository recruitmentRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public Page<RecruitmentDto> pagingRecruitment(SearchRecruitmentDto searchDto) {
        List<Recruitment> list = recruitmentRepository.findAll().stream()
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                .filter(p -> {
                    if (searchDto != null) {
                        // General Keyword Filter (search code, name, description)
                        if (searchDto.getKeyword() != null && !searchDto.getKeyword().isEmpty()) {
                            String kw = searchDto.getKeyword().toLowerCase().trim();
                            boolean match = (p.getName() != null && p.getName().toLowerCase().contains(kw))
                                    || (p.getCode() != null && p.getCode().toLowerCase().contains(kw))
                                    || (p.getDescription() != null && p.getDescription().toLowerCase().contains(kw));
                            if (!match) return false;
                        }
                        
                        // Exact/Contain Code filter
                        if (searchDto.getCode() != null && !searchDto.getCode().trim().isEmpty()) {
                            String codeSearch = searchDto.getCode().toLowerCase().trim();
                            if (p.getCode() == null || !p.getCode().toLowerCase().contains(codeSearch)) {
                                return false;
                            }
                        }

                        // Contain Name (Tiêu đề tuyển dụng) filter
                        if (searchDto.getName() != null && !searchDto.getName().trim().isEmpty()) {
                            String nameSearch = searchDto.getName().toLowerCase().trim();
                            if (p.getName() == null || !p.getName().toLowerCase().contains(nameSearch)) {
                                return false;
                            }
                        }

                        // Status Filter (checked against Enum values)
                        if (searchDto.getStatus() != null) {
                            if (p.getStatus() != searchDto.getStatus()) {
                                return false;
                            }
                        }

                        // CV Reviewer (Người duyệt CV) Filter
                        if (searchDto.getPersonApproveCVId() != null) {
                            if (p.getPersonApproveCV() == null || !p.getPersonApproveCV().getId().equals(searchDto.getPersonApproveCVId())) {
                                return false;
                            }
                        }
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    if (a.getCreateDate() != null && b.getCreateDate() != null) {
                        return b.getCreateDate().compareTo(a.getCreateDate());
                    }
                    return 0;
                })
                .collect(Collectors.toList());

        int total = list.size();
        int pageIndex = 0;
        int pageSize = 10;

        if (searchDto != null) {
            pageIndex = searchDto.getPageIndex() >= 1 ? searchDto.getPageIndex() - 1 : 0;
            pageSize = searchDto.getPageSize() > 0 ? searchDto.getPageSize() : 10;
        }

        int fromIndex = pageIndex * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<RecruitmentDto> pageContent = new ArrayList<>();
        if (fromIndex < total) {
            pageContent = list.subList(fromIndex, toIndex).stream()
                    .map(RecruitmentDto::new)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(pageContent, PageRequest.of(pageIndex, pageSize), total);
    }

    @Override
    public RecruitmentDto getById(UUID id) {
        Recruitment entity = recruitmentRepository.findById(id)
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng với ID: " + id));
        return new RecruitmentDto(entity);
    }

    @Override
    public RecruitmentDto saveRecruitment(RecruitmentDto dto) {
        Recruitment entity;
        if (dto.getId() != null) {
            entity = recruitmentRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng với ID: " + dto.getId()));
        } else {
            entity = new Recruitment();
            if (dto.getCode() == null || dto.getCode().isEmpty()) {
                entity.setCode(generateCode());
            } else {
                entity.setCode(dto.getCode());
            }
        }

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        // Default to RECRUITING (1) if status is not specified
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : RecruitmentStatus.RECRUITING);

        if (dto.getPersonApproveCVId() != null) {
            Staff reviewer = staffRepository.findById(dto.getPersonApproveCVId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên duyệt hồ sơ với ID: " + dto.getPersonApproveCVId()));
            entity.setPersonApproveCV(reviewer);
        } else {
            entity.setPersonApproveCV(null);
        }

        Recruitment saved = recruitmentRepository.save(entity);
        return new RecruitmentDto(saved);
    }

    @Override
    public void deleteRecruitment(UUID id) {
        Recruitment entity = recruitmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng với ID: " + id));
        entity.setIsDeleted(true);
        recruitmentRepository.save(entity);
    }

    @Override
    public void deleteMultipleRecruitment(List<UUID> ids) {
        if (ids != null) {
            for (UUID id : ids) {
                recruitmentRepository.findById(id).ifPresent(entity -> {
                    entity.setIsDeleted(true);
                    recruitmentRepository.save(entity);
                });
            }
        }
    }

    @Override
    public boolean isValidCode(RecruitmentDto dto) {
        if (dto == null || dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            return false;
        }
        if (dto.getId() == null) {
            return !recruitmentRepository.existsByCode(dto.getCode());
        }
        return !recruitmentRepository.existsByCodeAndIdNot(dto.getCode(), dto.getId());
    }

    @Override
    public String generateCode() {
        LocalDate now = LocalDate.now();
        String yearStr = String.format("%02d", now.getYear() % 100);
        String monthStr = String.format("%02d", now.getMonthValue());
        String prefix = "TTD" + yearStr + monthStr + "_";

        List<Recruitment> allRecruitments = recruitmentRepository.findAll();
        String maxCode = allRecruitments.stream()
                .map(Recruitment::getCode)
                .filter(Objects::nonNull)
                .filter(code -> code.startsWith(prefix))
                .max(Comparator.naturalOrder())
                .orElse(null);

        int nextNum = 1;
        if (maxCode != null && maxCode.length() > prefix.length()) {
            try {
                String suffix = maxCode.substring(prefix.length());
                nextNum = Integer.parseInt(suffix) + 1;
            } catch (NumberFormatException ignored) {}
        }

        return prefix + String.format("%03d", nextNum);
    }
}
