package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.EthnicDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.service.EthnicService;
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
@RequestMapping("/api/ethnics")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
public class EthnicController {

    @Autowired
    private EthnicService ethnicService;

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<EthnicDto>>> pagingEthnics(@RequestBody(required = false) SearchDto searchDto) {
        Page<EthnicDto> result = ethnicService.pagingEthnics(searchDto);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách dân tộc thành công", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EthnicDto>>> getAllEthnics() {
        List<EthnicDto> result = ethnicService.getAllEthnics();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách dân tộc thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EthnicDto>> getById(@PathVariable UUID id) {
        EthnicDto dto = ethnicService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin dân tộc thành công", dto));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<EthnicDto>> createEthnic(@RequestBody EthnicDto dto) {
        if (!ethnicService.isValidCode(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã dân tộc đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        EthnicDto result = ethnicService.saveEthnic(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo dân tộc thành công", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<EthnicDto>> updateEthnic(@PathVariable UUID id, @RequestBody EthnicDto dto) {
        dto.setId(id);
        if (!ethnicService.isValidCode(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã dân tộc đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        EthnicDto result = ethnicService.saveEthnic(dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật dân tộc thành công", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<Void>> deleteEthnic(@PathVariable UUID id) {
        ethnicService.deleteEthnic(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa dân tộc thành công", null));
    }
}
