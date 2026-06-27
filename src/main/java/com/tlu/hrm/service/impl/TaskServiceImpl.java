package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.TaskRequest;
import com.tlu.hrm.dto.response.TaskResponse;
import com.tlu.hrm.dto.search.TaskSearchRequest;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.model.*;
import com.tlu.hrm.repository.*;
import com.tlu.hrm.security.SecurityUtils;
import com.tlu.hrm.service.TaskService;
import com.tlu.hrm.service.TaskHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectWorkingStatusRepository projectWorkingStatusRepository;

    @Autowired
    private ProjectActivityRepository projectActivityRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private TaskHistoryService taskHistoryService;

    @Override
    public Page<TaskResponse> searchTasks(TaskSearchRequest request) {
        if (request == null) {
            return null;
        }
        List<Task> filteredList = getFilteredTasksList(request);

        int total = filteredList.size();
        int pageNum = request.getPageIndex() >= 1 ? request.getPageIndex() - 1 : 0;
        int size = request.getPageSize() > 0 ? request.getPageSize() : 10;

        int fromIndex = pageNum * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<TaskResponse> pageContent = new ArrayList<>();
        if (fromIndex < total) {
            pageContent = filteredList.subList(fromIndex, toIndex).stream()
                    .map(TaskResponse::new)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(pageContent, PageRequest.of(pageNum, size), total);
    }

    @Override
    public TaskResponse getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .filter(t -> t.getIsDeleted() == null || !t.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + id));
        return new TaskResponse(task);
    }

    @Override
    public TaskResponse saveTask(TaskRequest request) {
        Task task;
        boolean isNew = false;
        String oldStatusName = null;
        String oldAssigneeName = null;

        if (request.getId() != null) {
            task = taskRepository.findById(request.getId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + request.getId()));
            if (task.getStatus() != null)
                oldStatusName = task.getStatus().getName();
            if (task.getAssignee() != null)
                oldAssigneeName = task.getAssignee().getDisplayName();
        } else {
            task = new Task();
            isNew = true;
        }

        task.setName(request.getName());
        task.setDescription(request.getDescription());

        task.setPriority(request.getPriority());
        task.setStartTime(request.getStartTime());
        task.setEndTime(request.getEndTime());
        task.setEstimateHour(request.getEstimateHour());

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy dự án với ID: " + request.getProjectId()));
            task.setProject(project);

            if (isNew && task.getCode() == null) {
                String prefix = project.getCode() != null ? project.getCode() + "-" : "TASK-";
                List<Task> existingTasks = taskRepository.findByProjectId(project.getId());
                long maxStt = 0;
                for (Task existingTask : existingTasks) {
                    String existingCode = existingTask.getCode();
                    if (existingCode != null && existingCode.startsWith(prefix)) {
                        try {
                            String suffix = existingCode.substring(prefix.length());
                            long stt = Long.parseLong(suffix);
                            if (stt > maxStt) {
                                maxStt = stt;
                            }
                        } catch (NumberFormatException e) {
                            // Ignored
                        }
                    }
                }
                task.setCode(prefix + (maxStt + 1));
            }
        }

        if (request.getActivityId() != null) {
            ProjectActivity activity = projectActivityRepository.findById(request.getActivityId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy hoạt động dự án với ID: " + request.getActivityId()));
            task.setActivity(activity);
        } else {
            task.setActivity(null);
        }

        if (request.getStatusId() != null) {
            ProjectWorkingStatus status = projectWorkingStatusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy trạng thái với ID: " + request.getStatusId()));
            task.setStatus(status);
        } else if (isNew && request.getProjectId() != null) {
            List<ProjectWorkingStatus> statuses = projectWorkingStatusRepository
                    .findByProjectIdOrderByDisplayOrderAsc(request.getProjectId());
            if (!statuses.isEmpty()) {
                task.setStatus(statuses.get(0));
            }
        }

        if (request.getAssigneeId() != null) {
            Staff assignee = staffRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy nhân viên phụ trách với ID: " + request.getAssigneeId()));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }



        task.setIsDeleted(false);
        Task saved = taskRepository.save(task);

        // Lịch sử / Event logging
        if (isNew) {
            taskHistoryService.logEvent(saved.getId(), "Tạo mới công việc: " + saved.getName());
        } else {
            List<String> changes = new ArrayList<>();
            if (request.getName() != null && !request.getName().equals(saved.getName())) {
                changes.add("Đổi tên công việc");
            }
            String newStatusName = saved.getStatus() != null ? saved.getStatus().getName() : null;
            if (newStatusName != null && !newStatusName.equals(oldStatusName)) {
                changes.add("Đổi trạng thái từ '" + (oldStatusName != null ? oldStatusName : "N/A") + "' sang '"
                        + newStatusName + "'");
            }
            String newAssigneeName = saved.getAssignee() != null ? saved.getAssignee().getDisplayName() : null;
            if (newAssigneeName != null && !newAssigneeName.equals(oldAssigneeName)) {
                changes.add("Thay đổi người phụ trách thành '" + newAssigneeName + "'");
            }

            if (!changes.isEmpty()) {
                taskHistoryService.logEvent(saved.getId(), "Cập nhật công việc: " + String.join(", ", changes));
            }
        }

        return new TaskResponse(saved);
    }

    @Override
    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + id));
        task.setIsDeleted(true);
        taskRepository.save(task);
        taskHistoryService.logEvent(id, "Đã xóa công việc");
    }

    @Override
    public TaskResponse updateTaskStatus(UUID taskId, UUID statusId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + taskId));
        ProjectWorkingStatus status = projectWorkingStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái với ID: " + statusId));

        String oldStatusName = task.getStatus() != null ? task.getStatus().getName() : "N/A";
        task.setStatus(status);
        Task saved = taskRepository.save(task);

        taskHistoryService.logEvent(taskId,
                "Đổi trạng thái từ '" + oldStatusName + "' sang '" + status.getName() + "'");
        return new TaskResponse(saved);
    }

    @Override
    public Page<TaskResponse> getMyTasks(TaskSearchRequest request) {
        if (request == null) {
            return null;
        }
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getStaff() == null) {
            return null;
        }
        request.setAssigneeId(currentUser.getStaff().getId());
        return searchTasks(request);
    }

    @Override
    public Map<UUID, Long> countTasksByStatus(TaskSearchRequest request) {
        if (request == null || request.getProjectId() == null) {
            return null;
        }
        UUID projectId = request.getProjectId();
        Map<UUID, Long> countMap = new HashMap<>();

        // Khởi tạo các status của dự án với count = 0
        List<ProjectWorkingStatus> statuses = projectWorkingStatusRepository.findByProjectId(projectId);
        for (ProjectWorkingStatus status : statuses) {
            countMap.put(status.getId(), 0L);
        }

        // Tạo request clone để loại bỏ filter statusIds khi đếm (để đếm được tất cả
        // trạng thái)
        TaskSearchRequest countRequest = new TaskSearchRequest();
        countRequest.setProjectId(projectId);
        countRequest.setAssigneeId(request.getAssigneeId());

        countRequest.setActivityIds(request.getActivityIds());
        countRequest.setPriorities(request.getPriorities());
        countRequest.setStartCreatedDate(request.getStartCreatedDate());
        countRequest.setEndCreatedDate(request.getEndCreatedDate());
        countRequest.setKeyword(request.getKeyword());

        List<Task> filteredTasks = getFilteredTasksList(countRequest);

        // Đếm các task
        for (Task task : filteredTasks) {
            if (task.getStatus() != null) {
                UUID statusId = task.getStatus().getId();
                if (countMap.containsKey(statusId)) {
                    countMap.put(statusId, countMap.get(statusId) + 1L);
                }
            }
        }

        return countMap;
    }

    private List<Task> getFilteredTasksList(TaskSearchRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        boolean isManager = securityUtils.isManagerOrAdmin(currentUser);
        Staff currentStaff = (currentUser != null) ? currentUser.getStaff() : null;

        return taskRepository.findAll().stream()
                .filter(t -> t.getIsDeleted() == null || !t.getIsDeleted())
                .filter(t -> {
                    // Employee chỉ xem được task của dự án họ tham gia
                    if (!isManager) {
                        if (currentStaff == null)
                            return false;
                        Project project = t.getProject();
                        if (project == null)
                            return false;

                        // Check xem user có thuộc dự án không
                        boolean isMemberOfProject = project.getProjectStaffs().stream()
                                .filter(ps -> ps.getIsDeleted() == null || !ps.getIsDeleted())
                                .anyMatch(ps -> ps.getStaff() != null
                                        && ps.getStaff().getId().equals(currentStaff.getId()));
                        if (!isMemberOfProject) {
                            return false;
                        }

                        // Check xem user có phải Project Manager của dự án này không
                        boolean isProjectManager = project.getProjectStaffs().stream()
                                .filter(ps -> ps.getIsDeleted() == null || !ps.getIsDeleted())
                                .anyMatch(ps -> ps.getStaff() != null
                                        && ps.getStaff().getId().equals(currentStaff.getId())
                                        && com.tlu.hrm.enums.ProjectRole.MANAGER.equals(ps.getProjectRole()));

                        // Nếu không phải Project Manager, chỉ được xem task của chính mình (assignee)
                        if (!isProjectManager) {
                            return t.getAssignee() != null
                                    && t.getAssignee().getId().equals(currentStaff.getId());
                        }
                    }
                    return true;
                })
                .filter(t -> {
                    if (request != null) {
                        if (request.getProjectId() != null) {
                            if (t.getProject() == null || !t.getProject().getId().equals(request.getProjectId())) {
                                return false;
                            }
                        }
                        if (request.getAssigneeId() != null) {
                            if (t.getAssignee() == null || !t.getAssignee().getId().equals(request.getAssigneeId())) {
                                return false;
                            }
                        }

                        if (request.getStatusIds() != null && !request.getStatusIds().isEmpty()) {
                            if (t.getStatus() == null || !request.getStatusIds().contains(t.getStatus().getId())) {
                                return false;
                            }
                        }
                        if (request.getActivityIds() != null && !request.getActivityIds().isEmpty()) {
                            if (t.getActivity() == null
                                    || !request.getActivityIds().contains(t.getActivity().getId())) {
                                return false;
                            }
                        }
                        if (request.getPriorities() != null && !request.getPriorities().isEmpty()) {
                            if (t.getPriority() == null || !request.getPriorities().contains(t.getPriority())) {
                                return false;
                            }
                        }
                        if (request.getStartCreatedDate() != null) {
                            if (t.getCreateDate() == null
                                    || t.getCreateDate().isBefore(request.getStartCreatedDate())) {
                                return false;
                            }
                        }
                        if (request.getEndCreatedDate() != null) {
                            if (t.getCreateDate() == null || t.getCreateDate().isAfter(request.getEndCreatedDate())) {
                                return false;
                            }
                        }
                        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                            String kw = request.getKeyword().toLowerCase();
                            boolean nameMatch = t.getName() != null && t.getName().toLowerCase().contains(kw);
                            boolean codeMatch = t.getCode() != null && t.getCode().toLowerCase().contains(kw);
                            if (!nameMatch && !codeMatch) {
                                return false;
                            }
                        }
                    }
                    return true;
                })
                .sorted((t1, t2) -> {
                    // Sorting
                    if (request != null && request.getSortBy() != null) {
                        int comp = 0;
                        if ("createDate".equals(request.getSortBy())) {
                            comp = t1.getCreateDate().compareTo(t2.getCreateDate());
                        } else if ("name".equals(request.getSortBy())) {
                            comp = t1.getName().compareToIgnoreCase(t2.getName());
                        } else if ("code".equals(request.getSortBy())) {
                            comp = (t1.getCode() != null && t2.getCode() != null) ? t1.getCode().compareTo(t2.getCode()) : 0;
                        } else if ("priority".equals(request.getSortBy())) {
                            comp = t1.getPriority().compareTo(t2.getPriority());
                        }
                        return Boolean.TRUE.equals(request.getSortDesc()) ? -comp : comp;
                    }
                    // Default sort: latest created first
                    return t2.getCreateDate().compareTo(t1.getCreateDate());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getTasksForKanban(TaskSearchRequest request) {
        if (request == null || request.getProjectId() == null) {
            return null;
        }
        UUID projectId = request.getProjectId();

        List<ProjectWorkingStatus> statuses = projectWorkingStatusRepository
                .findByProjectIdOrderByDisplayOrderAsc(projectId);

        List<TaskResponse> allTasks = new ArrayList<>();

        for (ProjectWorkingStatus status : statuses) {
            TaskSearchRequest statusRequest = new TaskSearchRequest();
            statusRequest.setProjectId(projectId);
            statusRequest.setAssigneeId(request.getAssigneeId());

            statusRequest.setActivityIds(request.getActivityIds());
            statusRequest.setPriorities(request.getPriorities());
            statusRequest.setKeyword(request.getKeyword());
            statusRequest.setStatusIds(Collections.singletonList(status.getId()));

            statusRequest.setPageIndex(1);
            statusRequest.setPageSize(request.getPageSize() > 0 ? request.getPageSize() : 10);

            Page<TaskResponse> page = searchTasks(statusRequest);
            if (page.getContent() != null && !page.getContent().isEmpty()) {
                allTasks.addAll(page.getContent());
            }
        }

        return allTasks;
    }
}
