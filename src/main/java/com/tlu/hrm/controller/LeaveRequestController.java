package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.LeaveRequestDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.response.StaffAnnualLeaveBalanceDto;
import com.tlu.hrm.dto.search.LeaveRequestSearchRequest;
import com.tlu.hrm.model.User;
import com.tlu.hrm.security.SecurityUtils;
import com.tlu.hrm.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/leave-requests")
@CrossOrigin(origins = "*")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private SecurityUtils securityUtils;

    @PostMapping("/paging")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<Page<LeaveRequestDto>>> search(@RequestBody LeaveRequestSearchRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Người dùng chưa đăng nhập", HttpStatus.UNAUTHORIZED));
        }

        // Nếu là nhân viên thường, chỉ cho phép xem đơn của chính mình
        boolean isEmployeeOnly = currentUser.getUserRoles().stream()
                .noneMatch(ur -> ROLE_ADMIN.equals(ur.getRole().getName()) || HR_MANAGER.equals(ur.getRole().getName()));

        if (isEmployeeOnly) {
            if (currentUser.getStaff() == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Tài khoản chưa liên kết với nhân sự", HttpStatus.BAD_REQUEST));
            }
            request.setStaffId(currentUser.getStaff().getId());
        }

        Page<LeaveRequestDto> result = leaveRequestService.search(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn nghỉ phép thành công", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> getById(@PathVariable UUID id) {
        LeaveRequestDto result = leaveRequestService.getById(id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = securityUtils.getCurrentUser();
        boolean isEmployeeOnly = currentUser.getUserRoles().stream()
                .noneMatch(ur -> ROLE_ADMIN.equals(ur.getRole().getName()) || HR_MANAGER.equals(ur.getRole().getName()));

        if (isEmployeeOnly && !result.getRequestStaffId().equals(currentUser.getStaff().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Bạn không có quyền xem đơn nghỉ phép của người khác", HttpStatus.FORBIDDEN));
        }

        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn nghỉ phép thành công", result));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> create(@RequestBody LeaveRequestDto dto) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Người dùng chưa đăng nhập", HttpStatus.UNAUTHORIZED));
        }

        boolean isEmployeeOnly = currentUser.getUserRoles().stream()
                .noneMatch(ur -> ROLE_ADMIN.equals(ur.getRole().getName()) || HR_MANAGER.equals(ur.getRole().getName()));

        if (isEmployeeOnly) {
            if (currentUser.getStaff() == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Tài khoản chưa liên kết với nhân sự", HttpStatus.BAD_REQUEST));
            }
            // Nhân viên thường chỉ được tạo đơn cho chính mình
            dto.setRequestStaffId(currentUser.getStaff().getId());
        }

        LeaveRequestDto result = leaveRequestService.create(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo đơn nghỉ phép thành công", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> update(@PathVariable UUID id, @RequestBody LeaveRequestDto dto) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Người dùng chưa đăng nhập", HttpStatus.UNAUTHORIZED));
        }

        LeaveRequestDto existing = leaveRequestService.getById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isEmployeeOnly = currentUser.getUserRoles().stream()
                .noneMatch(ur -> ROLE_ADMIN.equals(ur.getRole().getName()) || HR_MANAGER.equals(ur.getRole().getName()));

        if (isEmployeeOnly) {
            if (!existing.getRequestStaffId().equals(currentUser.getStaff().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Bạn không có quyền sửa đơn nghỉ phép của người khác", HttpStatus.FORBIDDEN));
            }
            // Không cho phép nhân viên thường tự ý thay đổi requestStaffId
            dto.setRequestStaffId(currentUser.getStaff().getId());
        }

        LeaveRequestDto result = leaveRequestService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đơn nghỉ phép thành công", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Người dùng chưa đăng nhập", HttpStatus.UNAUTHORIZED));
        }

        LeaveRequestDto existing = leaveRequestService.getById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isEmployeeOnly = currentUser.getUserRoles().stream()
                .noneMatch(ur -> ROLE_ADMIN.equals(ur.getRole().getName()) || HR_MANAGER.equals(ur.getRole().getName()));

        if (isEmployeeOnly && !existing.getRequestStaffId().equals(currentUser.getStaff().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Bạn không có quyền xóa đơn nghỉ phép của người khác", HttpStatus.FORBIDDEN));
        }

        leaveRequestService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa/Hủy đơn nghỉ phép thành công", null));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> approve(
            @PathVariable UUID id,
            @RequestParam(required = false) String rejectReason) {
        LeaveRequestDto result = leaveRequestService.approve(id, rejectReason);
        return ResponseEntity.ok(ApiResponse.success("Phê duyệt đơn nghỉ phép thành công", result));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<LeaveRequestDto>> reject(
            @PathVariable UUID id,
            @RequestParam(required = false) String rejectReason) {
        LeaveRequestDto result = leaveRequestService.reject(id, rejectReason);
        return ResponseEntity.ok(ApiResponse.success("Từ chối đơn nghỉ phép thành công", result));
    }

    @GetMapping("/balance/{staffId}/{year}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<StaffAnnualLeaveBalanceDto>> getLeaveBalance(
            @PathVariable UUID staffId,
            @PathVariable int year) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Người dùng chưa đăng nhập", HttpStatus.UNAUTHORIZED));
        }

        boolean isEmployeeOnly = currentUser.getUserRoles().stream()
                .noneMatch(ur -> ROLE_ADMIN.equals(ur.getRole().getName()) || HR_MANAGER.equals(ur.getRole().getName()));

        if (isEmployeeOnly && !staffId.equals(currentUser.getStaff().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Bạn không có quyền xem số dư phép của người khác", HttpStatus.FORBIDDEN));
        }

        StaffAnnualLeaveBalanceDto result = leaveRequestService.getLeaveBalance(staffId, year);
        return ResponseEntity.ok(ApiResponse.success("Lấy số dư phép thành công", result));
    }

    @PostMapping("/balance/paging")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<Page<StaffAnnualLeaveBalanceDto>>> getLeaveBalances(
            @RequestBody(required = false) com.tlu.hrm.dto.search.SearchDto searchDto,
            @RequestParam(required = false) Integer year) {
        int targetYear = year != null ? year : java.time.LocalDate.now().getYear();
        Page<StaffAnnualLeaveBalanceDto> result = leaveRequestService.getLeaveBalances(searchDto, targetYear);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách số dư phép năm thành công", result));
    }
}
