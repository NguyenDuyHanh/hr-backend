package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.RoleDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.ROLE_ADMIN;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('" + ROLE_ADMIN + "')")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRoles() {
        List<RoleDto> result = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách vai trò thành công", result));
    }

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<RoleDto>>> pagingRoles(
            @RequestBody(required = false) SearchDto searchDto) {
        Page<RoleDto> result = roleService.pagingRoles(searchDto);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách vai trò thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> getById(@PathVariable UUID id) {
        RoleDto dto = roleService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin vai trò thành công", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@RequestBody RoleDto dto) {
        if (!roleService.isValidName(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Tên vai trò đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        RoleDto result = roleService.saveRole(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo vai trò thành công", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(@PathVariable UUID id, @RequestBody RoleDto dto) {
        dto.setId(id);
        if (!roleService.isValidName(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Tên vai trò đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        RoleDto result = roleService.saveRole(dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật vai trò thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa vai trò thành công", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/delete-multiple")
    public ResponseEntity<ApiResponse<Void>> deleteMultiple(@RequestBody List<UUID> ids) {
        try {
            roleService.deleteMultiple(ids);
            return ResponseEntity.ok(ApiResponse.success("Xóa danh sách vai trò thành công", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
}
