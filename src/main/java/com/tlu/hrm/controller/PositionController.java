package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.PositionDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.service.PositionService;
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
@RequestMapping("/api/positions")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<PositionDto>>> pagingPositions(
            @RequestBody(required = false) SearchDto searchDto) {
        Page<PositionDto> result = positionService.pagingPositions(searchDto);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách vị trí thành công", result));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PositionDto>>> getAllPositions() {
        List<PositionDto> result = positionService.getAllPositions();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách vị trí thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PositionDto>> getById(@PathVariable UUID id) {
        PositionDto dto = positionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin vị trí thành công", dto));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<PositionDto>> createPosition(@RequestBody PositionDto dto) {
        if (!positionService.isValidCode(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã vị trí đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        PositionDto result = positionService.savePosition(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo vị trí thành công", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<PositionDto>> updatePosition(@PathVariable UUID id,
            @RequestBody PositionDto dto) {
        dto.setId(id);
        if (!positionService.isValidCode(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã vị trí đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        PositionDto result = positionService.savePosition(dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật vị trí thành công", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<Void>> deletePosition(@PathVariable UUID id) {
        positionService.deletePosition(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa vị trí thành công", null));
    }

    @PostMapping("/delete-multiple")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<Void>> deleteMultiple(@RequestBody List<UUID> ids) {
        positionService.deleteMultiple(ids);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh sách vị trí thành công", null));
    }

    @GetMapping("/generate-code")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<String>> generateCode() {
        String code = positionService.generateCode();
        return ResponseEntity.ok(ApiResponse.success("Tạo mã vị trí thành công", code));
    }
}
