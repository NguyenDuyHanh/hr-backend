package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.response.TaskHistoryResponse;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.Task;
import com.tlu.hrm.model.TaskHistory;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.TaskHistoryRepository;
import com.tlu.hrm.repository.TaskRepository;
import com.tlu.hrm.security.SecurityUtils;
import com.tlu.hrm.service.TaskHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskHistoryServiceImpl implements TaskHistoryService {

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Override
    public List<TaskHistoryResponse> getHistoryByTaskId(UUID taskId) {
        return taskHistoryRepository.findByTaskIdAndVoidedFalseOrderByCreateDateDesc(taskId).stream()
                .map(TaskHistoryResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public TaskHistoryResponse addComment(UUID taskId, String comment) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + taskId));

        User currentUser = securityUtils.getCurrentUser();
        Staff modifier = currentUser != null ? currentUser.getStaff() : null;

        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setModifier(modifier);
        history.setComment(comment);
        history.setVoided(false);

        TaskHistory saved = taskHistoryRepository.save(history);
        return new TaskHistoryResponse(saved);
    }

    @Override
    public TaskHistoryResponse updateComment(UUID id, String comment) {
        TaskHistory history = taskHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận với ID: " + id));

        User currentUser = securityUtils.getCurrentUser();
        Staff currentStaff = currentUser != null ? currentUser.getStaff() : null;

        // Chỉ cho phép người viết bình luận hoặc Admin/Manager cập nhật
        if (history.getModifier() != null && currentStaff != null && 
            !history.getModifier().getId().equals(currentStaff.getId()) &&
            !securityUtils.isManagerOrAdmin(currentUser)) {
            throw new AccessDeniedException("Bạn không có quyền sửa bình luận này");
        }

        history.setComment(comment);
        TaskHistory saved = taskHistoryRepository.save(history);
        return new TaskHistoryResponse(saved);
    }

    @Override
    public void deleteComment(UUID id) {
        TaskHistory history = taskHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận với ID: " + id));

        User currentUser = securityUtils.getCurrentUser();
        Staff currentStaff = currentUser != null ? currentUser.getStaff() : null;

        // Chỉ cho phép người viết bình luận hoặc Admin/Manager xóa
        if (history.getModifier() != null && currentStaff != null && 
            !history.getModifier().getId().equals(currentStaff.getId()) &&
            !securityUtils.isManagerOrAdmin(currentUser)) {
            throw new AccessDeniedException("Bạn không có quyền xóa bình luận này");
        }

        history.setVoided(true);
        taskHistoryRepository.save(history);
    }

    @Override
    public void logEvent(UUID taskId, String event) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return;

        User currentUser = securityUtils.getCurrentUser();
        Staff modifier = currentUser != null ? currentUser.getStaff() : null;

        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setModifier(modifier);
        history.setEvent(event);
        history.setVoided(false);

        taskHistoryRepository.save(history);
    }
}
