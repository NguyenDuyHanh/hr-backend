package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.LeaveRequestDto;
import com.tlu.hrm.dto.response.StaffAnnualLeaveBalanceDto;
import com.tlu.hrm.dto.search.LeaveRequestSearchRequest;
import com.tlu.hrm.enums.LeaveApprovalStatus;
import com.tlu.hrm.enums.LeaveType;
import com.tlu.hrm.model.LeaveRequest;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.repository.LeaveRequestRepository;
import com.tlu.hrm.repository.ShiftWorkRepository;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.service.LeaveRequestService;
import com.tlu.hrm.service.TimesheetService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ShiftWorkRepository shiftWorkRepository;

    @Autowired
    private TimesheetService timesheetService;

    @Autowired
    private com.tlu.hrm.repository.UserRepository userRepository;

    @Autowired
    private com.tlu.hrm.service.NotificationService notificationService;

    @Override
    public Page<LeaveRequestDto> search(LeaveRequestSearchRequest request) {
        Specification<LeaveRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getStaffId() != null) {
                predicates.add(cb.equal(root.get("requestStaff").get("id"), request.getStaffId()));
            }

            if (request.getLeaveType() != null) {
                predicates.add(cb.equal(root.get("leaveType"), request.getLeaveType()));
            }

            if (request.getApprovalStatus() != null) {
                predicates.add(cb.equal(root.get("approvalStatus"), request.getApprovalStatus()));
            }

            if (request.getDepartmentId() != null) {
                predicates
                        .add(cb.equal(root.get("requestStaff").get("department").get("id"), request.getDepartmentId()));
            }

            if (request.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fromDate"), request.getFromDate()));
            }

            if (request.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("toDate"), request.getToDate()));
            }

            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                String kw = "%" + request.getKeyword().toLowerCase() + "%";
                Predicate codeLike = cb.like(cb.lower(root.get("requestStaff").get("staffCode")), kw);
                Predicate nameLike = cb.like(cb.lower(root.get("requestStaff").get("displayName")), kw);
                predicates.add(cb.or(codeLike, nameLike));
            }

            predicates.add(cb.or(cb.isNull(root.get("isDeleted")), cb.equal(root.get("isDeleted"), false)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        int pageNum = request.getPageIndex() >= 1 ? request.getPageIndex() - 1 : 0;
        int size = request.getPageSize() > 0 ? request.getPageSize() : 10;

        Page<LeaveRequest> page = leaveRequestRepository.findAll(spec, PageRequest.of(pageNum, size));
        List<LeaveRequestDto> dtos = page.getContent().stream()
                .map(LeaveRequestDto::new)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, PageRequest.of(pageNum, size), page.getTotalElements());
    }

    @Override
    public LeaveRequestDto getById(UUID id) {
        return leaveRequestRepository.findById(id)
                .filter(entity -> entity.getIsDeleted() == null || !entity.getIsDeleted())
                .map(LeaveRequestDto::new)
                .orElse(null);
    }

    @Override
    public LeaveRequestDto create(LeaveRequestDto dto) {
        LeaveRequest entity = new LeaveRequest();
        mapDtoToEntity(dto, entity);

        // Mặc định ngày tạo đơn và trạng thái PENDING
        entity.setRequestDate(LocalDate.now());
        entity.setApprovalStatus(LeaveApprovalStatus.PENDING);

        // Tính tổng số ngày phép
        calculateDaysAndHours(entity);

        // Kiểm tra trùng lịch
        validateOverlap(entity);

        // Nếu là đơn nghỉ phép năm, kiểm tra xem có vượt quá định mức không
        if (entity.getLeaveType() == LeaveType.ANNUAL) {
            validateLeaveBalanceLimit(entity);
        }

        LeaveRequest saved = leaveRequestRepository.save(entity);
        return new LeaveRequestDto(saved);
    }

    @Override
    public LeaveRequestDto update(UUID id, LeaveRequestDto dto) {
        LeaveRequest entity = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nghỉ phép"));

        if (entity.getApprovalStatus() != LeaveApprovalStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể cập nhật đơn nghỉ phép ở trạng thái chờ duyệt (PENDING)");
        }

        mapDtoToEntity(dto, entity);
        calculateDaysAndHours(entity);
        validateOverlap(entity);

        if (entity.getLeaveType() == LeaveType.ANNUAL) {
            validateLeaveBalanceLimit(entity);
        }

        LeaveRequest saved = leaveRequestRepository.save(entity);
        return new LeaveRequestDto(saved);
    }

    @Override
    public void delete(UUID id) {
        LeaveRequest entity = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nghỉ phép"));

        // Đơn đã duyệt mà bị xóa (hoặc hủy) cần cập nhật lại Timesheet
        LeaveApprovalStatus oldStatus = entity.getApprovalStatus();

        entity.setIsDeleted(true);
        leaveRequestRepository.save(entity);

        if (oldStatus == LeaveApprovalStatus.APPROVED) {
            updateTimesheetsForLeaveRequest(entity);
        }
    }

    @Override
    public LeaveRequestDto approve(UUID id, String rejectReason) {
        LeaveRequest entity = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nghỉ phép"));

        if (entity.getApprovalStatus() == LeaveApprovalStatus.APPROVED) {
            return new LeaveRequestDto(entity);
        }

        // Kiểm tra lại số dư phép năm nếu là phép ANNUAL
        if (entity.getLeaveType() == LeaveType.ANNUAL) {
            validateLeaveBalanceLimit(entity);
        }

        entity.setApprovalStatus(LeaveApprovalStatus.APPROVED);
        entity.setRejectReason(rejectReason);
        LeaveRequest saved = leaveRequestRepository.save(entity);

        // Đồng bộ dữ liệu chấm công (Timesheet)
        updateTimesheetsForLeaveRequest(saved);

        // Gửi thông báo cho nhân viên
        try {
            if (saved.getRequestStaff() != null) {
                userRepository.findByStaffId(saved.getRequestStaff().getId()).ifPresent(user -> {
                    com.tlu.hrm.dto.request.NotificationDto noti = new com.tlu.hrm.dto.request.NotificationDto();
                    noti.setTitle("Đơn nghỉ phép đã được phê duyệt");
                    noti.setContent("Đơn xin nghỉ phép của bạn từ ngày " + saved.getFromDate() + " đến ngày " + saved.getToDate() + " đã được phê duyệt.");
                    noti.setNotificationType(com.tlu.hrm.enums.NotificationType.LEAVE);
                    noti.setTargetObjectId(saved.getId());
                    noti.setLinkUrl("my-leave");
                    noti.setIsGlobal(false);
                    notificationService.sendToUsers(noti, List.of(user.getUsername()));
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi gửi thông báo nghỉ phép: " + e.getMessage());
        }

        return new LeaveRequestDto(saved);
    }

    @Override
    public LeaveRequestDto reject(UUID id, String rejectReason) {
        LeaveRequest entity = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nghỉ phép"));

        LeaveApprovalStatus oldStatus = entity.getApprovalStatus();

        entity.setApprovalStatus(LeaveApprovalStatus.REJECTED);
        entity.setRejectReason(rejectReason);
        LeaveRequest saved = leaveRequestRepository.save(entity);

        // Nếu trước đó đơn đã được APPROVED, nay chuyển sang REJECTED thì cần tính toán
        // lại Timesheet để bỏ công phép
        if (oldStatus == LeaveApprovalStatus.APPROVED) {
            updateTimesheetsForLeaveRequest(saved);
        }

        // Gửi thông báo cho nhân viên
        try {
            if (saved.getRequestStaff() != null) {
                userRepository.findByStaffId(saved.getRequestStaff().getId()).ifPresent(user -> {
                    com.tlu.hrm.dto.request.NotificationDto noti = new com.tlu.hrm.dto.request.NotificationDto();
                    noti.setTitle("Đơn nghỉ phép đã bị từ chối");
                    noti.setContent("Đơn xin nghỉ phép của bạn từ ngày " + saved.getFromDate() + " đến ngày " + saved.getToDate() + " đã bị từ chối. Lý do: " + (rejectReason != null ? rejectReason : "Không có lý do cụ thể."));
                    noti.setNotificationType(com.tlu.hrm.enums.NotificationType.LEAVE);
                    noti.setTargetObjectId(saved.getId());
                    noti.setLinkUrl("my-leave");
                    noti.setIsGlobal(false);
                    notificationService.sendToUsers(noti, List.of(user.getUsername()));
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi gửi thông báo nghỉ phép: " + e.getMessage());
        }

        return new LeaveRequestDto(saved);
    }

    @Override
    public StaffAnnualLeaveBalanceDto getLeaveBalance(UUID staffId, int year) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));

        Double limit = staff.getAnnualLeave();
        if (limit == null) {
            limit = 12.0;
        }

        Double used = leaveRequestRepository.calculateUsedAnnualLeave(staffId, year);
        Double remaining = limit - used;

        String deptName = staff.getDepartment() != null ? staff.getDepartment().getName() : "";
        String positionName = staff.getPosition() != null ? staff.getPosition().getName() : "";

        return StaffAnnualLeaveBalanceDto.builder()
                .staffId(staff.getId())
                .staffName(staff.getDisplayName())
                .staffCode(staff.getStaffCode())
                .departmentName(deptName)
                .positionName(positionName)
                .year(year)
                .annualLeave(limit)
                .usedDays(used)
                .remainingDays(remaining)
                .build();
    }

    private void mapDtoToEntity(LeaveRequestDto dto, LeaveRequest entity) {
        if (dto.getRequestStaffId() != null) {
            Staff requestStaff = staffRepository.findById(dto.getRequestStaffId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên yêu cầu"));
            entity.setRequestStaff(requestStaff);
        }

        entity.setLeaveType(dto.getLeaveType());
        entity.setFromDate(dto.getFromDate());
        entity.setToDate(dto.getToDate());
        entity.setRequestReason(dto.getRequestReason());

        entity.setHalfDayLeave(dto.getHalfDayLeave() != null ? dto.getHalfDayLeave() : false);

        if (dto.getShiftWorkId() != null) {
            shiftWorkRepository.findById(dto.getShiftWorkId()).ifPresent(entity::setShiftWork);
        } else {
            entity.setShiftWork(null);
        }
    }

    private void calculateDaysAndHours(LeaveRequest request) {
        if (request.getFromDate() == null || request.getToDate() == null) {
            request.setTotalDays(0.0);
            return;
        }

        double totalDays = 0.0;
        LocalDate start = request.getFromDate();
        LocalDate end = request.getToDate();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            // Loại trừ ngày nghỉ cuối tuần (Thứ 7 & Chủ nhật)
            DayOfWeek day = date.getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                continue;
            }

            double dayValue = 1.0;
            if (request.getHalfDayLeave() != null && request.getHalfDayLeave() && start.equals(end)) {
                dayValue = 0.5;
            }
            totalDays += dayValue;
        }

        request.setTotalDays(totalDays);
    }

    private void validateOverlap(LeaveRequest request) {
        List<LeaveRequest> overlaps = leaveRequestRepository.findOverlappingRequests(
                request.getRequestStaff().getId(),
                request.getFromDate(),
                request.getToDate(),
                request.getId());
        if (!overlaps.isEmpty()) {
            throw new IllegalArgumentException("Nhân viên đã có đơn nghỉ phép trùng lặp trong khoảng thời gian này!");
        }
    }

    private void validateLeaveBalanceLimit(LeaveRequest request) {
        int year = request.getFromDate().getYear();
        Double used = leaveRequestRepository.calculateUsedAnnualLeave(request.getRequestStaff().getId(), year);
        Double limit = request.getRequestStaff().getAnnualLeave();
        if (limit == null) {
            limit = 12.0;
        }

        if (used + request.getTotalDays() > limit) {
            throw new IllegalArgumentException(String.format(
                    "Không đủ phép năm còn lại! Định mức: %.1f, Đã dùng: %.1f, Đăng ký mới: %.1f",
                    limit, used, request.getTotalDays()));
        }
    }

    private void updateTimesheetsForLeaveRequest(LeaveRequest request) {
        if (request.getRequestStaff() == null)
            return;
        UUID staffId = request.getRequestStaff().getId();
        LocalDate start = request.getFromDate();
        LocalDate end = request.getToDate();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                continue;
            }
            try {
                // Kích hoạt tính lại công cho ngày đó để đồng bộ phép
                timesheetService.calculateTimesheet(staffId, date);
            } catch (Exception e) {
                // Ghi log lỗi và bỏ qua để tránh rollback giao dịch phê duyệt phép
            }
        }
    }

    @Override
    public Page<StaffAnnualLeaveBalanceDto> getLeaveBalances(com.tlu.hrm.dto.search.SearchDto searchDto, int year) {
        List<Staff> filteredList = staffRepository.findAll().stream()
                .filter(staff -> staff.getIsDeleted() == null || !staff.getIsDeleted())
                .filter(staff -> {
                    if (searchDto != null) {
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
                        if (searchDto.getDepartmentId() != null) {
                            if (staff.getDepartment() == null
                                    || !staff.getDepartment().getId().equals(searchDto.getDepartmentId())) {
                                return false;
                            }
                        }
                        if (searchDto.getPositionId() != null) {
                            if (staff.getPosition() == null
                                    || !staff.getPosition().getId().equals(searchDto.getPositionId())) {
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

        List<StaffAnnualLeaveBalanceDto> pageContent = new ArrayList<>();
        if (fromIndex < total) {
            for (Staff staff : filteredList.subList(fromIndex, toIndex)) {
                Double limit = staff.getAnnualLeave();
                if (limit == null) {
                    limit = 12.0;
                }
                Double used = leaveRequestRepository.calculateUsedAnnualLeave(staff.getId(), year);
                Double remaining = limit - used;
                String deptName = staff.getDepartment() != null ? staff.getDepartment().getName() : "";
                String positionName = staff.getPosition() != null ? staff.getPosition().getName() : "";

                pageContent.add(StaffAnnualLeaveBalanceDto.builder()
                        .staffId(staff.getId())
                        .staffName(staff.getDisplayName())
                        .staffCode(staff.getStaffCode())
                        .departmentName(deptName)
                        .positionName(positionName)
                        .year(year)
                        .annualLeave(limit)
                        .usedDays(used)
                        .remainingDays(remaining)
                        .build());
            }
        }

        return new PageImpl<>(pageContent, PageRequest.of(pageNum, size), total);
    }
}
