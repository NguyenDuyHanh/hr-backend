package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.CandidateDto;
import com.tlu.hrm.dto.request.StaffDto;
import com.tlu.hrm.dto.search.SearchCandidateDto;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.model.*;
import com.tlu.hrm.repository.*;
import com.tlu.hrm.service.CandidateService;
import com.tlu.hrm.service.StaffService;
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
public class CandidateServiceImpl implements CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private RecruitmentRepository recruitmentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StaffService staffService;

    @Override
    public Page<CandidateDto> pagingCandidates(SearchCandidateDto searchDto) {
        List<Candidate> list = candidateRepository.findAll().stream()
                .filter(c -> c.getVoided() == null || !c.getVoided())
                .filter(c -> {
                    if (searchDto != null) {
                        if (searchDto.getKeyword() != null && !searchDto.getKeyword().isEmpty()) {
                            String kw = searchDto.getKeyword().toLowerCase();
                            boolean match = (c.getDisplayName() != null && c.getDisplayName().toLowerCase().contains(kw))
                                    || (c.getCandidateCode() != null && c.getCandidateCode().toLowerCase().contains(kw))
                                    || (c.getEmail() != null && c.getEmail().toLowerCase().contains(kw))
                                    || (c.getPhoneNumber() != null && c.getPhoneNumber().contains(kw));
                            if (!match) return false;
                        }
                        if (searchDto.getRecruitmentId() != null) {
                            if (c.getRecruitment() == null || !searchDto.getRecruitmentId().equals(c.getRecruitment().getId())) {
                                return false;
                            }
                        }
                        if (searchDto.getStatus() != null) {
                            if (!searchDto.getStatus().equals(c.getStatus())) {
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

        List<CandidateDto> pageContent = new ArrayList<>();
        if (fromIndex < total) {
            pageContent = list.subList(fromIndex, toIndex).stream()
                    .map(CandidateDto::new)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(pageContent, PageRequest.of(pageIndex, pageSize), total);
    }

    @Override
    public CandidateDto getById(UUID id) {
        Candidate entity = candidateRepository.findById(id)
                .filter(c -> c.getVoided() == null || !c.getVoided())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ứng viên với ID: " + id));
        return new CandidateDto(entity);
    }

    @Override
    public CandidateDto saveCandidate(CandidateDto dto) {
        Candidate entity;
        if (dto.getId() != null) {
            entity = candidateRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ứng viên với ID: " + dto.getId()));
        } else {
            entity = new Candidate();
            if (dto.getCandidateCode() == null || dto.getCandidateCode().isEmpty()) {
                entity.setCandidateCode(generateCode());
            } else {
                entity.setCandidateCode(dto.getCandidateCode());
            }
        }

        entity.setDisplayName(dto.getDisplayName());
        entity.setGender(dto.getGender());
        entity.setBirthDate(dto.getBirthDate());
        entity.setEmail(dto.getEmail());
        entity.setPhoneNumber(dto.getPhoneNumber());

        entity.setCurrentResidence(dto.getCurrentResidence());
        entity.setCvFilePath(dto.getCvFilePath());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 0); // Mặc định 0: Sơ tuyển hồ sơ
        entity.setOnboardStatus(dto.getOnboardStatus() != null ? dto.getOnboardStatus() : 0);
        entity.setNote(dto.getNote());

        if (dto.getRecruitmentId() != null) {
            Recruitment recruitment = recruitmentRepository.findById(dto.getRecruitmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng với ID: " + dto.getRecruitmentId()));
            entity.setRecruitment(recruitment);
        } else {
            entity.setRecruitment(null);
        }

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ban với ID: " + dto.getDepartmentId()));
            entity.setDepartment(dept);
        } else {
            entity.setDepartment(null);
        }

        if (dto.getPositionId() != null) {
            Position pos = positionRepository.findById(dto.getPositionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vị trí với ID: " + dto.getPositionId()));
            entity.setPosition(pos);
        } else {
            entity.setPosition(null);
        }



        Candidate saved = candidateRepository.save(entity);
        return new CandidateDto(saved);
    }

    @Override
    public void deleteCandidate(UUID id) {
        Candidate entity = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ứng viên với ID: " + id));
        entity.setVoided(true);
        candidateRepository.save(entity);
    }

    @Override
    public void deleteMultiple(List<UUID> ids) {
        if (ids != null) {
            for (UUID id : ids) {
                candidateRepository.findById(id).ifPresent(entity -> {
                    entity.setVoided(true);
                    candidateRepository.save(entity);
                });
            }
        }
    }

    @Override
    public boolean isValidCode(CandidateDto dto) {
        if (dto == null || dto.getCandidateCode() == null || dto.getCandidateCode().trim().isEmpty()) {
            return false;
        }
        if (dto.getId() == null) {
            return !candidateRepository.existsByCandidateCode(dto.getCandidateCode());
        }
        return !candidateRepository.existsByCandidateCodeAndIdNot(dto.getCandidateCode(), dto.getId());
    }

    @Override
    public String generateCode() {
        LocalDate now = LocalDate.now();
        String yearStr = String.format("%02d", now.getYear() % 100);
        String monthStr = String.format("%02d", now.getMonthValue());
        String prefix = "UV" + yearStr + monthStr + "_";

        List<Candidate> allCandidates = candidateRepository.findAll();
        String maxCode = allCandidates.stream()
                .map(Candidate::getCandidateCode)
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

    @Override
    public Boolean updateStatus(UUID id, Integer status, String refusalReason) {
        Candidate cand = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ứng viên để cập nhật trạng thái với ID: " + id));
        cand.setStatus(status);
        if (status == 5 && refusalReason != null) {
            cand.setNote(refusalReason);
        }
        candidateRepository.save(cand);
        return true;
    }

    @Override
    public StaffDto convertToReceivedJob(UUID id) {
        Candidate cand = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ứng viên để tiếp nhận với ID: " + id));

        if (cand.getOnboardStatus() == 1) {
            throw new RuntimeException("Ứng viên này đã được tiếp nhận thành nhân viên trước đó.");
        }

        Staff staff = new Staff();
        staff.setStaffCode(staffService.generateStaffCode());
        staff.setDisplayName(cand.getDisplayName());
        staff.setGender(cand.getGender());
        staff.setBirthDate(cand.getBirthDate());
        staff.setEmail(cand.getEmail());
        staff.setPhoneNumber(cand.getPhoneNumber());

        staff.setCurrentResidence(cand.getCurrentResidence());
        
        staff.setDepartment(cand.getDepartment());
        staff.setPosition(cand.getPosition());

        staff.setStartDate(LocalDate.now());
        staff.setRecruitmentDate(LocalDate.now());
        staff.setWorkingStatus("Thử việc"); // Trạng thái ban đầu khi onboard

        Staff savedStaff = staffRepository.save(staff);

        // Cập nhật trạng thái ứng viên
        cand.setOnboardStatus(1);
        cand.setStatus(4); // 4: Đã onboard

        candidateRepository.save(cand);

        return new StaffDto(savedStaff);
    }
}
