package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.StaffDto;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.User;
import com.tlu.hrm.enums.WorkingStatus;
import com.tlu.hrm.enums.Gender;
import com.tlu.hrm.repository.DepartmentRepository;
import com.tlu.hrm.repository.PositionRepository;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.repository.UserRepository;
import com.tlu.hrm.service.StaffService;
import com.tlu.hrm.utils.ExcelUtil;
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

    @Autowired
    private UserRepository userRepository;

    private List<Staff> getFilteredStaffList(SearchDto searchDto) {
        return staffRepository.findAll().stream()
                .filter(staff -> staff.getIsDeleted() == null || !staff.getIsDeleted())
                .filter(staff -> {
                    if (searchDto != null) {
                        // 1. Keyword search
                        if (searchDto.getKeyword() != null && !searchDto.getKeyword().isEmpty()) {
                            String keyword = searchDto.getKeyword().toLowerCase();
                            boolean matches = (staff.getStaffCode() != null
                                    && staff.getStaffCode().toLowerCase().contains(keyword))
                                    || (staff.getDisplayName() != null
                                            && staff.getDisplayName().toLowerCase().contains(keyword))
                                    || (staff.getEmail() != null && staff.getEmail().toLowerCase().contains(keyword))
                                    || (staff.getPhoneNumber() != null && staff.getPhoneNumber().contains(keyword));
                            if (!matches)
                                return false;
                        }
                        // 2. Department filter
                        if (searchDto.getDepartmentId() != null) {
                            if (staff.getDepartment() == null
                                    || !staff.getDepartment().getId().equals(searchDto.getDepartmentId())) {
                                return false;
                            }
                        }
                        // Position filter
                        if (searchDto.getPositionId() != null) {
                            if (staff.getPosition() == null
                                    || !staff.getPosition().getId().equals(searchDto.getPositionId())) {
                                return false;
                            }
                        }
                        // WorkingStatus filter
                        if (searchDto.getWorkingStatus() != null) {
                            if (staff.getWorkingStatus() != searchDto.getWorkingStatus()) {
                                return false;
                            }
                        }
                        // 3. Custom role filter for recruitment approvers (ADMIN, HR_MANAGER, HR_RECRUITMENT)
                        if ("recruitment_approvers".equals(searchDto.getExtWhereClause())) {
                            User user = userRepository.findByStaffId(staff.getId()).orElse(null);
                            if (user == null) {
                                return false;
                            }
                            boolean isApprover = user.getUserRoles().stream()
                                    .anyMatch(ur -> {
                                        String roleName = ur.getRole().getName();
                                        return "ROLE_ADMIN".equals(roleName) 
                                            || "HR_MANAGER".equals(roleName) 
                                            || "HR_RECRUITMENT".equals(roleName);
                                    });
                            if (!isApprover) {
                                return false;
                            }
                        }
                        // 4. Custom filter for staff without user accounts (optionally ignoring a specific user ID)
                        if (searchDto.getExtWhereClause() != null && searchDto.getExtWhereClause().startsWith("no_account")) {
                            User user = userRepository.findByStaffId(staff.getId()).orElse(null);
                            if (user != null) {
                                if (searchDto.getExtWhereClause().length() > 10) {
                                    String currentUserIdStr = searchDto.getExtWhereClause().substring(11); // "no_account_".length() = 11
                                    try {
                                        java.util.UUID currentUserId = java.util.UUID.fromString(currentUserIdStr);
                                        if (user.getId().equals(currentUserId)) {
                                            return true;
                                        }
                                    } catch (IllegalArgumentException ignored) {}
                                }
                                return false;
                            }
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<StaffDto> getAllStaffs(SearchDto searchDto) {
        List<Staff> filteredList = getFilteredStaffList(searchDto);

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
                .filter(staff -> staff.getIsDeleted() == null || !staff.getIsDeleted())
                .map(StaffDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StaffDto> getStaffById(UUID id) {
        return staffRepository.findById(id)
                .filter(staff -> staff.getIsDeleted() == null || !staff.getIsDeleted())
                .map(StaffDto::new);
    }

    @Override
    public StaffDto saveStaff(StaffDto dto) {
        // Validate email uniqueness if email is provided
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            String email = dto.getEmail().trim();
            boolean emailExists;
            if (dto.getId() != null) {
                emailExists = staffRepository.existsByEmailAndIdNotAndActive(email, dto.getId());
            } else {
                emailExists = staffRepository.existsByEmailAndActive(email);
            }
            if (emailExists) {
                throw new com.tlu.hrm.exception.BadRequestException("Email đã tồn tại trong hệ thống");
            }
        }

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
        staff.setStartDate(dto.getStartDate());
        staff.setCurrentAddress(dto.getCurrentAddress());
        staff.setSocialInsuranceCode(dto.getSocialInsuranceCode());

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
        staff.setAvatarUrl(dto.getAvatarUrl());
        staff.setBirthPlace(dto.getBirthPlace());
        staff.setNationality(dto.getNationality());
        staff.setEthnics(dto.getEthnics());
        staff.setReligion(dto.getReligion());
        staff.setEducationDegree(dto.getEducationDegree());
        staff.setProvince(dto.getProvince());
        staff.setCommune(dto.getCommune());
        staff.setPermanentResidence(dto.getPermanentResidence());
        staff.setCurrentResidence(dto.getCurrentResidence());
        staff.setIdNumberIssueDate(dto.getIdNumberIssueDate());
        staff.setIdNumberIssueBy(dto.getIdNumberIssueBy());
        staff.setCompanyEmail(dto.getCompanyEmail());
        staff.setTaxCode(dto.getTaxCode());
        staff.setHealthInsuranceNumber(dto.getHealthInsuranceNumber());
        staff.setBankName(dto.getBankName());
        staff.setBankAccountNumber(dto.getBankAccountNumber());
        staff.setBankAccountName(dto.getBankAccountName());
        staff.setBankBin(dto.getBankBin());
        staff.setAnnualLeave(dto.getAnnualLeave());

        Staff savedStaff = staffRepository.save(staff);
        return new StaffDto(savedStaff);
    }

    @Override
    public void deleteStaff(UUID id) {
        staffRepository.findById(id).ifPresent(staff -> {
            // Ngắt liên kết với tài khoản User (nếu có)
            userRepository.findByStaffId(id).ifPresent(user -> {
                user.setStaff(null);
                userRepository.save(user);
            });

            staff.setIsDeleted(true);
            staffRepository.save(staff);
        });
    }

    @Override
    public boolean existsById(UUID id) {
        return staffRepository.findById(id)
                .map(staff -> staff.getIsDeleted() == null || !staff.getIsDeleted())
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
                .max(java.util.Comparator.comparingInt(code -> Integer.parseInt(code.substring(code.indexOf("_") + 1))))
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

    @Override
    public byte[] exportStaffExcel(SearchDto searchDto) {
        List<StaffDto> filteredList = getFilteredStaffList(searchDto).stream()
                .map(StaffDto::new)
                .collect(Collectors.toList());

        List<String> headers = List.of(
                "STT",
                "Mã nhân viên",
                "Họ và tên",
                "Ngày sinh",
                "Giới tính",
                "SĐT",
                "Email",
                "CCCD/CMND",
                "Ngày vào làm",
                "Trạng thái NV",
                "Phòng ban",
                "Vị trí",
                "Nơi ở hiện tại",
                "Mã BHXH",
                "Mã số thuế");

        return ExcelUtil.exportToExcel("Danh sách nhân viên", headers, filteredList,
                (dto, row, style, centerStyle, stt) -> {
                    int col = 0;
                    ExcelUtil.writeCell(row, col++, stt, centerStyle); // STT (center)
                    ExcelUtil.writeCell(row, col++, dto.getStaffCode(), centerStyle); // Mã NV (center)
                    ExcelUtil.writeCell(row, col++, dto.getDisplayName(), style);
                    ExcelUtil.writeCell(row, col++, dto.getBirthDate(), centerStyle); // Ngày sinh (center)

                    String genderStr = "";
                    if (dto.getGender() != null) {
                        genderStr = switch (dto.getGender()) {
                            case MALE -> "Nam";
                            case FEMALE -> "Nữ";
                            case OTHER -> "Khác";
                        };
                    }
                    ExcelUtil.writeCell(row, col++, genderStr, centerStyle); // Giới tính (center)
                    ExcelUtil.writeCell(row, col++, dto.getPhoneNumber(), style);
                    ExcelUtil.writeCell(row, col++, dto.getEmail(), style);
                    ExcelUtil.writeCell(row, col++, dto.getIdNumber(), style);
                    ExcelUtil.writeCell(row, col++, dto.getStartDate(), centerStyle); // Ngày vào làm (center)
                    String workingStatusStr = "";
                    if (dto.getWorkingStatus() != null) {
                        workingStatusStr = switch (dto.getWorkingStatus()) {
                            case PROBATION -> "Thử việc";
                            case ACTIVE -> "Đang làm việc";
                            case TEMPORARY_LEAVE -> "Tạm nghỉ";
                            case RESIGNED -> "Đã nghỉ việc";
                        };
                    }
                    ExcelUtil.writeCell(row, col++, workingStatusStr, style);
                    ExcelUtil.writeCell(row, col++, dto.getDepartmentName(), style);
                    ExcelUtil.writeCell(row, col++, dto.getPositionName(), style);
                    ExcelUtil.writeCell(row, col++, dto.getCurrentAddress(), style);
                    ExcelUtil.writeCell(row, col++, dto.getSocialInsuranceCode(), style);
                    ExcelUtil.writeCell(row, col++, dto.getTaxCode(), style);
                });
    }
}
