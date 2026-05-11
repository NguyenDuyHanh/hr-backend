package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.StaffDto;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.repository.DepartmentRepository;
import com.tlu.hrm.repository.PositionTitleRepository;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private PositionTitleRepository positionTitleRepository;

    @Override
    public List<StaffDto> getAllStaffs() {
        return staffRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StaffDto> getStaffById(UUID id) {
        return staffRepository.findById(id).map(this::toDto);
    }

    @Override
    public StaffDto saveStaff(StaffDto staffDto) {
        Staff staff = toEntity(staffDto);
        Staff savedStaff = staffRepository.save(staff);
        return toDto(savedStaff);
    }

    @Override
    public void deleteStaff(UUID id) {
        staffRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return staffRepository.existsById(id);
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

    private StaffDto toDto(Staff staff) {
        StaffDto dto = new StaffDto();
        dto.setId(staff.getId());
        dto.setStaffCode(staff.getStaffCode());
        dto.setDisplayName(staff.getDisplayName());
        dto.setBirthDate(staff.getBirthDate());
        dto.setGender(staff.getGender());
        dto.setPhoneNumber(staff.getPhoneNumber());
        dto.setEmail(staff.getEmail());
        dto.setWorkingStatus(staff.getWorkingStatus());
        dto.setIdNumber(staff.getIdNumber());
        dto.setRecruitmentDate(staff.getRecruitmentDate());
        dto.setStartDate(staff.getStartDate());
        dto.setCurrentAddress(staff.getCurrentAddress());
        dto.setSocialInsuranceCode(staff.getSocialInsuranceCode());
        dto.setLevel(staff.getLevel());
        
        if (staff.getDepartment() != null) {
            dto.setDepartmentId(staff.getDepartment().getId());
            dto.setDepartmentName(staff.getDepartment().getName());
        }
        
        if (staff.getPosition() != null) {
            dto.setPositionId(staff.getPosition().getId());
            dto.setPositionName(staff.getPosition().getName());
        }
        
        return dto;
    }

    private Staff toEntity(StaffDto dto) {
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
        }
        
        if (dto.getPositionId() != null) {
            positionTitleRepository.findById(dto.getPositionId()).ifPresent(staff::setPosition);
        }
        
        return staff;
    }
}
