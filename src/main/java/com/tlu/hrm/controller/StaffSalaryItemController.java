package com.tlu.hrm.controller;

import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.response.StaffSalaryItemResponse;
import com.tlu.hrm.model.StaffSalaryItem;
import com.tlu.hrm.service.SalaryItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/staff-salary-items")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class StaffSalaryItemController {

    private final SalaryItemService salaryItemService;

    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_COMPENSATION_BENEFIT + "', '"
            + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<List<StaffSalaryItemResponse>>> getStaffSalaryItems(@PathVariable UUID staffId) {
        List<StaffSalaryItemResponse> result = salaryItemService.getStaffSalaryItems(staffId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cấu hình lương nhân viên thành công", result));
    }

    @PostMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_COMPENSATION_BENEFIT + "')")
    public ResponseEntity<ApiResponse<List<StaffSalaryItemResponse>>> saveStaffSalaryItems(@PathVariable UUID staffId,
            @RequestBody List<StaffSalaryItem> items) {
        List<StaffSalaryItemResponse> result = salaryItemService.saveStaffSalaryItems(staffId, items);
        return ResponseEntity.ok(ApiResponse.success("Lưu cấu hình lương nhân viên thành công", result));
    }
}
