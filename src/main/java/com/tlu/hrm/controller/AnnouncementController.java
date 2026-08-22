package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.AnnouncementDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.AnnouncementSearchRequest;
import com.tlu.hrm.enums.AnnouncementStatus;
import com.tlu.hrm.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.tlu.hrm.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.ROLE_ADMIN;
import static com.tlu.hrm.enums.RoleType.Constants.HR_MANAGER;

@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private SecurityUtils securityUtils;

    private boolean isManager() {
        return securityUtils.isManagerOrAdmin(securityUtils.getCurrentUser());
    }

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<AnnouncementDto>>> search(@RequestBody(required = false) AnnouncementSearchRequest request) {
        if (request == null) {
            request = new AnnouncementSearchRequest();
        }

        AnnouncementStatus status = request.getStatus();
        if (!isManager()) {
            // Nhân viên chỉ được xem thông báo đã ban hành (PUBLISHED)
            status = AnnouncementStatus.PUBLISHED;
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Page<AnnouncementDto> result = announcementService.search(
                username,
                request.getKeyword(),
                request.getCategory(),
                status,
                request.getTargetDeptId(),
                request
        );

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnouncementDto>> getById(@PathVariable UUID id) {
        AnnouncementDto result = announcementService.getById(id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        // Nếu thông báo ở trạng thái nháp (DRAFT) mà không phải HR/Admin thì từ chối hiển thị
        if (result.getStatus() == AnnouncementStatus.DRAFT && !isManager()) {
            return ResponseEntity.status(403).body(ApiResponse.error("Bạn không có quyền xem thông báo này"));
        }

        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết thông báo thành công", result));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> createOrUpdate(@RequestBody AnnouncementDto dto) {
        AnnouncementDto result = announcementService.saveOrUpdate(dto);
        if (result != null) {
            return ResponseEntity.ok(ApiResponse.success("Lưu thông báo thành công", result));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Lưu thông báo thất bại"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        announcementService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thông báo thành công", null));
    }

    @GetMapping("/generate-code")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<String>> generateCode() {
        String code = announcementService.generateAnnouncementCode();
        return ResponseEntity.ok(ApiResponse.success("Tạo mã thông báo thành công", code));
    }
}
