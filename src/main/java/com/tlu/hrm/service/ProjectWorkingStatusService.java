package com.tlu.hrm.service;

import com.tlu.hrm.dto.response.ProjectWorkingStatusResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectWorkingStatusService {
    List<ProjectWorkingStatusResponse> getProjectWorkingStatuses(UUID projectId, String keyword);

    ProjectWorkingStatusResponse addProjectWorkingStatus(UUID projectId, ProjectWorkingStatusResponse request);

    ProjectWorkingStatusResponse updateProjectWorkingStatus(UUID projectId, UUID statusId, ProjectWorkingStatusResponse request);

    void deleteProjectWorkingStatus(UUID projectId, UUID statusId);

    List<ProjectWorkingStatusResponse> reorderProjectWorkingStatuses(UUID projectId, List<UUID> statusIds);
}
