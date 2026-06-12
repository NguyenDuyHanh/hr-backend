package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.CheckInOutRecordDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.service.CheckInOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/check-in-out")
@CrossOrigin(origins = "*")
public class CheckInOutController {

    @Autowired
    private CheckInOutService checkInOutService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<CheckInOutRecordDto>> logCheckInOut(@RequestBody CheckInOutRecordDto dto) {
        CheckInOutRecordDto result = checkInOutService.save(dto);
        return ResponseEntity.ok(ApiResponse.success("Ghi nhận lượt chấm công thành công", result));
    }

    @GetMapping("/raw-logs")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<List<CheckInOutRecordDto>>> getRawLogs(
            @RequestParam UUID staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CheckInOutRecordDto> result = checkInOutService.getRawLogs(staffId, date);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lịch sử quẹt thẻ thành công", result));
    }
}
