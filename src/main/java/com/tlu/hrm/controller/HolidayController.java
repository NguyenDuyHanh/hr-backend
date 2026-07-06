package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.HolidayDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/holidays")
@CrossOrigin(origins = "*")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @PostMapping("/paging")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<Page<HolidayDto>>> paging(@RequestBody(required = false) com.tlu.hrm.dto.search.HolidaySearchRequest searchDto) {
        Page<HolidayDto> result = holidayService.getPage(searchDto != null ? searchDto : new com.tlu.hrm.dto.search.HolidaySearchRequest());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ngày lễ thành công", result));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_TIMEKEEPING_MANAGER + "')")
    public ResponseEntity<ApiResponse<List<HolidayDto>>> getAll() {
        List<HolidayDto> result = holidayService.getAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ ngày lễ thành công", result));
    }

    @GetMapping("/year/{year}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_TIMEKEEPING_MANAGER + "')")
    public ResponseEntity<ApiResponse<List<HolidayDto>>> getByYear(@PathVariable Integer year) {
        List<HolidayDto> result = holidayService.getByYear(year);
        return ResponseEntity.ok(ApiResponse.success("Lấy ngày lễ theo năm thành công", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<HolidayDto>> getById(@PathVariable UUID id) {
        HolidayDto result = holidayService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin ngày lễ thành công", result));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<HolidayDto>> create(@RequestBody HolidayDto dto) {
        HolidayDto result = holidayService.saveOrUpdate(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo ngày lễ thành công", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<HolidayDto>> update(@PathVariable UUID id, @RequestBody HolidayDto dto) {
        dto.setId(id);
        HolidayDto result = holidayService.saveOrUpdate(dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ngày lễ thành công", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        holidayService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa ngày lễ thành công", null));
    }
}
