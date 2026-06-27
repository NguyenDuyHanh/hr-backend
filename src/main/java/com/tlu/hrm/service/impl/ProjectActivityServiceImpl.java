package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.ProjectActivityRequest;
import com.tlu.hrm.dto.response.ProjectActivityResponse;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.model.Project;
import com.tlu.hrm.model.ProjectActivity;
import com.tlu.hrm.repository.ProjectActivityRepository;
import com.tlu.hrm.repository.ProjectRepository;
import com.tlu.hrm.service.ProjectActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectActivityServiceImpl implements ProjectActivityService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectActivityRepository projectActivityRepository;

    private Project getProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));
    }

    @Override
    public List<ProjectActivityResponse> getProjectActivities(UUID projectId, String keyword) {
        getProject(projectId);
        return projectActivityRepository.findByProjectIdOrderByDisplayOrderAsc(projectId).stream()
                .filter(a -> a.getIsDeleted() == null || !a.getIsDeleted())
                .filter(a -> {
                    if (keyword == null || keyword.trim().isEmpty()) {
                        return true;
                    }
                    String kw = keyword.toLowerCase().trim();
                    return (a.getName() != null && a.getName().toLowerCase().contains(kw)) ||
                            (a.getCode() != null && a.getCode().toLowerCase().contains(kw)) ||
                            (a.getDescription() != null && a.getDescription().toLowerCase().contains(kw));
                })
                .map(ProjectActivityResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectActivityResponse addProjectActivity(UUID projectId, ProjectActivityRequest request) {
        Project project = getProject(projectId);

        ProjectActivity activity = new ProjectActivity();
        activity.setProject(project);
        activity.setName(request.getName());
        activity.setCode(request.getCode());
        activity.setDescription(request.getDescription());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        activity.setIsDeleted(false);

        ProjectActivity saved = projectActivityRepository.save(activity);
        return new ProjectActivityResponse(saved);
    }

    @Override
    public ProjectActivityResponse updateProjectActivity(UUID projectId, UUID activityId,
            ProjectActivityRequest request) {
        ProjectActivity activity = projectActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hoạt động"));

        getProject(projectId);

        activity.setName(request.getName());
        activity.setCode(request.getCode());
        activity.setDescription(request.getDescription());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setDisplayOrder(
                request.getDisplayOrder() != null ? request.getDisplayOrder() : activity.getDisplayOrder());

        ProjectActivity saved = projectActivityRepository.save(activity);
        return new ProjectActivityResponse(saved);
    }

    @Override
    public void deleteProjectActivity(UUID projectId, UUID activityId) {
        getProject(projectId);
        ProjectActivity activity = projectActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hoạt động"));
        activity.setIsDeleted(true);
        projectActivityRepository.save(activity);
    }

    @Override
    public List<ProjectActivityResponse> reorderProjectActivities(UUID projectId, List<UUID> activityIds) {
        getProject(projectId);
        List<ProjectActivity> activities = projectActivityRepository.findByProjectId(projectId);
        for (int i = 0; i < activityIds.size(); i++) {
            UUID id = activityIds.get(i);
            int finalI = i;
            activities.stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .ifPresent(a -> {
                        a.setDisplayOrder(finalI + 1);
                        projectActivityRepository.save(a);
                    });
        }
        return getProjectActivities(projectId, null);
    }
}
