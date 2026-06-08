package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.ProjectActivityRequest;
import com.tlu.hrm.dto.response.ProjectActivityResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectActivityService {
    List<ProjectActivityResponse> getProjectActivities(UUID projectId, String keyword);

    ProjectActivityResponse addProjectActivity(UUID projectId, ProjectActivityRequest request);

    ProjectActivityResponse updateProjectActivity(UUID projectId, UUID activityId, ProjectActivityRequest request);

    void deleteProjectActivity(UUID projectId, UUID activityId);

    List<ProjectActivityResponse> reorderProjectActivities(UUID projectId, List<UUID> activityIds);
}
