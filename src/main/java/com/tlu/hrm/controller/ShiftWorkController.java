package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.ShiftWorkDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.service.ShiftWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/shifts")
@CrossOrigin(origins = "*")
public class ShiftWorkController {

    @Autowired
    private ShiftWorkService shiftWorkService;

    @PostMapping("/paging")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_TIMEKEEPING_MANAGER + "')")
    public ResponseEntity<ApiResponse<Page<ShiftWorkDto>>> paging(@RequestBody(required = false) SearchDto searchDto) {
        int index = searchDto != null ? searchDto.getPageIndex() : 1;
        int size = searchDto != null ? searchDto.getPageSize() : 10;
        String keyword = searchDto != null ? searchDto.getKeyword() : "";
        
        Page<ShiftWorkDto> result = shiftWorkService.getPage(index, size, keyword);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ca làm việc thành công", result));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_TIMEKEEPING_MANAGER + "', '" + HR_EMPLOYEE + "')")
    public ResponseEntity<ApiResponse<List<ShiftWorkDto>>> getAll() {
        List<ShiftWorkDto> result = shiftWorkService.getAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ ca làm việc thành công", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_TIMEKEEPING_MANAGER + "')")
    public ResponseEntity<ApiResponse<ShiftWorkDto>> getById(@PathVariable UUID id) {
        ShiftWorkDto result = shiftWorkService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin ca làm việc thành công", result));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_TIMEKEEPING_MANAGER + "')")
    public ResponseEntity<ApiResponse<ShiftWorkDto>> create(@RequestBody ShiftWorkDto dto) {
        ShiftWorkDto result = shiftWorkService.saveOrUpdate(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo ca làm việc thành công", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_TIMEKEEPING_MANAGER + "')")
    public ResponseEntity<ApiResponse<ShiftWorkDto>> update(@PathVariable UUID id, @RequestBody ShiftWorkDto dto) {
        dto.setId(id);
        ShiftWorkDto result = shiftWorkService.saveOrUpdate(dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ca làm việc thành công", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_TIMEKEEPING_MANAGER + "')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        shiftWorkService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa ca làm việc thành công", null));
    }
}
