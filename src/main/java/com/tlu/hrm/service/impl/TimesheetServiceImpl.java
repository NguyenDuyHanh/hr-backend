package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.HolidayDto;
import com.tlu.hrm.dto.request.TimesheetDetailDto;
import com.tlu.hrm.dto.request.TimesheetDto;
import com.tlu.hrm.dto.search.TimesheetSearchRequest;
import com.tlu.hrm.enums.CheckInOutType;
import com.tlu.hrm.enums.TimesheetStatus;
import com.tlu.hrm.enums.LeaveType;
import com.tlu.hrm.model.*;
import com.tlu.hrm.repository.*;
import com.tlu.hrm.service.HolidayService;
import com.tlu.hrm.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.tlu.hrm.utils.ExcelUtil;
import com.tlu.hrm.utils.DateTimeUtils;

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

    @Autowired
    private HolidayService holidayService;

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
                Optional<Period> periodOpt = periodRepository.findById(request.getPeriodId());
                if (periodOpt.isPresent()) {
                    Period period = periodOpt.get();
                    predicates.add(cb.between(root.get("workingDate"), period.getFromDate(), period.getToDate()));
                } else {
                    predicates.add(cb.disjunction());
                }
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

            predicates.add(cb.or(cb.isNull(root.get("isDeleted")), cb.equal(root.get("isDeleted"), false)));

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
                .filter(ts -> ts.getIsDeleted() == null || !ts.getIsDeleted())
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

        // Tích hợp Đơn nghỉ phép đã duyệt
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedLeaveRequestsOnDate(staffId, date);
        if (!approvedLeaves.isEmpty()) {
            LeaveRequest leave = approvedLeaves.get(0);
            boolean isPaid = leave.getLeaveType() != LeaveType.UNPAID;

            if (leave.getHalfDayLeave() != null && leave.getHalfDayLeave()) {
                // Nghỉ nửa ngày: Tạo 1 chi tiết công nghỉ phép và quét log cho ca còn lại
                timesheet.getDetails().clear();

                ShiftWork offShift = leave.getShiftWork();
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
                            } else if (log.getShift() == null || "CA_CA_NGAY".equals(log.getShift().getCode())) {
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
                            CheckInOutRecord outRec = workingOutLogs.isEmpty() ? null
                                    : workingOutLogs.get(workingOutLogs.size() - 1);
                            mapCheckInOut(workingDetail, inRec, outRec, workingShift.getStartTime(),
                                    workingShift.getEndTime(), 0.5);
                            timesheet.getDetails().add(workingDetail);
                        }
                    }

                    double totalRatio = 0.0;
                    for (TimesheetDetail d : timesheet.getDetails()) {
                        totalRatio += d.getWorkRatio() != null ? d.getWorkRatio() : 0.0;
                    }
                    double stdHours = calculateStandardHours(timesheet.getDetails());
                    double otHours = calculateOvertimeHours(timesheet.getDetails());
                    finalizeTimesheetHours(timesheet, date, totalRatio, stdHours, otHours);
                    timesheet.setStatus(TimesheetStatus.APPROVED);
                    timesheet.setNote(
                            "Nghỉ nửa ngày: " + leave.getLeaveType().name() + " (" + leave.getRequestReason() + ")");
                    timesheetRepository.save(timesheet);
                    return;
                }
            } else {
                // Nghỉ cả ngày
                timesheet.getDetails().clear();
                timesheet.setTotalWorkRatio(isPaid ? 1.0 : 0.0);
                timesheet.setStandardHours(isPaid ? 8.0 : 0.0);
                timesheet.setOvertimeHours(0.0);
                timesheet.setWeekendOvertimeHours(0.0);
                timesheet.setHolidayOvertimeHours(0.0);

                TimesheetDetail leaveDetail = new TimesheetDetail();
                leaveDetail.setTimesheet(timesheet);
                ShiftWork fullDayShift = shiftWorkRepository.findByCode("CA_CA_NGAY").orElse(null);
                leaveDetail.setShift(fullDayShift);
                if (fullDayShift != null) {
                    leaveDetail.setCheckInTime(date.atTime(fullDayShift.getStartTime()));
                    leaveDetail.setCheckOutTime(date.atTime(fullDayShift.getEndTime()));
                }
                leaveDetail.setWorkRatio(isPaid ? 1.0 : 0.0);
                leaveDetail.setLateMinutes(0);
                leaveDetail.setEarlyMinutes(0);
                timesheet.getDetails().add(leaveDetail);

                timesheet.setStatus(TimesheetStatus.APPROVED);
                timesheet.setNote(
                        "Nghỉ cả ngày: " + leave.getLeaveType().name() + " (" + leave.getRequestReason() + ")");
                timesheetRepository.save(timesheet);
                return;
            }
        }

        // Nếu bảng công đã được duyệt hoặc từ chối, giữ nguyên trạng thái bảng công
        if (timesheet.getStatus() == TimesheetStatus.APPROVED || timesheet.getStatus() == TimesheetStatus.REJECTED) {
            // Không thay đổi trạng thái
        } else if (timesheet.getStatus() == null) {
            timesheet.setStatus(TimesheetStatus.SUBMITTED);
        }

        // Fetch 4 ca làm việc mặc định
        ShiftWork morningShift = shiftWorkRepository.findByCode("CA_SANG").orElse(null);
        ShiftWork afternoonShift = shiftWorkRepository.findByCode("CA_CHIEU").orElse(null);
        ShiftWork fullDayShift = shiftWorkRepository.findByCode("CA_CA_NGAY").orElse(null);
        ShiftWork otShift = shiftWorkRepository.findByCode("CA_OT").orElse(null);

        // 2. Lấy danh sách lượt chấm công thô trong ngày
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);
        List<CheckInOutRecord> rawLogs = checkInOutRecordRepository
                .findByStaffIdAndRecordTimeBetweenOrderByRecordTimeAsc(staffId, startOfDay, endOfDay);

        timesheet.getDetails().clear();

        if (rawLogs.isEmpty()) {
            if (holidayService.isHoliday(date)) {
                timesheet.setTotalWorkRatio(1.0);
                timesheet.setStandardHours(8.0);
                timesheet.setNote("Nghỉ lễ hưởng nguyên lương");
            } else {
                timesheet.setTotalWorkRatio(0.0);
                timesheet.setStandardHours(0.0);
            }
            timesheet.setOvertimeHours(0.0);
            timesheet.setWeekendOvertimeHours(0.0);
            timesheet.setHolidayOvertimeHours(0.0);
            timesheetRepository.save(timesheet);
            return;
        }

        // Phân loại lượt chấm công thô theo dải giờ và loại ghi nhận (CHECK_IN /
        // CHECK_OUT)
        List<CheckInOutRecord> morningInLogs = new java.util.ArrayList<>();
        List<CheckInOutRecord> morningOutLogs = new java.util.ArrayList<>();
        List<CheckInOutRecord> afternoonInLogs = new java.util.ArrayList<>();
        List<CheckInOutRecord> afternoonOutLogs = new java.util.ArrayList<>();
        List<CheckInOutRecord> otInLogs = new java.util.ArrayList<>();
        List<CheckInOutRecord> otOutLogs = new java.util.ArrayList<>();

        for (CheckInOutRecord log : rawLogs) {
            ShiftWork logShift = log.getShift();
            CheckInOutType type = log.getRecordType();

            if (logShift != null && type != null) {
                String code = logShift.getCode();
                if ("CA_SANG".equals(code)) {
                    if (type == CheckInOutType.CHECK_IN) {
                        morningInLogs.add(log);
                    } else if (type == CheckInOutType.CHECK_OUT) {
                        morningOutLogs.add(log);
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
                double ratio = (actualOtShift != null && actualOtShift.getWorkRatio() != null)
                        ? actualOtShift.getWorkRatio()
                        : 0.0;
                otDetail.setWorkRatio(ratio);
            } else {
                otDetail.setWorkRatio(0.0);
            }
            timesheet.getDetails().add(otDetail);
            totalRatio += otDetail.getWorkRatio();
        }

        // B. Xét ca ban ngày: Ca Cả Ngày vs. Ca Sáng/Chiều

        // Kiểm tra xem ngày hôm nay có lượt chấm công nào chỉ định đích danh ca Sáng
        // hoặc ca Chiều không
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
        // Chỉ tách khi có lượt chấm công chọn đích danh ca Sáng/Chiều (hasSpecificHalfDayShift)
        boolean shouldSplit = hasSpecificHalfDayShift;

        if (shouldSplit) {
            // Chia làm Ca Sáng + Ca Chiều riêng biệt
            // Ca Sáng
            if (morningShift != null && (!morningInLogs.isEmpty() || !morningOutLogs.isEmpty())) {
                TimesheetDetail morningDetail = new TimesheetDetail();
                morningDetail.setTimesheet(timesheet);
                morningDetail.setShift(morningShift);

                CheckInOutRecord inRec = morningInLogs.isEmpty() ? null : morningInLogs.get(0);
                CheckInOutRecord outRec = morningOutLogs.isEmpty() ? null
                        : morningOutLogs.get(morningOutLogs.size() - 1);

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

                // Cải tiến: Nếu không chấm công check-in chiều (afternoonInLogs rỗng) nhưng có
                // chấm công
                // nghỉ trưa (morningOutLogs),
                // ta lấy log chấm công nghỉ trưa cuối cùng làm check-in chiều để tránh nhân
                // viên bị
                // mất công ca chiều khi làm cả ngày
                CheckInOutRecord inRec = afternoonInLogs.isEmpty()
                        ? (morningOutLogs.isEmpty() ? null : morningOutLogs.get(morningOutLogs.size() - 1))
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

        double stdHours = calculateStandardHours(timesheet.getDetails());
        double otHours = calculateOvertimeHours(timesheet.getDetails());
        finalizeTimesheetHours(timesheet, date, totalRatio, stdHours, otHours);
        timesheetRepository.save(timesheet);
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

    // isHoliday() đã được chuyển sang HolidayService - query từ bảng tbl_holiday

    private void finalizeTimesheetHours(Timesheet timesheet, LocalDate date, double totalRatio, double stdHours,
            double otHours) {
        boolean isHoliday = holidayService.isHoliday(date);
        boolean isWeekend = (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY);

        if (isHoliday) {
            double totalOt = stdHours + otHours;
            timesheet.setTotalWorkRatio(1.0); // Holiday pay (x1.0 standard pay)
            timesheet.setStandardHours(0.0);
            timesheet.setHolidayOvertimeHours(totalOt); // Work hours paid at x3.0
            timesheet.setWeekendOvertimeHours(0.0);
            timesheet.setOvertimeHours(0.0);
        } else if (isWeekend) {
            double totalOt = stdHours + otHours;
            timesheet.setTotalWorkRatio(0.0); // Weekend rest day, no standard day pay (x1.0)
            timesheet.setStandardHours(0.0);
            timesheet.setHolidayOvertimeHours(0.0);
            timesheet.setWeekendOvertimeHours(totalOt); // Work hours paid at x2.0
            timesheet.setOvertimeHours(0.0);

            // Set workRatio of details to 0.0 for weekends
            if (timesheet.getDetails() != null) {
                for (TimesheetDetail d : timesheet.getDetails()) {
                    d.setWorkRatio(0.0);
                }
            }
        } else {
            timesheet.setTotalWorkRatio(totalRatio);
            timesheet.setStandardHours(stdHours);
            timesheet.setHolidayOvertimeHours(0.0);
            timesheet.setWeekendOvertimeHours(0.0);
            timesheet.setOvertimeHours(otHours);
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

    @Override
    public byte[] exportTimesheetExcel(TimesheetSearchRequest request) {
        LocalDate start = request.getFromDate() != null ? request.getFromDate() : LocalDate.now().withDayOfMonth(1);
        LocalDate end = request.getToDate() != null ? request.getToDate()
                : LocalDate.now().with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Thống kê công");

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontName("Times New Roman");
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle metaStyle = workbook.createCellStyle();
            Font metaFont = workbook.createFont();
            metaFont.setFontName("Times New Roman");
            metaFont.setFontHeightInPoints((short) 11);
            metaFont.setItalic(true);
            metaStyle.setFont(metaFont);

            CellStyle headerStyle = ExcelUtil.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelUtil.createDataStyle(workbook);
            CellStyle centerStyle = ExcelUtil.createCenterDataStyle(workbook);

            CellStyle totalStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalFont.setFontName("Times New Roman");
            totalFont.setFontHeightInPoints((short) 11);
            totalStyle.setFont(totalFont);
            totalStyle.setBorderTop(BorderStyle.THIN);
            totalStyle.setBorderBottom(BorderStyle.DOUBLE);
            totalStyle.setBorderLeft(BorderStyle.THIN);
            totalStyle.setBorderRight(BorderStyle.THIN);
            totalStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle centerTotalStyle = workbook.createCellStyle();
            centerTotalStyle.cloneStyleFrom(totalStyle);
            centerTotalStyle.setAlignment(HorizontalAlignment.CENTER);

            int currentRowIndex = 0;

            if (request.getStaffId() != null) {
                // Case 1: Single Employee Detail Report
                Staff staff = staffRepository.findById(request.getStaffId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

                List<Timesheet> timesheets = timesheetRepository
                        .findByStaffIdAndWorkingDateBetweenOrderByWorkingDateAsc(
                                staff.getId(), start, end);

                // Title
                Row titleRow = sheet.createRow(currentRowIndex++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("BÁO CÁO CHI TIẾT CHẤM CÔNG CÁ NHÂN");
                titleCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 11));

                // Metadata
                Row metaRow1 = sheet.createRow(currentRowIndex++);
                metaRow1.createCell(0)
                        .setCellValue("Nhân viên: " + staff.getDisplayName() + " (" + staff.getStaffCode() + ")");
                metaRow1.getCell(0).setCellStyle(metaStyle);

                Row metaRow2 = sheet.createRow(currentRowIndex++);
                metaRow2.createCell(0).setCellValue(
                        "Bộ phận: " + (staff.getDepartment() != null ? staff.getDepartment().getName() : "---"));
                metaRow2.getCell(0).setCellStyle(metaStyle);

                Row metaRow3 = sheet.createRow(currentRowIndex++);
                metaRow3.createCell(0)
                        .setCellValue("Thời gian: Từ "
                                + start.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đến "
                                + end.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                metaRow3.getCell(0).setCellStyle(metaStyle);

                currentRowIndex++; // Empty row

                // Table Header
                List<String> headers = List.of(
                        "STT", "Ngày", "Thứ", "Giờ vào", "Giờ ra", "Ca áp dụng", "Công", "Giờ chuẩn", "Giờ OT",
                        "Đi muộn (phút)", "Về sớm (phút)", "Trạng thái");
                Row headerRow = sheet.createRow(currentRowIndex++);
                headerRow.setHeightInPoints(28);
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers.get(i));
                    cell.setCellStyle(headerStyle);
                }

                // Write day-by-day records
                LocalDate current = start;
                int stt = 1;
                double totalWorkRatio = 0;
                double totalStandardHours = 0;
                double totalOvertimeHours = 0;
                int totalLateMinutes = 0;
                int totalEarlyMinutes = 0;

                while (!current.isAfter(end)) {
                    final LocalDate dateToSearch = current;
                    Timesheet record = timesheets.stream()
                            .filter(t -> t.getWorkingDate().equals(dateToSearch))
                            .findFirst()
                            .orElse(null);

                    Row row = sheet.createRow(currentRowIndex++);
                    row.setHeightInPoints(20);

                    // Calculations
                    String minCheckIn = "--:--";
                    String maxCheckOut = "--:--";
                    String shiftsStr = "---";
                    double workRatio = 0;
                    double stdHours = 0;
                    double otHours = 0;
                    int late = 0;
                    int early = 0;
                    String statusStr = "Vắng/Nghỉ";

                    if (record != null) {
                        java.time.LocalDateTime minCi = null;
                        java.time.LocalDateTime maxCo = null;
                        List<String> shiftList = new ArrayList<>();
                        for (TimesheetDetail d : record.getDetails()) {
                            if (d.getCheckInTime() != null) {
                                if (minCi == null || d.getCheckInTime().isBefore(minCi)) {
                                    minCi = d.getCheckInTime();
                                }
                            }
                            if (d.getCheckOutTime() != null) {
                                if (maxCo == null || d.getCheckOutTime().isAfter(maxCo)) {
                                    maxCo = d.getCheckOutTime();
                                }
                            }
                            late += d.getLateMinutes() != null ? d.getLateMinutes() : 0;
                            early += d.getEarlyMinutes() != null ? d.getEarlyMinutes() : 0;
                            if (d.getShift() != null) {
                                shiftList.add(d.getShift().getName() + " ("
                                        + DateTimeUtils.formatShiftTime(d.getShift().getStartTime()) + "-"
                                        + DateTimeUtils.formatShiftTime(d.getShift().getEndTime()) + ")");
                            }
                        }
                        if (minCi != null) {
                            minCheckIn = minCi.toLocalTime()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                        }
                        if (maxCo != null) {
                            maxCheckOut = maxCo.toLocalTime()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                        }
                        if (!shiftList.isEmpty()) {
                            shiftsStr = String.join(" | ", shiftList);
                        }
                        workRatio = record.getTotalWorkRatio() != null ? record.getTotalWorkRatio() : 0.0;
                        stdHours = record.getStandardHours() != null ? record.getStandardHours() : 0.0;
                        otHours = record.getOvertimeHours() != null ? record.getOvertimeHours() : 0.0;
                        if (record.getStatus() != null) {
                            statusStr = switch (record.getStatus()) {
                                case APPROVED -> "Đã duyệt";
                                case SUBMITTED -> "Chờ duyệt";
                                case REJECTED -> "Từ chối";
                                default -> "Nháp";
                            };
                        }
                    }

                    // Accumulate totals
                    totalWorkRatio += workRatio;
                    totalStandardHours += stdHours;
                    totalOvertimeHours += otHours;
                    totalLateMinutes += late;
                    totalEarlyMinutes += early;

                    // Fill cells
                    int col = 0;
                    ExcelUtil.writeCell(row, col++, stt++, centerStyle);
                    ExcelUtil.writeCell(row, col++,
                            current.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), centerStyle);
                    ExcelUtil.writeCell(row, col++, DateTimeUtils.getDayOfWeekVietnamese(current), centerStyle);
                    ExcelUtil.writeCell(row, col++, minCheckIn, centerStyle);
                    ExcelUtil.writeCell(row, col++, maxCheckOut, centerStyle);
                    ExcelUtil.writeCell(row, col++, shiftsStr, dataStyle);
                    ExcelUtil.writeCell(row, col++, workRatio, centerStyle);
                    ExcelUtil.writeCell(row, col++, stdHours, centerStyle);
                    ExcelUtil.writeCell(row, col++, otHours, centerStyle);
                    ExcelUtil.writeCell(row, col++, late > 0 ? late : "", centerStyle);
                    ExcelUtil.writeCell(row, col++, early > 0 ? early : "", centerStyle);
                    ExcelUtil.writeCell(row, col++, statusStr, centerStyle);

                    current = current.plusDays(1);
                }

                // Total Row
                Row totalRow = sheet.createRow(currentRowIndex++);
                totalRow.setHeightInPoints(22);
                for (int i = 0; i < headers.size(); i++) {
                    totalRow.createCell(i).setCellStyle(totalStyle);
                }
                totalRow.getCell(0).setCellValue("TỔNG CỘNG");
                totalRow.getCell(6).setCellValue(totalWorkRatio);
                totalRow.getCell(6).setCellStyle(centerTotalStyle);
                totalRow.getCell(7).setCellValue(totalStandardHours);
                totalRow.getCell(7).setCellStyle(centerTotalStyle);
                totalRow.getCell(8).setCellValue(totalOvertimeHours);
                totalRow.getCell(8).setCellStyle(centerTotalStyle);
                totalRow.getCell(9).setCellValue(totalLateMinutes);
                totalRow.getCell(9).setCellStyle(centerTotalStyle);
                totalRow.getCell(10).setCellValue(totalEarlyMinutes);
                totalRow.getCell(10).setCellStyle(centerTotalStyle);

            } else {
                // Case 2: Multiple Employees Summary Report
                List<Staff> staffs = staffRepository.findAll().stream()
                        .filter(staff -> staff.getIsDeleted() == null || !staff.getIsDeleted())
                        .filter(staff -> {
                            if (request.getDepartmentId() != null) {
                                if (staff.getDepartment() == null
                                        || !staff.getDepartment().getId().equals(request.getDepartmentId())) {
                                    return false;
                                }
                            }
                            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                                String keyword = request.getKeyword().toLowerCase();
                                boolean matches = (staff.getStaffCode() != null
                                        && staff.getStaffCode().toLowerCase().contains(keyword))
                                        || (staff.getDisplayName() != null
                                                && staff.getDisplayName().toLowerCase().contains(keyword));
                                if (!matches)
                                    return false;
                            }
                            return true;
                        })
                        .collect(Collectors.toList());

                List<UUID> staffIds = staffs.stream().map(Staff::getId).collect(Collectors.toList());

                List<Timesheet> timesheets = staffIds.isEmpty() ? List.of()
                        : timesheetRepository.findAll((root, query, cb) -> {
                            List<Predicate> predicates = new ArrayList<>();
                            predicates.add(root.get("staff").get("id").in(staffIds));
                            predicates.add(cb.between(root.get("workingDate"), start, end));
                            predicates.add(
                                    cb.or(cb.isNull(root.get("isDeleted")), cb.equal(root.get("isDeleted"), false)));
                            return cb.and(predicates.toArray(new Predicate[0]));
                        });

                Map<UUID, List<Timesheet>> timesheetsByStaff = timesheets.stream()
                        .collect(Collectors.groupingBy(t -> t.getStaff().getId()));

                // Title
                Row titleRow = sheet.createRow(currentRowIndex++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("BÁO CÁO THỐNG KÊ TỔNG CÔNG NHÂN VIÊN");
                titleCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(
                        new org.apache.poi.ss.util.CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), 0, 8));

                // Metadata
                Row metaRow1 = sheet.createRow(currentRowIndex++);
                if (request.getDepartmentId() != null && !staffs.isEmpty() && staffs.get(0).getDepartment() != null) {
                    metaRow1.createCell(0).setCellValue("Bộ phận: " + staffs.get(0).getDepartment().getName());
                } else {
                    metaRow1.createCell(0).setCellValue("Bộ phận: Tất cả");
                }
                metaRow1.getCell(0).setCellStyle(metaStyle);

                Row metaRow2 = sheet.createRow(currentRowIndex++);
                metaRow2.createCell(0)
                        .setCellValue("Thời gian: Từ "
                                + start.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đến "
                                + end.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                metaRow2.getCell(0).setCellStyle(metaStyle);

                currentRowIndex++; // Empty row

                // Table Header
                List<String> headers = List.of(
                        "STT", "Mã NV", "Họ tên", "Phòng ban", "Vị trí", "Tổng công", "Tổng giờ chuẩn", "Tổng giờ OT",
                        "Đi muộn (phút)", "Về sớm (phút)");
                Row headerRow = sheet.createRow(currentRowIndex++);
                headerRow.setHeightInPoints(28);
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers.get(i));
                    cell.setCellStyle(headerStyle);
                }

                int stt = 1;
                double totalWorkRatio = 0;
                double totalStandardHours = 0;
                double totalOvertimeHours = 0;
                int totalLateMinutes = 0;
                int totalEarlyMinutes = 0;

                for (Staff staff : staffs) {
                    List<Timesheet> staffTimesheets = timesheetsByStaff.getOrDefault(staff.getId(), List.of());

                    double workRatio = 0;
                    double stdHours = 0;
                    double otHours = 0;
                    int late = 0;
                    int early = 0;

                    for (Timesheet ts : staffTimesheets) {
                        workRatio += ts.getTotalWorkRatio() != null ? ts.getTotalWorkRatio() : 0.0;
                        stdHours += ts.getStandardHours() != null ? ts.getStandardHours() : 0.0;
                        otHours += ts.getOvertimeHours() != null ? ts.getOvertimeHours() : 0.0;

                        if (ts.getDetails() != null) {
                            for (TimesheetDetail d : ts.getDetails()) {
                                late += d.getLateMinutes() != null ? d.getLateMinutes() : 0;
                                early += d.getEarlyMinutes() != null ? d.getEarlyMinutes() : 0;
                            }
                        }
                    }

                    totalWorkRatio += workRatio;
                    totalStandardHours += stdHours;
                    totalOvertimeHours += otHours;
                    totalLateMinutes += late;
                    totalEarlyMinutes += early;

                    Row row = sheet.createRow(currentRowIndex++);
                    row.setHeightInPoints(20);

                    int col = 0;
                    ExcelUtil.writeCell(row, col++, stt++, centerStyle);
                    ExcelUtil.writeCell(row, col++, staff.getStaffCode() != null ? staff.getStaffCode() : "---",
                            centerStyle);
                    ExcelUtil.writeCell(row, col++, staff.getDisplayName() != null ? staff.getDisplayName() : "---",
                            dataStyle);
                    ExcelUtil.writeCell(row, col++,
                            staff.getDepartment() != null ? staff.getDepartment().getName() : "---", dataStyle);
                    ExcelUtil.writeCell(row, col++, staff.getPosition() != null ? staff.getPosition().getName() : "---",
                            dataStyle);
                    ExcelUtil.writeCell(row, col++, workRatio, centerStyle);
                    ExcelUtil.writeCell(row, col++, stdHours, centerStyle);
                    ExcelUtil.writeCell(row, col++, otHours, centerStyle);
                    ExcelUtil.writeCell(row, col++, late > 0 ? late : "", centerStyle);
                    ExcelUtil.writeCell(row, col++, early > 0 ? early : "", centerStyle);
                }

                // Total Row
                Row totalRow = sheet.createRow(currentRowIndex++);
                totalRow.setHeightInPoints(22);
                for (int i = 0; i < headers.size(); i++) {
                    totalRow.createCell(i).setCellStyle(totalStyle);
                }
                totalRow.getCell(0).setCellValue("TỔNG CỘNG");
                totalRow.getCell(5).setCellValue(totalWorkRatio);
                totalRow.getCell(5).setCellStyle(centerTotalStyle);
                totalRow.getCell(6).setCellValue(totalStandardHours);
                totalRow.getCell(6).setCellStyle(centerTotalStyle);
                totalRow.getCell(7).setCellValue(totalOvertimeHours);
                totalRow.getCell(7).setCellStyle(centerTotalStyle);
                totalRow.getCell(8).setCellValue(totalLateMinutes);
                totalRow.getCell(8).setCellStyle(centerTotalStyle);
                totalRow.getCell(9).setCellValue(totalEarlyMinutes);
                totalRow.getCell(9).setCellStyle(centerTotalStyle);
            }

            // Auto-size columns with padding (skip column 0 for STT as it holds
            // title/metadata text)
            int maxCols = request.getStaffId() != null ? 12 : 10;
            sheet.setColumnWidth(0, 8 * 256);
            for (int i = 1; i < maxCols; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(currentWidth + 512, 256 * 50));
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi tạo file Excel: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void initHolidayTimesheets(UUID staffId, LocalDate start, LocalDate end) {
        if (staffId == null || start == null || end == null || start.isAfter(end)) {
            return;
        }
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));

        // Lấy danh sách ngày lễ trong khoảng từ DB thay vì check từng ngày hardcoded
        List<HolidayDto> holidays = holidayService.getHolidaysInRange(start, end);
        for (HolidayDto holiday : holidays) {
            LocalDate holidayStart = holiday.getStartDate().isBefore(start) ? start : holiday.getStartDate();
            LocalDate holidayEnd = holiday.getEndDate().isAfter(end) ? end : holiday.getEndDate();

            for (LocalDate date = holidayStart; !date.isAfter(holidayEnd); date = date.plusDays(1)) {
                boolean exists = timesheetRepository.findByStaffIdAndWorkingDate(staffId, date).isPresent();
                if (!exists) {
                    Timesheet timesheet = new Timesheet();
                    timesheet.setStaff(staff);
                    timesheet.setWorkingDate(date);
                    timesheet.setTotalWorkRatio(1.0);
                    timesheet.setStandardHours(8.0);
                    timesheet.setStatus(TimesheetStatus.APPROVED);
                    timesheet.setNote("Nghỉ lễ hưởng nguyên lương - " + holiday.getName());
                    timesheet.setOvertimeHours(0.0);
                    timesheet.setWeekendOvertimeHours(0.0);
                    timesheet.setHolidayOvertimeHours(0.0);
                    timesheetRepository.save(timesheet);
                }
            }
        }
    }
}
