package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.response.ProjectWorkingStatusResponse;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.model.Project;
import com.tlu.hrm.model.ProjectWorkingStatus;
import com.tlu.hrm.repository.ProjectRepository;
import com.tlu.hrm.repository.ProjectWorkingStatusRepository;
import com.tlu.hrm.service.ProjectWorkingStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectWorkingStatusServiceImpl implements ProjectWorkingStatusService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectWorkingStatusRepository projectWorkingStatusRepository;

    private Project getProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));
    }

    @Override
    public List<ProjectWorkingStatusResponse> getProjectWorkingStatuses(UUID projectId, String keyword) {
        getProject(projectId);
        return projectWorkingStatusRepository.findByProjectIdOrderByDisplayOrderAsc(projectId).stream()
                .filter(ws -> ws.getIsDeleted() == null || !ws.getIsDeleted())
                .filter(ws -> {
                    if (keyword == null || keyword.trim().isEmpty()) {
                        return true;
                    }
                    String kw = keyword.toLowerCase().trim();
                    return (ws.getName() != null && ws.getName().toLowerCase().contains(kw)) ||
                            (ws.getCode() != null && ws.getCode().toLowerCase().contains(kw)) ||
                            (ws.getDescription() != null && ws.getDescription().toLowerCase().contains(kw));
                })
                .map(ProjectWorkingStatusResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectWorkingStatusResponse addProjectWorkingStatus(UUID projectId, ProjectWorkingStatusResponse request) {
        Project project = getProject(projectId);

        ProjectWorkingStatus ws = new ProjectWorkingStatus();
        ws.setProject(project);
        ws.setName(request.getName());
        ws.setCode(request.getCode());
        ws.setDescription(request.getDescription());
        ws.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        ws.setColor(request.getColor() != null ? request.getColor() : "#9e9e9e");
        ws.setIsDeleted(false);

        ProjectWorkingStatus saved = projectWorkingStatusRepository.save(ws);
        return new ProjectWorkingStatusResponse(saved);
    }

    @Override
    public ProjectWorkingStatusResponse updateProjectWorkingStatus(UUID projectId, UUID statusId,
            ProjectWorkingStatusResponse request) {
        getProject(projectId);
        ProjectWorkingStatus ws = projectWorkingStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái công việc"));

        ws.setName(request.getName());
        ws.setCode(request.getCode());
        ws.setDescription(request.getDescription());
        ws.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : ws.getDisplayOrder());
        ws.setColor(request.getColor() != null ? request.getColor() : ws.getColor());

        ProjectWorkingStatus saved = projectWorkingStatusRepository.save(ws);
        return new ProjectWorkingStatusResponse(saved);
    }

    @Override
    public void deleteProjectWorkingStatus(UUID projectId, UUID statusId) {
        getProject(projectId);
        ProjectWorkingStatus ws = projectWorkingStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái công việc"));
        ws.setIsDeleted(true);
        projectWorkingStatusRepository.save(ws);
    }

    @Override
    public List<ProjectWorkingStatusResponse> reorderProjectWorkingStatuses(UUID projectId, List<UUID> statusIds) {
        getProject(projectId);
        List<ProjectWorkingStatus> statuses = projectWorkingStatusRepository.findByProjectId(projectId);
        for (int i = 0; i < statusIds.size(); i++) {
            UUID id = statusIds.get(i);
            int finalI = i;
            statuses.stream()
                    .filter(ws -> ws.getId().equals(id))
                    .findFirst()
                    .ifPresent(ws -> {
                        ws.setDisplayOrder(finalI + 1);
                        projectWorkingStatusRepository.save(ws);
                    });
        }
        return getProjectWorkingStatuses(projectId, null);
    }
}
