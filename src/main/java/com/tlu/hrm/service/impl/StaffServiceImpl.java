package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.StaffDto;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.repository.DepartmentRepository;
import com.tlu.hrm.repository.PositionRepository;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StaffServiceImpl implements StaffService {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Override
    public Page<StaffDto> getAllStaffs(SearchDto searchDto) {
        List<Staff> filteredList = staffRepository.findAll().stream()
                .filter(staff -> staff.getVoided() == null || !staff.getVoided())
                .filter(staff -> {
                    if (searchDto != null) {
                        // 1. Keyword search
                        if (searchDto.getKeyword() != null && !searchDto.getKeyword().isEmpty()) {
                            String keyword = searchDto.getKeyword().toLowerCase();
                            boolean matches = (staff.getStaffCode() != null && staff.getStaffCode().toLowerCase().contains(keyword))
                                || (staff.getDisplayName() != null && staff.getDisplayName().toLowerCase().contains(keyword))
                                || (staff.getEmail() != null && staff.getEmail().toLowerCase().contains(keyword))
                                || (staff.getPhoneNumber() != null && staff.getPhoneNumber().contains(keyword));
                            if (!matches) return false;
                        }
                        // 2. Department filter
                        if (searchDto.getDepartmentId() != null) {
                            if (staff.getDepartment() == null || !staff.getDepartment().getId().equals(searchDto.getDepartmentId())) {
                                return false;
                            }
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int total = filteredList.size();
        int pageNum = 0;
        int size = 10;

        if (searchDto != null) {
            pageNum = searchDto.getPageIndex() >= 1 ? searchDto.getPageIndex() - 1 : 0;
            size = searchDto.getPageSize() > 0 ? searchDto.getPageSize() : 10;
        }

        int fromIndex = pageNum * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<StaffDto> pageContent = new java.util.ArrayList<>();
        if (fromIndex < total) {
            pageContent = filteredList.subList(fromIndex, toIndex).stream()
                    .map(StaffDto::new)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(pageContent, PageRequest.of(pageNum, size), total);
    }

    @Override
    public List<StaffDto> getAllStaffsUnpaginated() {
        return staffRepository.findAll().stream()
                .filter(staff -> staff.getVoided() == null || !staff.getVoided())
                .map(StaffDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StaffDto> getStaffById(UUID id) {
        return staffRepository.findById(id)
                .filter(staff -> staff.getVoided() == null || !staff.getVoided())
                .map(StaffDto::new);
    }

    @Override
    public StaffDto saveStaff(StaffDto dto) {
        Staff staff;
        if (dto.getId() != null) {
            staff = staffRepository.findById(dto.getId()).orElse(new Staff());
        } else {
            staff = new Staff();
        }
        
        staff.setStaffCode(dto.getStaffCode());
        staff.setDisplayName(dto.getDisplayName());
        staff.setBirthDate(dto.getBirthDate());
        staff.setGender(dto.getGender());
        staff.setPhoneNumber(dto.getPhoneNumber());
        staff.setEmail(dto.getEmail());
        staff.setWorkingStatus(dto.getWorkingStatus());
        staff.setIdNumber(dto.getIdNumber());
        staff.setRecruitmentDate(dto.getRecruitmentDate());
        staff.setStartDate(dto.getStartDate());
        staff.setCurrentAddress(dto.getCurrentAddress());
        staff.setSocialInsuranceCode(dto.getSocialInsuranceCode());
        staff.setLevel(dto.getLevel());
        
        if (dto.getDepartmentId() != null) {
            departmentRepository.findById(dto.getDepartmentId()).ifPresent(staff::setDepartment);
        } else {
            staff.setDepartment(null);
        }
        
        if (dto.getPositionId() != null) {
            positionRepository.findById(dto.getPositionId()).ifPresent(staff::setPosition);
        } else {
            staff.setPosition(null);
        }

        // Map expanded fields
        staff.setImagePath(dto.getImagePath());
        staff.setMaritalStatus(dto.getMaritalStatus());
        staff.setBirthPlace(dto.getBirthPlace());
        staff.setNationalityId(dto.getNationalityId());
        staff.setEthnicsId(dto.getEthnicsId());
        staff.setReligionId(dto.getReligionId());
        staff.setEducationDegreeId(dto.getEducationDegreeId());
        staff.setProvinceId(dto.getProvinceId());
        staff.setAdministrativeunitId(dto.getAdministrativeunitId());
        staff.setPermanentResidence(dto.getPermanentResidence());
        staff.setCurrentResidence(dto.getCurrentResidence());
        staff.setHomeTown(dto.getHomeTown());
        staff.setIdNumberIssueDate(dto.getIdNumberIssueDate());
        staff.setIdNumberIssueBy(dto.getIdNumberIssueBy());
        staff.setPersonalIdentificationNumber(dto.getPersonalIdentificationNumber());
        staff.setPersonalIdentificationIssueDate(dto.getPersonalIdentificationIssueDate());
        staff.setPersonalIdentificationIssuePlace(dto.getPersonalIdentificationIssuePlace());
        staff.setPassportNumber(dto.getPassportNumber());
        staff.setWorkPermitNumber(dto.getWorkPermitNumber());
        staff.setStatusId(dto.getStatusId());
        staff.setStaffWorkingFormat(dto.getStaffWorkingFormat());
        staff.setIntroducerId(dto.getIntroducerId());
        staff.setRecruiterId(dto.getRecruiterId());
        staff.setApprenticeDays(dto.getApprenticeDays());
        staff.setCompanyEmail(dto.getCompanyEmail());
        staff.setStaffPhase(dto.getStaffPhase());
        staff.setStaffPositionType(dto.getStaffPositionType());
        staff.setHealthCareRegistrationPlaceId(dto.getHealthCareRegistrationPlaceId());
        staff.setStaffWorkShiftType(dto.getStaffWorkShiftType());
        staff.setFixShiftWorkId(dto.getFixShiftWorkId());
        staff.setStaffLeaveShiftType(dto.getStaffLeaveShiftType());
        staff.setFixLeaveWeekDay(dto.getFixLeaveWeekDay());
        staff.setFixLeaveWeekDay2(dto.getFixLeaveWeekDay2());
        staff.setSkipTimekeeping(dto.getSkipTimekeeping());
        staff.setSkipLateEarlyCount(dto.getSkipLateEarlyCount());
        staff.setSkipOvertimeCount(dto.getSkipOvertimeCount());
        staff.setOnBlacklist(dto.getOnBlacklist());
        staff.setHasSocialIns(dto.getHasSocialIns());
        staff.setUnemploymentDeclaration(dto.getUnemploymentDeclaration());
        staff.setAllowExternalIpTimekeeping(dto.getAllowExternalIpTimekeeping());
        staff.setOrganizationId(dto.getOrganizationId());
        staff.setPositionTitleId(dto.getPositionTitleId());
        staff.setContactPersonInfo(dto.getContactPersonInfo());
        staff.setTaxCode(dto.getTaxCode());
        staff.setSocialInsuranceNumber(dto.getSocialInsuranceNumber());
        staff.setHealthInsuranceNumber(dto.getHealthInsuranceNumber());
        staff.setSocialInsuranceNote(dto.getSocialInsuranceNote());
        staff.setBankName(dto.getBankName());
        staff.setBankAccountNumber(dto.getBankAccountNumber());
        staff.setBankAccountName(dto.getBankAccountName());
        staff.setBankBin(dto.getBankBin());

        Staff savedStaff = staffRepository.save(staff);
        return new StaffDto(savedStaff);
    }

    @Override
    public void deleteStaff(UUID id) {
        staffRepository.findById(id).ifPresent(staff -> {
            staff.setVoided(true);
            staffRepository.save(staff);
        });
    }

    @Override
    public boolean existsById(UUID id) {
        return staffRepository.findById(id)
                .map(staff -> staff.getVoided() == null || !staff.getVoided())
                .orElse(false);
    }

    @Override
    public String generateStaffCode() {
        // 1. Tạo tiền tố dựa trên năm và tháng hiện tại (Ví dụ: NV2605_)
        java.time.LocalDate now = java.time.LocalDate.now();
        String year = String.format("%02d", now.getYear() % 100);
        String month = String.format("%02d", now.getMonthValue());
        String prefix = "NV" + year + month + "_";

        // 2. Lấy tất cả mã nhân viên có định dạng NV..._... từ Repository
        java.util.List<String> codes = staffRepository.findMaxValidStaffCode();

        // 3. Tìm mã có số thứ tự (phần sau dấu _) lớn nhất trong toàn hệ thống
        String maxCode = codes.stream()
                .filter(code -> code.matches("^NV[0-9]{4}_[0-9]{3}$"))
                .max(java.util.Comparator.comparingInt(code -> 
                    Integer.parseInt(code.substring(code.indexOf("_") + 1))
                ))
                .orElse(null);

        int nextNumber = 1;
        if (maxCode != null) {
            try {
                String[] parts = maxCode.split("_");
                if (parts.length == 2) {
                    nextNumber = Integer.parseInt(parts[1]) + 1;
                }
            } catch (NumberFormatException ignored) {
                // Nếu parse lỗi thì mặc định bắt đầu từ 1
            }
        }

        // 4. Kết hợp tiền tố hiện tại với số thứ tự mới (Ví dụ: NV2605_005)
        return prefix + String.format("%03d", nextNumber);
    }
}
