package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.StaffDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staffs")
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffDto>>> getAllStaffsUnpaginated() {
        List<StaffDto> result = staffService.getAllStaffsUnpaginated();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách nhân viên thành công", result));
    }

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<StaffDto>>> getAllStaffs(@RequestBody(required = false) SearchDto searchDto) {
        Page<StaffDto> result = staffService.getAllStaffs(searchDto);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhân viên thành công", result));
    }

    @GetMapping("/generate-staff-code")
    public ResponseEntity<ApiResponse<String>> generateStaffCode() {
        String code = staffService.generateStaffCode();
        return ResponseEntity.ok(ApiResponse.success("Tạo mã nhân viên thành công", code));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StaffDto>> createStaff(@RequestBody StaffDto staffDto) {
        StaffDto result = staffService.saveStaff(staffDto);
        return ResponseEntity.ok(ApiResponse.success("Thêm nhân viên thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffDto>> getStaffById(@PathVariable UUID id) {
        StaffDto staffDto = staffService.getStaffById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhân viên thành công", staffDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffDto>> updateStaff(@PathVariable UUID id, @RequestBody StaffDto staffDto) {
        if (!staffService.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên để cập nhật với ID: " + id);
        }
        staffDto.setId(id);
        StaffDto result = staffService.saveStaff(staffDto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhân viên thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable UUID id) {
        if (!staffService.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên để xóa với ID: " + id);
        }
        staffService.deleteStaff(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa nhân viên thành công", null));
    }
}

