package com.tlu.hrm.controller;

import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.response.ProjectWorkingStatusResponse;
import com.tlu.hrm.service.ProjectWorkingStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/projects/{projectId}/working-statuses")
@CrossOrigin(origins = "*")
public class ProjectWorkingStatusController {

    @Autowired
    private ProjectWorkingStatusService projectWorkingStatusService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "') or @projectUtils.hasProjectAccess(#projectId)")
    public ResponseEntity<ApiResponse<List<ProjectWorkingStatusResponse>>> getWorkingStatuses(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String keyword) {
        List<ProjectWorkingStatusResponse> result = projectWorkingStatusService.getProjectWorkingStatuses(projectId, keyword);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách trạng thái thành công", result));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "') or @projectUtils.hasProjectManagerAccess(#projectId)")
    public ResponseEntity<ApiResponse<ProjectWorkingStatusResponse>> addWorkingStatus(
            @PathVariable UUID projectId,
            @RequestBody ProjectWorkingStatusResponse request) {
        ProjectWorkingStatusResponse result = projectWorkingStatusService.addProjectWorkingStatus(projectId, request);
        return ResponseEntity.ok(ApiResponse.success("Thêm trạng thái thành công", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "') or @projectUtils.hasProjectManagerAccess(#projectId)")
    public ResponseEntity<ApiResponse<ProjectWorkingStatusResponse>> updateWorkingStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @RequestBody ProjectWorkingStatusResponse request) {
        ProjectWorkingStatusResponse result = projectWorkingStatusService.updateProjectWorkingStatus(projectId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "') or @projectUtils.hasProjectManagerAccess(#projectId)")
    public ResponseEntity<ApiResponse<Void>> deleteWorkingStatus(@PathVariable UUID projectId, @PathVariable UUID id) {
        projectWorkingStatusService.deleteProjectWorkingStatus(projectId, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa trạng thái thành công", null));
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "') or @projectUtils.hasProjectManagerAccess(#projectId)")
    public ResponseEntity<ApiResponse<List<ProjectWorkingStatusResponse>>> reorderWorkingStatuses(
            @PathVariable UUID projectId,
            @RequestBody List<UUID> statusIds) {
        List<ProjectWorkingStatusResponse> result = projectWorkingStatusService.reorderProjectWorkingStatuses(projectId, statusIds);
        return ResponseEntity.ok(ApiResponse.success("Sắp xếp trạng thái thành công", result));
    }
}
