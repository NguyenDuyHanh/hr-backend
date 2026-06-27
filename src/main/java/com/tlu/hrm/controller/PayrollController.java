package com.tlu.hrm.controller;

import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.model.Period;
import com.tlu.hrm.model.Payroll;
import com.tlu.hrm.model.Payslip;
import com.tlu.hrm.model.SalaryItem;
import com.tlu.hrm.model.StaffSalaryItem;
import com.tlu.hrm.security.SecurityUtils;
import com.tlu.hrm.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/payrolls")
@CrossOrigin(origins = "*")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_COMPENSATION_BENEFIT + "')")
    public ResponseEntity<ApiResponse<Payroll>> createPayroll(
            @RequestParam UUID periodId,
            @RequestParam String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String description) {
        Payroll result = payrollService.createPayroll(periodId, name, code, description);
        return ResponseEntity.ok(ApiResponse.success("Tạo bảng lương mới thành công", result));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_COMPENSATION_BENEFIT + "', '"
            + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<List<Payroll>>> getAllPayrolls() {
        List<Payroll> result = payrollService.getAllPayrolls();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tất cả bảng lương thành công", result));
    }

    @GetMapping("/by-period/{periodId}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_COMPENSATION_BENEFIT + "', '"
            + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<List<Payroll>>> getPayrollsByPeriod(@PathVariable UUID periodId) {
        List<Payroll> result = payrollService.getPayrollsByPeriod(periodId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bảng lương thành công", result));
    }

    @PostMapping("/{id}/calculate")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_COMPENSATION_BENEFIT + "')")
    public ResponseEntity<ApiResponse<List<Payslip>>> calculatePayroll(@PathVariable UUID id) {
        List<Payslip> result = payrollService.calculatePayroll(id);
        return ResponseEntity.ok(ApiResponse.success("Tính lương thành công", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_COMPENSATION_BENEFIT + "', '"
            + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<List<Payslip>>> getPayrollDetails(@PathVariable UUID id) {
        List<Payslip> result = payrollService.getPayrollDetails(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết bảng lương thành công", result));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_COMPENSATION_BENEFIT + "')")
    public ResponseEntity<ApiResponse<Payroll>> confirmPayroll(@PathVariable UUID id) {
        Payroll result = payrollService.confirmPayroll(id);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận bảng lương thành công", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_COMPENSATION_BENEFIT + "')")
    public ResponseEntity<ApiResponse<Void>> deletePayroll(@PathVariable UUID id) {
        payrollService.deletePayroll(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bảng lương thành công", null));
    }

    @GetMapping("/my-payslip")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_COMPENSATION_BENEFIT + "', '"
            + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<Payslip>> getMyPayslip(@RequestParam UUID periodId) {
        com.tlu.hrm.model.User currentUser = securityUtils.getCurrentUser();
        Payslip result = payrollService.getMyPayslip(periodId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Lấy phiếu lương của tôi thành công", result));
    }

    @PutMapping("/payslip/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_COMPENSATION_BENEFIT + "')")
    public ResponseEntity<ApiResponse<Payslip>> updatePayslip(
            @PathVariable UUID id,
            @RequestParam com.tlu.hrm.enums.PaidStatus paidStatus,
            @RequestParam(required = false) String note) {
        Payslip result = payrollService.updatePayslip(id, paidStatus, note);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật phiếu lương thành công", result));
    }
}
