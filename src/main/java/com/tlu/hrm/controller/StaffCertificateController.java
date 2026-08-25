package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.StaffCertificateDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.enums.QualificationType;
import com.tlu.hrm.service.StaffCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/staff-certificates")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "', '" + HR_EMPLOYEE + "')")
public class StaffCertificateController {

    @Autowired
    private StaffCertificateService staffCertificateService;

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<List<StaffCertificateDto>>> getCertificatesByStaffId(
            @PathVariable UUID staffId,
            @RequestParam(required = false) QualificationType type) {
        List<StaffCertificateDto> result = staffCertificateService.getCertificatesByStaffId(staffId, type);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bằng cấp chứng chỉ thành công", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StaffCertificateDto>> createCertificate(@RequestBody StaffCertificateDto dto) {
        StaffCertificateDto result = staffCertificateService.createCertificate(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo bằng cấp chứng chỉ thành công", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffCertificateDto>> updateCertificate(@PathVariable UUID id, @RequestBody StaffCertificateDto dto) {
        StaffCertificateDto result = staffCertificateService.updateCertificate(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bằng cấp chứng chỉ thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCertificate(@PathVariable UUID id) {
        staffCertificateService.deleteCertificate(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bằng cấp chứng chỉ thành công", null));
    }
}
