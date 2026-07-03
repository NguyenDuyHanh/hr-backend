package com.tlu.hrm.controller;

import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.model.Role;
import com.tlu.hrm.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tlu.hrm.enums.RoleType.Constants.ROLE_ADMIN;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('" + ROLE_ADMIN + "')")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> result = roleRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách vai trò thành công", result));
    }
}
