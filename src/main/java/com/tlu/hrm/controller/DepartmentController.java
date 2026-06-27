package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.DepartmentDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<DepartmentDto>>> pagingDepartments(
            @RequestBody(required = false) SearchDto searchDto) {
        Page<DepartmentDto> result = departmentService.pagingDepartments(searchDto);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phòng ban thành công", result));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getAllDepartments() {
        List<DepartmentDto> result = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách phòng ban thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDto>> getById(@PathVariable UUID id) {
        DepartmentDto dto = departmentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin phòng ban thành công", dto));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<DepartmentDto>> createDepartment(@RequestBody DepartmentDto dto) {
        if (!departmentService.isValidCode(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã phòng ban đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        DepartmentDto result = departmentService.saveDepartment(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo phòng ban thành công", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<DepartmentDto>> updateDepartment(@PathVariable UUID id,
            @RequestBody DepartmentDto dto) {
        dto.setId(id);
        if (!departmentService.isValidCode(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã phòng ban đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        DepartmentDto result = departmentService.saveDepartment(dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật phòng ban thành công", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable UUID id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa phòng ban thành công", null));
    }

    @PostMapping("/delete-multiple")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<Void>> deleteMultiple(@RequestBody List<UUID> ids) {
        departmentService.deleteMultiple(ids);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh sách phòng ban thành công", null));
    }

    @GetMapping("/generate-code")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<String>> generateCode() {
        String code = departmentService.generateCode();
        return ResponseEntity.ok(ApiResponse.success("Tạo mã phòng ban thành công", code));
    }
}
