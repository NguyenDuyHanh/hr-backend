package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.CandidateDto;
import com.tlu.hrm.dto.search.SearchCandidateDto;
import com.tlu.hrm.enums.CandidateStatus;
import com.tlu.hrm.enums.Gender;
import com.tlu.hrm.enums.WorkingStatus;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.model.*;
import com.tlu.hrm.repository.*;
import com.tlu.hrm.service.CandidateService;
import com.tlu.hrm.service.StaffService;
import com.tlu.hrm.utils.ExcelUtil;
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
                .filter(c -> c.getIsDeleted() == null || !c.getIsDeleted())
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
                .filter(c -> c.getIsDeleted() == null || !c.getIsDeleted())
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

        entity.setAddress(dto.getAddress());
        entity.setCvFileUrl(dto.getCvFileUrl());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : CandidateStatus.SCREENING); // Mặc định SCREENING: Sơ tuyển hồ sơ
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
        entity.setIsDeleted(true);
        candidateRepository.save(entity);
    }

    @Override
    public void deleteMultiple(List<UUID> ids) {
        if (ids != null) {
            for (UUID id : ids) {
                candidateRepository.findById(id).ifPresent(entity -> {
                    entity.setIsDeleted(true);
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
    public Boolean updateStatus(UUID id, CandidateStatus status, String refusalReason) {
        Candidate cand = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ứng viên để cập nhật trạng thái với ID: " + id));
        cand.setStatus(status);
        if (status == CandidateStatus.REJECTED && refusalReason != null) {
            cand.setNote(refusalReason);
        }
        candidateRepository.save(cand);
        return true;
    }

    @Override
    public byte[] exportCandidatesExcel(SearchCandidateDto searchDto) {
        List<CandidateDto> filteredList = candidateRepository.findAll().stream()
                .filter(c -> c.getIsDeleted() == null || !c.getIsDeleted())
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
                .map(CandidateDto::new)
                .collect(Collectors.toList());

        List<String> headers = List.of(
                "STT",
                "Mã ứng viên",
                "Họ và tên",
                "Giới tính",
                "Ngày sinh",
                "SĐT",
                "Email",
                "Địa chỉ",
                "Phòng ban tiếp nhận",
                "Vị trí tiếp nhận",
                "Tin tuyển dụng",
                "Trạng thái",
                "Ghi chú");

        return ExcelUtil.exportToExcel("Danh sách ứng viên", headers, filteredList,
                (dto, row, style, centerStyle, stt) -> {
                    int col = 0;
                    ExcelUtil.writeCell(row, col++, stt, centerStyle); // STT (center)
                    ExcelUtil.writeCell(row, col++, dto.getCandidateCode(), centerStyle); // Mã ứng viên (center)
                    ExcelUtil.writeCell(row, col++, dto.getDisplayName(), style);
                    
                    String genderStr = "";
                    if (dto.getGender() != null) {
                        genderStr = switch (dto.getGender()) {
                            case MALE -> "Nam";
                            case FEMALE -> "Nữ";
                            case OTHER -> "Khác";
                        };
                    }
                    ExcelUtil.writeCell(row, col++, genderStr, centerStyle); // Giới tính (center)
                    ExcelUtil.writeCell(row, col++, dto.getBirthDate(), centerStyle); // Ngày sinh (center)
                    ExcelUtil.writeCell(row, col++, dto.getPhoneNumber(), style);
                    ExcelUtil.writeCell(row, col++, dto.getEmail(), style);
                    ExcelUtil.writeCell(row, col++, dto.getAddress(), style);
                    ExcelUtil.writeCell(row, col++, dto.getDepartmentName(), style);
                    ExcelUtil.writeCell(row, col++, dto.getPositionName(), style);
                    ExcelUtil.writeCell(row, col++, dto.getRecruitmentName(), style);
                    
                    String statusStr = "";
                    if (dto.getStatus() != null) {
                        statusStr = switch (dto.getStatus()) {
                            case SCREENING -> "Sơ tuyển";
                            case INTERVIEW -> "Phỏng vấn";
                            case QUALIFIED -> "Đạt yêu cầu";
                            case WAITING -> "Chờ việc";
                            case ONBOARDED -> "Đã onboard";
                            case REJECTED -> "Từ chối";
                        };
                    }
                    ExcelUtil.writeCell(row, col++, statusStr, centerStyle); // Trạng thái (center)
                    ExcelUtil.writeCell(row, col++, dto.getNote(), style);
                });
    }
}
