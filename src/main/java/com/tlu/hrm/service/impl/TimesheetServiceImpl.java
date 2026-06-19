package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.TimesheetDetailDto;
import com.tlu.hrm.dto.request.TimesheetDto;
import com.tlu.hrm.dto.search.TimesheetSearchRequest;
import com.tlu.hrm.enums.CheckInOutType;
import com.tlu.hrm.enums.TimesheetStatus;
import com.tlu.hrm.enums.LeaveType;
import com.tlu.hrm.model.*;
import com.tlu.hrm.repository.*;
import com.tlu.hrm.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TimesheetServiceImpl implements TimesheetService {

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private PeriodRepository periodRepository;

    @Autowired
    private TimesheetDetailRepository timesheetDetailRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ShiftWorkRepository shiftWorkRepository;

    @Autowired
    private CheckInOutRecordRepository checkInOutRecordRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Override
    public Page<TimesheetDto> search(TimesheetSearchRequest request) {
        Specification<Timesheet> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getStaffId() != null) {
                predicates.add(cb.equal(root.get("staff").get("id"), request.getStaffId()));
            }

            if (request.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("staff").get("department").get("id"), request.getDepartmentId()));
            }

            if (request.getPeriodId() != null) {
                predicates.add(cb.equal(root.get("period").get("id"), request.getPeriodId()));
            }

            if (request.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("workingDate"), request.getFromDate()));
            }

            if (request.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("workingDate"), request.getToDate()));
            }

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                String kw = "%" + request.getKeyword().toLowerCase() + "%";
                Predicate codeLike = cb.like(cb.lower(root.get("staff").get("staffCode")), kw);
                Predicate nameLike = cb.like(cb.lower(root.get("staff").get("displayName")), kw);
                predicates.add(cb.or(codeLike, nameLike));
            }

            predicates.add(cb.or(cb.isNull(root.get("voided")), cb.equal(root.get("voided"), false)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        int pageNum = request.getPageIndex() >= 1 ? request.getPageIndex() - 1 : 0;
        int size = request.getPageSize() > 0 ? request.getPageSize() : 10;

        Page<Timesheet> page = timesheetRepository.findAll(spec, PageRequest.of(pageNum, size));
        List<TimesheetDto> dtos = page.getContent().stream()
                .map(TimesheetDto::new)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, PageRequest.of(pageNum, size), page.getTotalElements());
    }

    @Override
    public TimesheetDto getById(UUID id) {
        return timesheetRepository.findById(id)
                .filter(ts -> ts.getVoided() == null || !ts.getVoided())
                .map(TimesheetDto::new)
                .orElse(null);
    }

    @Override
    public TimesheetDto saveOrUpdate(TimesheetDto dto) {
        Timesheet entity;
        if (dto.getId() != null) {
            entity = timesheetRepository.findById(dto.getId()).orElse(new Timesheet());
        } else {
            entity = new Timesheet();
        }

        if (dto.getStaffId() != null) {
            staffRepository.findById(dto.getStaffId()).ifPresent(entity::setStaff);
        }
        entity.setWorkingDate(dto.getWorkingDate());
        entity.setTotalWorkRatio(dto.getTotalWorkRatio());
        if (dto.getStandardHours() != null) {
            entity.setStandardHours(dto.getStandardHours());
        }
        if (dto.getOvertimeHours() != null) {
            entity.setOvertimeHours(dto.getOvertimeHours());
        }
        entity.setStatus(dto.getStatus());
        entity.setNote(dto.getNote());

        if (dto.getPeriodId() != null) {
            periodRepository.findById(dto.getPeriodId()).ifPresent(entity::setPeriod);
        } else if (entity.getWorkingDate() != null) {
            periodRepository.findPeriodContainingDate(entity.getWorkingDate()).ifPresent(entity::setPeriod);
        }

        Timesheet saved = timesheetRepository.save(entity);
        return new TimesheetDto(saved);
    }

    @Override
    public boolean approve(UUID id, TimesheetStatus status, String note) {
        Optional<Timesheet> optional = timesheetRepository.findById(id);
        if (optional.isPresent()) {
            Timesheet entity = optional.get();
            entity.setStatus(status);
            if (note != null) {
                entity.setNote(note);
            }
            timesheetRepository.save(entity);
            return true;
        }
        return false;
    }

    @Override
    public List<TimesheetDto> getByStaffAndDateRange(UUID staffId, LocalDate start, LocalDate end) {
        return timesheetRepository.findByStaffIdAndWorkingDateBetweenOrderByWorkingDateAsc(staffId, start, end)
                .stream()
                .map(TimesheetDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public void calculateTimesheet(UUID staffId, LocalDate date) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));

        // Tìm bảng công của ngày đó hoặc khởi tạo mới
        Timesheet timesheet = timesheetRepository.findByStaffIdAndWorkingDate(staffId, date)
                .orElseGet(() -> {
                    Timesheet ts = new Timesheet();
                    ts.setStaff(staff);
                    ts.setWorkingDate(date);
                    return ts;
                });

        if (timesheet.getPeriod() == null && date != null) {
            periodRepository.findPeriodContainingDate(date).ifPresent(timesheet::setPeriod);
        }

        // Tích hợp Đơn nghỉ phép đã duyệt
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedLeaveRequestsOnDate(staffId, date);
        if (!approvedLeaves.isEmpty()) {
            LeaveRequest leave = approvedLeaves.get(0);
            boolean isPaid = leave.getLeaveType() != LeaveType.UNPAID;

            if (leave.getHalfDayLeave() != null && leave.getHalfDayLeave()) {
                // Nghỉ nửa ngày: Tạo 1 chi tiết công nghỉ phép và quét log cho ca còn lại
                timesheet.getDetails().clear();
                
                ShiftWork offShift = leave.getShiftWorkStart();
                if (offShift == null) {
                    offShift = shiftWorkRepository.findByCode("CA_SANG").orElse(null);
                }
                
                if (offShift != null) {
                    TimesheetDetail leaveDetail = new TimesheetDetail();
                    leaveDetail.setTimesheet(timesheet);
                    leaveDetail.setShift(offShift);
                    leaveDetail.setCheckInTime(date.atTime(offShift.getStartTime()));
                    leaveDetail.setCheckOutTime(date.atTime(offShift.getEndTime()));
                    leaveDetail.setWorkRatio(isPaid ? 0.5 : 0.0);
                    leaveDetail.setLateMinutes(0);
                    leaveDetail.setEarlyMinutes(0);
                    timesheet.getDetails().add(leaveDetail);
                    
                    // Quét log ca còn lại (ca đi làm thực tế)
                    LocalDateTime startOfDay = date.atStartOfDay();
                    LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);
                    List<CheckInOutRecord> rawLogs = checkInOutRecordRepository
                            .findByStaffIdAndRecordTimeBetweenOrderByRecordTimeAsc(staffId, startOfDay, endOfDay);
                    
                    String workingShiftCode = "CA_CHIEU".equals(offShift.getCode()) ? "CA_SANG" : "CA_CHIEU";
                    ShiftWork workingShift = shiftWorkRepository.findByCode(workingShiftCode).orElse(null);
                    
                    if (workingShift != null && !rawLogs.isEmpty()) {
                        List<CheckInOutRecord> workingInLogs = new ArrayList<>();
                        List<CheckInOutRecord> workingOutLogs = new ArrayList<>();
                        for (CheckInOutRecord log : rawLogs) {
                            if (log.getShift() != null && log.getShift().getCode().equals(workingShiftCode)) {
                                if (log.getRecordType() == CheckInOutType.CHECK_IN) {
                                    workingInLogs.add(log);
                                } else {
                                    workingOutLogs.add(log);
                                }
                            } else if (log.getShift() == null) {
                                LocalTime t = log.getRecordTime().toLocalTime();
                                if (log.getRecordType() == CheckInOutType.CHECK_IN) {
                                    if ("CA_SANG".equals(workingShiftCode) && !t.isAfter(LocalTime.of(11, 30))) {
                                        workingInLogs.add(log);
                                    } else if ("CA_CHIEU".equals(workingShiftCode) && t.isAfter(LocalTime.of(12, 0))) {
                                        workingInLogs.add(log);
                                    }
                                } else {
                                    if ("CA_SANG".equals(workingShiftCode) && t.isBefore(LocalTime.of(13, 0))) {
                                        workingOutLogs.add(log);
                                    } else if ("CA_CHIEU".equals(workingShiftCode) && t.isAfter(LocalTime.of(16, 0))) {
                                        workingOutLogs.add(log);
                                    }
                                }
                            }
                        }
                        
                        if (!workingInLogs.isEmpty() || !workingOutLogs.isEmpty()) {
                            TimesheetDetail workingDetail = new TimesheetDetail();
                            workingDetail.setTimesheet(timesheet);
                            workingDetail.setShift(workingShift);
                            CheckInOutRecord inRec = workingInLogs.isEmpty() ? null : workingInLogs.get(0);
                            CheckInOutRecord outRec = workingOutLogs.isEmpty() ? null : workingOutLogs.get(workingOutLogs.size() - 1);
                            mapCheckInOut(workingDetail, inRec, outRec, workingShift.getStartTime(), workingShift.getEndTime(), 0.5);
                            timesheet.getDetails().add(workingDetail);
                        }
                    }
                    
                    double totalRatio = 0.0;
                    for (TimesheetDetail d : timesheet.getDetails()) {
                        totalRatio += d.getWorkRatio() != null ? d.getWorkRatio() : 0.0;
                    }
                    timesheet.setTotalWorkRatio(totalRatio);
                    timesheet.setStandardHours(calculateStandardHours(timesheet.getDetails()));
                    timesheet.setOvertimeHours(calculateOvertimeHours(timesheet.getDetails()));
                    timesheet.setStatus(TimesheetStatus.APPROVED);
                    timesheet.setNote("Nghỉ nửa ngày: " + leave.getLeaveType().name() + " (" + leave.getRequestReason() + ")");
                    timesheetRepository.save(timesheet);
                    return;
                }
            } else {
                // Nghỉ cả ngày
                timesheet.getDetails().clear();
                timesheet.setTotalWorkRatio(isPaid ? 1.0 : 0.0);
                timesheet.setStandardHours(isPaid ? 8.0 : 0.0);
                timesheet.setOvertimeHours(0.0);
                
                TimesheetDetail leaveDetail = new TimesheetDetail();
                leaveDetail.setTimesheet(timesheet);
                ShiftWork fullDayShift = shiftWorkRepository.findByCode("CA_CA_NGAY").orElse(null);
                leaveDetail.setShift(fullDayShift);
                leaveDetail.setWorkRatio(isPaid ? 1.0 : 0.0);
                leaveDetail.setLateMinutes(0);
                leaveDetail.setEarlyMinutes(0);
                timesheet.getDetails().add(leaveDetail);
                
                timesheet.setStatus(TimesheetStatus.APPROVED);
                timesheet.setNote("Nghỉ cả ngày: " + leave.getLeaveType().name() + " (" + leave.getRequestReason() + ")");
                timesheetRepository.save(timesheet);
                return;
            }
        }

        // Nếu bảng công đã được duyệt hoặc từ chối, giữ nguyên trạng thái bảng công
        if (timesheet.getStatus() == TimesheetStatus.APPROVED || timesheet.getStatus() == TimesheetStatus.REJECTED) {
            // Không thay đổi trạng thái
        } else if (timesheet.getStatus() == null || timesheet.getStatus() == TimesheetStatus.DRAFT) {
            timesheet.setStatus(TimesheetStatus.SUBMITTED);
        }

        // Fetch 4 ca làm việc mặc định
        ShiftWork morningShift = shiftWorkRepository.findByCode("CA_SANG").orElse(null);
        ShiftWork afternoonShift = shiftWorkRepository.findByCode("CA_CHIEU").orElse(null);
        ShiftWork fullDayShift = shiftWorkRepository.findByCode("CA_CA_NGAY").orElse(null);
        ShiftWork otShift = shiftWorkRepository.findByCode("CA_OT").orElse(null);

        // 1. Trường hợp nhân viên không cần chấm công (skipTimekeeping = true)
        if (staff.getSkipTimekeeping() != null && staff.getSkipTimekeeping()) {
            timesheet.getDetails().clear();
            if (fullDayShift != null) {
                TimesheetDetail detail = new TimesheetDetail();
                detail.setTimesheet(timesheet);
                detail.setShift(fullDayShift);
                detail.setCheckInTime(date.atTime(8, 0));
                detail.setCheckOutTime(date.atTime(17, 30));
                detail.setLateMinutes(0);
                detail.setEarlyMinutes(0);
                detail.setWorkRatio(1.0);
                timesheet.getDetails().add(detail);
            }
            timesheet.setTotalWorkRatio(1.0);
            double stdHours = calculateStandardHours(timesheet.getDetails());
            double otHours = calculateOvertimeHours(timesheet.getDetails());
            timesheet.setStandardHours(stdHours);
            timesheet.setOvertimeHours(otHours);
            timesheetRepository.save(timesheet);
            return;
        }

        // 2. Lấy danh sách lượt quẹt thô trong ngày
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);
        List<CheckInOutRecord> rawLogs = checkInOutRecordRepository
                .findByStaffIdAndRecordTimeBetweenOrderByRecordTimeAsc(staffId, startOfDay, endOfDay);

        timesheet.getDetails().clear();

        if (rawLogs.isEmpty()) {
            timesheet.setTotalWorkRatio(0.0);
            timesheet.setStandardHours(0.0);
            timesheet.setOvertimeHours(0.0);
            timesheetRepository.save(timesheet);
            return;
        }

        // Định nghĩa các dải giờ cho mốc quẹt (không còn khe hở nhờ phân loại theo
        // CHECK_IN/CHECK_OUT)
        LocalTime morningInStart = LocalTime.of(6, 0);
        LocalTime morningInEnd = LocalTime.of(11, 30);
        LocalTime lunchStart = LocalTime.of(11, 30);
        LocalTime lunchEnd = LocalTime.of(13, 30);
        LocalTime afternoonInStart = LocalTime.of(13, 0);
        LocalTime afternoonInEnd = LocalTime.of(16, 30);
        LocalTime afternoonOutStart = LocalTime.of(16, 30);
        LocalTime afternoonOutEnd = LocalTime.of(18, 30);
        LocalTime otInStart = LocalTime.of(18, 0);
        LocalTime otInEnd = LocalTime.of(19, 30);
        LocalTime otOutStart = LocalTime.of(19, 0);
        LocalTime otOutEnd = LocalTime.of(23, 59);

        // Phân loại lượt quẹt thô theo dải giờ và loại ghi nhận (CHECK_IN / CHECK_OUT)
        List<CheckInOutRecord> morningInLogs = new java.util.ArrayList<>();
        List<CheckInOutRecord> lunchLogs = new java.util.ArrayList<>();
        List<CheckInOutRecord> afternoonInLogs = new java.util.ArrayList<>();
        List<CheckInOutRecord> afternoonOutLogs = new java.util.ArrayList<>();
        List<CheckInOutRecord> otInLogs = new java.util.ArrayList<>();
        List<CheckInOutRecord> otOutLogs = new java.util.ArrayList<>();

        for (CheckInOutRecord log : rawLogs) {
            ShiftWork logShift = log.getShift();
            CheckInOutType type = log.getRecordType();
            
            if (logShift != null) {
                String code = logShift.getCode();
                if ("CA_SANG".equals(code)) {
                    if (type == CheckInOutType.CHECK_IN) {
                        morningInLogs.add(log);
                    } else if (type == CheckInOutType.CHECK_OUT) {
                        lunchLogs.add(log);
                    }
                } else if ("CA_CHIEU".equals(code)) {
                    if (type == CheckInOutType.CHECK_IN) {
                        afternoonInLogs.add(log);
                    } else if (type == CheckInOutType.CHECK_OUT) {
                        afternoonOutLogs.add(log);
                    }
                } else if ("CA_CA_NGAY".equals(code)) {
                    if (type == CheckInOutType.CHECK_IN) {
                        morningInLogs.add(log);
                    } else if (type == CheckInOutType.CHECK_OUT) {
                        afternoonOutLogs.add(log);
                    }
                } else if (isOvertimeShiftCode(code)) {
                    if (type == CheckInOutType.CHECK_IN) {
                        otInLogs.add(log);
                    } else if (type == CheckInOutType.CHECK_OUT) {
                        otOutLogs.add(log);
                    }
                }
            } else {
                // Fallback theo dải giờ nếu log không gắn với ca cụ thể
                LocalTime t = log.getRecordTime().toLocalTime();
                if (type == CheckInOutType.CHECK_IN) {
                    if (!t.isBefore(morningInStart) && !t.isAfter(morningInEnd)) {
                        morningInLogs.add(log);
                    } else if (!t.isBefore(afternoonInStart) && !t.isAfter(afternoonInEnd)) {
                        afternoonInLogs.add(log);
                    } else if (!t.isBefore(otInStart) && !t.isAfter(otInEnd)) {
                        otInLogs.add(log);
                    }
                } else if (type == CheckInOutType.CHECK_OUT) {
                    if (!t.isBefore(lunchStart) && !t.isAfter(lunchEnd)) {
                        lunchLogs.add(log);
                    } else if (!t.isBefore(afternoonOutStart) && !t.isAfter(afternoonOutEnd)) {
                        afternoonOutLogs.add(log);
                    } else if (!t.isBefore(otOutStart) && !t.isAfter(otOutEnd)) {
                        otOutLogs.add(log);
                    }
                }
            }
        }

        double totalRatio = 0.0;

        // A. Xét ca OT (Tăng ca) độc lập
        if (!otInLogs.isEmpty() || !otOutLogs.isEmpty()) {
            TimesheetDetail otDetail = new TimesheetDetail();
            otDetail.setTimesheet(timesheet);

            CheckInOutRecord inRec = otInLogs.isEmpty() ? null : otInLogs.get(0);
            CheckInOutRecord outRec = otOutLogs.isEmpty() ? null : otOutLogs.get(otOutLogs.size() - 1);

            ShiftWork actualOtShift = otShift;
            if (inRec != null && inRec.getShift() != null) {
                actualOtShift = inRec.getShift();
            } else if (outRec != null && outRec.getShift() != null) {
                actualOtShift = outRec.getShift();
            }
            otDetail.setShift(actualOtShift);

            if (inRec != null) {
                otDetail.setCheckInTime(inRec.getRecordTime());
                otDetail.setIpCheckIn(inRec.getIpAddress());
                otDetail.setPhotoCheckInUrl(inRec.getPhotoUrl());
            }
            if (outRec != null) {
                otDetail.setCheckOutTime(outRec.getRecordTime());
                otDetail.setIpCheckOut(outRec.getIpAddress());
                otDetail.setPhotoCheckOutUrl(outRec.getPhotoUrl());
            }

            if (inRec != null && outRec != null) {
                double ratio = (actualOtShift != null && actualOtShift.getWorkRatio() != null) ? actualOtShift.getWorkRatio() : 0.0;
                otDetail.setWorkRatio(ratio);
            } else {
                otDetail.setWorkRatio(0.0);
            }
            timesheet.getDetails().add(otDetail);
            totalRatio += otDetail.getWorkRatio();
        }

        // B. Xét ca ban ngày: Ca Cả Ngày vs. Ca Sáng/Chiều
        boolean hasLunchQuets = !lunchLogs.isEmpty();
        
        // Kiểm tra xem ngày hôm nay có lượt quẹt nào chỉ định đích danh ca Sáng hoặc ca Chiều không
        boolean hasSpecificHalfDayShift = false;
        for (CheckInOutRecord log : rawLogs) {
            if (log.getShift() != null) {
                String code = log.getShift().getCode();
                if ("CA_SANG".equals(code) || "CA_CHIEU".equals(code)) {
                    hasSpecificHalfDayShift = true;
                    break;
                }
            }
        }

        // Quyết định xem có tách ca Sáng/Chiều hay không:
        // 1. Có quẹt nghỉ trưa (hasLunchQuets)
        // 2. Hoặc có lượt quẹt chọn đích danh ca Sáng/Chiều (hasSpecificHalfDayShift)
        boolean shouldSplit = hasLunchQuets || hasSpecificHalfDayShift;

        if (shouldSplit) {
            // Chia làm Ca Sáng + Ca Chiều riêng biệt
            // Ca Sáng
            if (morningShift != null && (!morningInLogs.isEmpty() || !lunchLogs.isEmpty())) {
                TimesheetDetail morningDetail = new TimesheetDetail();
                morningDetail.setTimesheet(timesheet);
                morningDetail.setShift(morningShift);

                CheckInOutRecord inRec = morningInLogs.isEmpty() ? null : morningInLogs.get(0);
                CheckInOutRecord outRec = lunchLogs.isEmpty() ? null : lunchLogs.get(lunchLogs.size() - 1);

                mapCheckInOut(morningDetail, inRec, outRec, morningShift.getStartTime(), morningShift.getEndTime(),
                        0.5);
                timesheet.getDetails().add(morningDetail);
                totalRatio += morningDetail.getWorkRatio();
            }

            // Ca Chiều
            if (afternoonShift != null && (!afternoonInLogs.isEmpty() || !afternoonOutLogs.isEmpty())) {
                TimesheetDetail afternoonDetail = new TimesheetDetail();
                afternoonDetail.setTimesheet(timesheet);
                afternoonDetail.setShift(afternoonShift);

                // Cải tiến: Nếu không quẹt check-in chiều (afternoonInLogs rỗng) nhưng có quẹt nghỉ trưa (lunchLogs),
                // ta lấy log quẹt nghỉ trưa cuối cùng làm check-in chiều để tránh nhân viên bị mất công ca chiều khi làm cả ngày
                CheckInOutRecord inRec = afternoonInLogs.isEmpty() 
                        ? (lunchLogs.isEmpty() ? null : lunchLogs.get(lunchLogs.size() - 1)) 
                        : afternoonInLogs.get(0);
                CheckInOutRecord outRec = afternoonOutLogs.isEmpty() ? null
                        : afternoonOutLogs.get(afternoonOutLogs.size() - 1);

                mapCheckInOut(afternoonDetail, inRec, outRec, afternoonShift.getStartTime(),
                        afternoonShift.getEndTime(), 0.5);
                timesheet.getDetails().add(afternoonDetail);
                totalRatio += afternoonDetail.getWorkRatio();
            }
        } else {
            // Chấm Ca Cả Ngày (Hành chính)
            if (fullDayShift != null && (!morningInLogs.isEmpty() || !afternoonOutLogs.isEmpty())) {
                TimesheetDetail fullDayDetail = new TimesheetDetail();
                fullDayDetail.setTimesheet(timesheet);
                fullDayDetail.setShift(fullDayShift);

                CheckInOutRecord inRec = morningInLogs.isEmpty() ? null : morningInLogs.get(0);
                CheckInOutRecord outRec = afternoonOutLogs.isEmpty() ? null
                        : afternoonOutLogs.get(afternoonOutLogs.size() - 1);

                mapCheckInOut(fullDayDetail, inRec, outRec, fullDayShift.getStartTime(), fullDayShift.getEndTime(),
                        1.0);
                timesheet.getDetails().add(fullDayDetail);
                totalRatio += fullDayDetail.getWorkRatio();
            }
        }

        timesheet.setTotalWorkRatio(totalRatio);
        double stdHours = calculateStandardHours(timesheet.getDetails());
        double otHours = calculateOvertimeHours(timesheet.getDetails());
        timesheet.setStandardHours(stdHours);
        timesheet.setOvertimeHours(otHours);
        timesheetRepository.save(timesheet);
    }

    private List<CheckInOutRecord> filterLogs(List<CheckInOutRecord> logs, LocalTime start, LocalTime end,
            CheckInOutType type) {
        return logs.stream()
                .filter(l -> {
                    LocalTime t = l.getRecordTime().toLocalTime();
                    boolean timeMatch = !t.isBefore(start) && !t.isAfter(end);
                    return timeMatch && l.getRecordType() == type;
                })
                .collect(Collectors.toList());
    }

    private void mapCheckInOut(TimesheetDetail detail, CheckInOutRecord inRec, CheckInOutRecord outRec,
            LocalTime shiftStart, LocalTime shiftEnd, double ratio) {
        if (inRec != null) {
            detail.setCheckInTime(inRec.getRecordTime());
            detail.setIpCheckIn(inRec.getIpAddress());
            detail.setPhotoCheckInUrl(inRec.getPhotoUrl());

            LocalTime checkInLocal = inRec.getRecordTime().toLocalTime();
            if (checkInLocal.isAfter(shiftStart)) {
                long minutes = Duration.between(shiftStart, checkInLocal).toMinutes();
                detail.setLateMinutes((int) minutes);
            } else {
                detail.setLateMinutes(0);
            }
        }

        if (outRec != null) {
            detail.setCheckOutTime(outRec.getRecordTime());
            detail.setIpCheckOut(outRec.getIpAddress());
            detail.setPhotoCheckOutUrl(outRec.getPhotoUrl());

            LocalTime checkOutLocal = outRec.getRecordTime().toLocalTime();
            if (checkOutLocal.isBefore(shiftEnd)) {
                long minutes = Duration.between(checkOutLocal, shiftEnd).toMinutes();
                detail.setEarlyMinutes((int) minutes);
            } else {
                detail.setEarlyMinutes(0);
            }
        }

        if (inRec != null && outRec != null) {
            detail.setWorkRatio(ratio);
        } else {
            detail.setWorkRatio(0.0);
        }
    }

    private boolean isOvertimeShift(ShiftWork shift) {
        if (shift == null) {
            return false;
        }
        return isOvertimeShiftCode(shift.getCode());
    }

    private boolean isOvertimeShiftCode(String code) {
        if (code == null) {
            return false;
        }
        String upper = code.toUpperCase();
        return upper.contains("OT") || upper.contains("TANG_CA") || upper.contains("OVERTIME");
    }

    private double calculateStandardHours(List<TimesheetDetail> details) {
        double total = 0.0;
        if (details == null) {
            return total;
        }
        for (TimesheetDetail detail : details) {
            if (detail.getShift() != null && !isOvertimeShift(detail.getShift())) {
                total += calculateDetailHours(detail);
            }
        }
        return total;
    }

    private double calculateOvertimeHours(List<TimesheetDetail> details) {
        double total = 0.0;
        if (details == null) {
            return total;
        }
        for (TimesheetDetail detail : details) {
            if (detail.getShift() != null && isOvertimeShift(detail.getShift())) {
                total += calculateDetailHours(detail);
            }
        }
        return total;
    }

    private double calculateDetailHours(TimesheetDetail detail) {
        if (detail == null || detail.getCheckInTime() == null || detail.getCheckOutTime() == null) {
            return 0.0;
        }
        LocalDateTime in = detail.getCheckInTime();
        LocalDateTime out = detail.getCheckOutTime();
        if (!out.isAfter(in)) {
            return 0.0;
        }
        
        ShiftWork shift = detail.getShift();
        if (shift == null) {
            long minutes = Duration.between(in, out).toMinutes();
            return minutes > 0 ? minutes / 60.0 : 0.0;
        }
        
        // Giới hạn giờ vào/ra của tất cả các ca theo khung giờ của ca đó
        LocalTime shiftStart = shift.getStartTime();
        LocalTime shiftEnd = shift.getEndTime();
        
        LocalTime checkInTime = in.toLocalTime();
        LocalTime checkOutTime = out.toLocalTime();
        
        if (checkInTime.isBefore(shiftStart)) {
            checkInTime = shiftStart;
        }
        if (checkOutTime.isAfter(shiftEnd)) {
            checkOutTime = shiftEnd;
        }
        
        in = in.with(checkInTime);
        out = out.with(checkOutTime);
        
        if (!out.isAfter(in)) {
            return 0.0;
        }
        
        long minutes = Duration.between(in, out).toMinutes();
        
        // Subtract lunch break if it's a full-day shift
        if ("CA_CA_NGAY".equals(shift.getCode())) {
            LocalTime lunchStart = LocalTime.of(12, 0);
            LocalTime lunchEnd = LocalTime.of(13, 30);
            
            if (checkInTime.isBefore(lunchEnd) && checkOutTime.isAfter(lunchStart)) {
                LocalTime intersectStart = checkInTime.isAfter(lunchStart) ? checkInTime : lunchStart;
                LocalTime intersectEnd = checkOutTime.isBefore(lunchEnd) ? checkOutTime : lunchEnd;
                long lunchOverlapMins = Duration.between(intersectStart, intersectEnd).toMinutes();
                if (lunchOverlapMins > 0) {
                    minutes -= lunchOverlapMins;
                }
            }
        }
        return minutes > 0 ? minutes / 60.0 : 0.0;
    }
}
