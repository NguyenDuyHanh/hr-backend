package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.ProjectCreateRequest;
import com.tlu.hrm.dto.request.ProjectStaffRequest;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.response.ProjectResponse;
import com.tlu.hrm.dto.response.ProjectStaffDto;
import com.tlu.hrm.dto.search.ProjectSearchRequest;
import com.tlu.hrm.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.tlu.hrm.enums.RoleType.Constants.*;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getAllProjects(@RequestBody(required = false) ProjectSearchRequest request) {
        Page<ProjectResponse> result = projectService.getAllProjects(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách dự án thành công", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjectsUnpaginated() {
        List<ProjectResponse> result = projectService.getAllProjectsUnpaginated();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách dự án thành công", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "') or @projectUtils.hasProjectAccess(#id)")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable UUID id) {
        ProjectResponse result = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin dự án thành công", result));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@RequestBody ProjectCreateRequest request) {
        ProjectResponse result = projectService.saveProject(request);
        return ResponseEntity.ok(ApiResponse.success("Thêm dự án thành công", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "') or @projectUtils.hasProjectManagerAccess(#id)")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(@PathVariable UUID id, @RequestBody ProjectCreateRequest request) {
        request.setId(id);
        ProjectResponse result = projectService.saveProject(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật dự án thành công", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "') or @projectUtils.hasProjectManagerAccess(#id)")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa dự án thành công", null));
    }

    @PutMapping("/{id}/finish")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "') or @projectUtils.hasProjectManagerAccess(#id)")
    public ResponseEntity<ApiResponse<Void>> finishProject(@PathVariable UUID id) {
        projectService.finishProject(id);
        return ResponseEntity.ok(ApiResponse.success("Hoàn thành dự án thành công", null));
    }

    @PutMapping("/{id}/unfinish")
    @PreAuthorize("hasAnyAuthority('" + ROLE_ADMIN + "', '" + HR_MANAGER + "') or @projectUtils.hasProjectManagerAccess(#id)")
    public ResponseEntity<ApiResponse<Void>> unfinishProject(@PathVariable UUID id) {
        projectService.unfinishProject(id);
        return ResponseEntity.ok(ApiResponse.success("Bỏ đánh dấu hoàn thành dự án thành công", null));
    }

}
