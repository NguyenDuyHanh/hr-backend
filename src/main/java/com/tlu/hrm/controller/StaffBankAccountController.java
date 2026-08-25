package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.StaffBankAccountDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.service.StaffBankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/staff-bank-accounts")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
public class StaffBankAccountController {

    @Autowired
    private StaffBankAccountService staffBankAccountService;

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<List<StaffBankAccountDto>>> getBankAccountsByStaffId(@PathVariable UUID staffId) {
        List<StaffBankAccountDto> result = staffBankAccountService.getBankAccountsByStaffId(staffId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tài khoản ngân hàng thành công", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StaffBankAccountDto>> createBankAccount(@RequestBody StaffBankAccountDto dto) {
        StaffBankAccountDto result = staffBankAccountService.createBankAccount(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo tài khoản ngân hàng thành công", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffBankAccountDto>> updateBankAccount(@PathVariable UUID id, @RequestBody StaffBankAccountDto dto) {
        StaffBankAccountDto result = staffBankAccountService.updateBankAccount(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tài khoản ngân hàng thành công", result));
    }

    @PutMapping("/{id}/set-default")
    public ResponseEntity<ApiResponse<StaffBankAccountDto>> setDefaultAccount(@PathVariable UUID id) {
        StaffBankAccountDto result = staffBankAccountService.setDefaultAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Đặt làm tài khoản mặc định thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBankAccount(@PathVariable UUID id) {
        staffBankAccountService.deleteBankAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tài khoản ngân hàng thành công", null));
    }
}
