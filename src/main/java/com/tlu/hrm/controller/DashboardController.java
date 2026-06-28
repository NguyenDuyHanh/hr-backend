package com.tlu.hrm.controller;

import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.response.DashboardSummaryResponse;
import com.tlu.hrm.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

import static com.tlu.hrm.enums.RoleType.Constants.HR_MANAGER;
import static com.tlu.hrm.enums.RoleType.Constants.ROLE_ADMIN;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        DashboardSummaryResponse result = dashboardService.getSummary();
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu tổng quan dashboard thành công", result));
    }
}
