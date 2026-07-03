package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.StaffDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/staffs")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @Autowired
    private com.tlu.hrm.repository.DepartmentRepository departmentRepository;

    @Autowired
    private com.tlu.hrm.repository.PositionRepository positionRepository;

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<com.tlu.hrm.model.Department>>> getAllDepartments() {
        List<com.tlu.hrm.model.Department> result = departmentRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách phòng ban thành công", result));
    }

    @GetMapping("/positions")
    public ResponseEntity<ApiResponse<List<com.tlu.hrm.model.Position>>> getAllPositions() {
        List<com.tlu.hrm.model.Position> result = positionRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách chức danh thành công", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffDto>>> getAllStaffsUnpaginated() {
        List<StaffDto> result = staffService.getAllStaffsUnpaginated();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách nhân viên thành công", result));
    }

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<StaffDto>>> getAllStaffs(
            @RequestBody(required = false) SearchDto searchDto) {
        Page<StaffDto> result = staffService.getAllStaffs(searchDto);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhân viên thành công", result));
    }

    @GetMapping("/generate-staff-code")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<String>> generateStaffCode() {
        String code = staffService.generateStaffCode();
        return ResponseEntity.ok(ApiResponse.success("Tạo mã nhân viên thành công", code));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
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
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<StaffDto>> updateStaff(@PathVariable UUID id, @RequestBody StaffDto staffDto) {
        if (!staffService.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên để cập nhật với ID: " + id);
        }
        staffDto.setId(id);
        StaffDto result = staffService.saveStaff(staffDto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhân viên thành công", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable UUID id) {
        if (!staffService.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên để xóa với ID: " + id);
        }
        staffService.deleteStaff(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa nhân viên thành công", null));
    }

    @PostMapping("/export-excel")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<byte[]> exportStaffExcel(@RequestBody(required = false) SearchDto searchDto) {
        byte[] excelData = staffService.exportStaffExcel(searchDto);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment",
                java.net.URLEncoder.encode("DanhSachNhanVien.xlsx", java.nio.charset.StandardCharsets.UTF_8));

        return ResponseEntity.ok().headers(headers).body(excelData);
    }
}
