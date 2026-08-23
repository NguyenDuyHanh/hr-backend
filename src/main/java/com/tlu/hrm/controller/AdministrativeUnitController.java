package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.AdministrativeUnitDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.service.AdministrativeUnitService;
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
@RequestMapping("/api/administrative-units")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
public class AdministrativeUnitController {

    @Autowired
    private AdministrativeUnitService administrativeUnitService;

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<AdministrativeUnitDto>>> pagingAdministrativeUnits(@RequestBody(required = false) SearchDto searchDto) {
        Page<AdministrativeUnitDto> result = administrativeUnitService.pagingAdministrativeUnits(searchDto);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn vị hành chính thành công", result));
    }

    @GetMapping("/provinces")
    public ResponseEntity<ApiResponse<List<AdministrativeUnitDto>>> getAllProvinces() {
        List<AdministrativeUnitDto> result = administrativeUnitService.getAllProvinces();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách Tỉnh/Thành phố thành công", result));
    }

    @GetMapping("/by-parent-code/{parentCode}")
    public ResponseEntity<ApiResponse<List<AdministrativeUnitDto>>> getChildrenByParentCode(@PathVariable String parentCode) {
        List<AdministrativeUnitDto> result = administrativeUnitService.getChildrenByParentCode(parentCode);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn vị trực thuộc thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdministrativeUnitDto>> getById(@PathVariable UUID id) {
        AdministrativeUnitDto dto = administrativeUnitService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin đơn vị hành chính thành công", dto));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<AdministrativeUnitDto>> createAdministrativeUnit(@RequestBody AdministrativeUnitDto dto) {
        if (!administrativeUnitService.isValidCode(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã đơn vị hành chính đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        AdministrativeUnitDto result = administrativeUnitService.saveAdministrativeUnit(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo đơn vị hành chính thành công", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<AdministrativeUnitDto>> updateAdministrativeUnit(@PathVariable UUID id, @RequestBody AdministrativeUnitDto dto) {
        dto.setId(id);
        if (!administrativeUnitService.isValidCode(dto)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã đơn vị hành chính đã tồn tại", HttpStatus.BAD_REQUEST));
        }
        AdministrativeUnitDto result = administrativeUnitService.saveAdministrativeUnit(dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đơn vị hành chính thành công", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "')")
    public ResponseEntity<ApiResponse<Void>> deleteAdministrativeUnit(@PathVariable UUID id) {
        administrativeUnitService.deleteAdministrativeUnit(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa đơn vị hành chính thành công", null));
    }
}
