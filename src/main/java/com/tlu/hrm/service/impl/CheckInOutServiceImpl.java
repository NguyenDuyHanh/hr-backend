package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.CheckInOutRecordDto;
import com.tlu.hrm.enums.TimesheetStatus;
import com.tlu.hrm.model.CheckInOutRecord;
import com.tlu.hrm.model.ShiftWork;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.Timesheet;
import com.tlu.hrm.model.TimesheetDetail;
import com.tlu.hrm.repository.CheckInOutRecordRepository;
import com.tlu.hrm.repository.ShiftWorkRepository;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.repository.TimesheetRepository;
import com.tlu.hrm.service.CheckInOutService;
import com.tlu.hrm.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CheckInOutServiceImpl implements CheckInOutService {

    @Autowired
    private CheckInOutRecordRepository checkInOutRecordRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ShiftWorkRepository shiftWorkRepository;

    @Autowired
    @Lazy
    private TimesheetService timesheetService;

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Override
    public CheckInOutRecordDto save(CheckInOutRecordDto dto) {
        if (dto.getPhotoUrl() == null || dto.getPhotoUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Chấm công bắt buộc phải chụp ảnh webcam minh chứng!");
        }
        if (dto.getLatitude() == null || dto.getLongitude() == null) {
            throw new IllegalArgumentException("Không thể chấm công khi chưa bật hoặc cấp quyền định vị GPS!");
        }

        Staff staff = staffRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));

        LocalDateTime recordTime = dto.getRecordTime() != null ? dto.getRecordTime() : LocalDateTime.now();
        recordTime = recordTime.withSecond(0).withNano(0);

        // Kiểm tra xem bảng công hôm nay đã được DUYỆT hoặc TỪ CHỐI chưa
        Optional<Timesheet> timesheetOpt = timesheetRepository
                .findByStaffIdAndWorkingDate(staff.getId(), recordTime.toLocalDate());
        if (timesheetOpt.isPresent()) {
            Timesheet timesheet = timesheetOpt.get();
            TimesheetStatus status = timesheet.getStatus();
            if (status == TimesheetStatus.APPROVED || status == TimesheetStatus.REJECTED) {
                throw new IllegalStateException(
                        "Ngày công này đã được phê duyệt hoặc từ chối, không thể tiếp tục chấm công!");
            }
        }

        CheckInOutRecord record = new CheckInOutRecord();
        record.setStaff(staff);
        record.setRecordTime(recordTime);

        record.setIpAddress(dto.getIpAddress());
        record.setLatitude(dto.getLatitude());
        record.setLongitude(dto.getLongitude());
        record.setDeviceType(dto.getDeviceType());
        record.setPhotoUrl(dto.getPhotoUrl());
        record.setRecordType(dto.getRecordType());

        if (dto.getShiftId() != null) {
            ShiftWork shift = shiftWorkRepository.findById(dto.getShiftId()).orElse(null);
            record.setShift(shift);
        }

        CheckInOutRecord saved = checkInOutRecordRepository.save(record);

        // Kích hoạt tính toán bảng công cho nhân viên vào ngày tương ứng
        timesheetService.calculateTimesheet(staff.getId(), recordTime.toLocalDate());

        return new CheckInOutRecordDto(saved);
    }

    @Override
    public List<CheckInOutRecordDto> getRawLogs(UUID staffId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59, 999999999);
        return checkInOutRecordRepository.findByStaffIdAndRecordTimeBetweenOrderByRecordTimeAsc(staffId, start, end)
                .stream()
                .map(CheckInOutRecordDto::new)
                .collect(Collectors.toList());
    }
}
