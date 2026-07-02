package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.TimesheetDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.TimesheetSearchRequest;
import com.tlu.hrm.enums.TimesheetStatus;
import com.tlu.hrm.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/timesheets")
@CrossOrigin(origins = "*")
public class TimesheetController {

    @Autowired
    private TimesheetService timesheetService;

    @PostMapping("/paging")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_TIMEKEEPING_MANAGER + "')")
    public ResponseEntity<ApiResponse<Page<TimesheetDto>>> search(@RequestBody TimesheetSearchRequest request) {
        Page<TimesheetDto> result = timesheetService.search(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ngày công thành công", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_TIMEKEEPING_MANAGER + "', '" + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<TimesheetDto>> getById(@PathVariable UUID id) {
        TimesheetDto result = timesheetService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin ngày công chi tiết thành công", result));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_TIMEKEEPING_MANAGER + "')")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable UUID id,
            @RequestParam TimesheetStatus status,
            @RequestParam(required = false) String note) {
        boolean success = timesheetService.approve(id, status, note);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái ngày công thành công", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy ngày công để cập nhật", org.springframework.http.HttpStatus.BAD_REQUEST));
        }
    }

    @GetMapping("/staff-range")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<List<TimesheetDto>>> getByStaffAndRange(
            @RequestParam UUID staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<TimesheetDto> result = timesheetService.getByStaffAndDateRange(staffId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử chấm công nhân viên thành công", result));
    }

    @PostMapping("/export-excel")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_TIMEKEEPING_MANAGER + "')")
    public ResponseEntity<byte[]> exportTimesheetExcel(@RequestBody(required = false) TimesheetSearchRequest request) {
        byte[] excelData = timesheetService.exportTimesheetExcel(request);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment",
                java.net.URLEncoder.encode("BaoCaoCong.xlsx", java.nio.charset.StandardCharsets.UTF_8));

        return ResponseEntity.ok().headers(headers).body(excelData);
    }
}
