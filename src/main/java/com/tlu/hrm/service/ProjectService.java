package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.ProjectCreateRequest;
import com.tlu.hrm.dto.request.StaffDto;
import com.tlu.hrm.dto.response.ProjectResponse;
import com.tlu.hrm.dto.search.ProjectSearchRequest;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.UUID;

public interface ProjectService {
    Page<ProjectResponse> getAllProjects(ProjectSearchRequest request);

    List<ProjectResponse> getAllProjectsUnpaginated();

    ProjectResponse getProjectById(UUID id);

    ProjectResponse saveProject(ProjectCreateRequest request);

    void deleteProject(UUID id);

    void finishProject(UUID id);

    void unfinishProject(UUID id);

    List<StaffDto> getProjectStaffs(UUID projectId);
}
