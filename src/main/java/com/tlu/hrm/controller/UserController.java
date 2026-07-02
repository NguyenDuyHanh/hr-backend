package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.UserDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/unpaginated")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsersUnpaginated() {
        List<UserDto> result = userService.getAllUsersUnpaginated();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách người dùng thành công", result));
    }

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllUsers(@RequestBody(required = false) SearchDto searchDto) {
        Page<UserDto> result = userService.getAllUsers(searchDto);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody UserDto userDto) {
        UserDto result = userService.saveUser(userDto);
        return ResponseEntity.ok(ApiResponse.success("Tạo người dùng thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable UUID id, @RequestBody UserDto userDto) {
        if (!userService.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng để cập nhật với ID: " + id);
        }
        userDto.setId(id);
        UserDto result = userService.saveUser(userDto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật người dùng thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        if (!userService.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng để xóa với ID: " + id);
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công", null));
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<UserDto>> lockUser(@PathVariable UUID id) {
        UserDto result = userService.lockUser(id);
        return ResponseEntity.ok(ApiResponse.success("Khóa tài khoản thành công", result));
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<UserDto>> unlockUser(@PathVariable UUID id) {
        UserDto result = userService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success("Mở khóa tài khoản thành công", result));
    }
}
