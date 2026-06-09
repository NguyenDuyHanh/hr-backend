package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.TaskRequest;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.response.TaskAttachmentResponse;
import com.tlu.hrm.dto.response.TaskResponse;
import com.tlu.hrm.dto.search.TaskSearchRequest;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.model.Task;
import com.tlu.hrm.model.TaskAttachment;
import com.tlu.hrm.repository.TaskAttachmentRepository;
import com.tlu.hrm.repository.TaskRepository;
import com.tlu.hrm.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAttachmentRepository taskAttachmentRepository;

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> searchTasks(@RequestBody(required = false) TaskSearchRequest request) {
        if (request == null) request = new TaskSearchRequest();
        Page<TaskResponse> result = taskService.searchTasks(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách công việc thành công", result));
    }

    @PostMapping("/kanban")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksForKanban(@RequestBody(required = false) TaskSearchRequest request) {
        if (request == null) request = new TaskSearchRequest();
        List<TaskResponse> result = taskService.getTasksForKanban(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách Kanban thành công", result));
    }

    @PostMapping("/my-tasks")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getMyTasks(@RequestBody(required = false) TaskSearchRequest request) {
        if (request == null) request = new TaskSearchRequest();
        Page<TaskResponse> result = taskService.getMyTasks(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách công việc của tôi thành công", result));
    }

    @GetMapping("/project/{projectId}/count-by-status")
    public ResponseEntity<ApiResponse<java.util.Map<UUID, Long>>> countTasksByStatus(@PathVariable UUID projectId) {
        java.util.Map<UUID, Long> result = taskService.countTasksByStatus(projectId);
        return ResponseEntity.ok(ApiResponse.success("Đếm số lượng công việc theo trạng thái thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable UUID id) {
        TaskResponse result = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết công việc thành công", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@RequestBody TaskRequest request) {
        request.setId(null);
        TaskResponse result = taskService.saveTask(request);
        return ResponseEntity.ok(ApiResponse.success("Tạo công việc thành công", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(@PathVariable UUID id, @RequestBody TaskRequest request) {
        request.setId(id);
        TaskResponse result = taskService.saveTask(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật công việc thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa công việc thành công", null));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable UUID id,
            @RequestParam UUID statusId) {
        TaskResponse result = taskService.updateTaskStatus(id, statusId);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái công việc thành công", result));
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<ApiResponse<TaskAttachmentResponse>> uploadAttachment(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) throws IOException {
        Task task = taskRepository.findById(id)
                .filter(t -> t.getVoided() == null || !t.getVoided())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + id));

        // Lưu file vật lý
        String uploadDir = "uploads/tasks/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String savedFilename = UUID.randomUUID().toString() + extension;
        String filePath = uploadDir + savedFilename;
        Files.copy(file.getInputStream(), Paths.get(filePath));

        TaskAttachment attachment = new TaskAttachment();
        attachment.setTask(task);
        attachment.setName(originalFilename);
        attachment.setSize(file.getSize());
        attachment.setFilePath(filePath);
        attachment.setVoided(false);

        TaskAttachment savedAttachment = taskAttachmentRepository.save(attachment);
        return ResponseEntity.ok(ApiResponse.success("Tải tập tin lên thành công", new TaskAttachmentResponse(savedAttachment)));
    }

    @PostMapping("/{id}/attachments/link")
    public ResponseEntity<ApiResponse<TaskAttachmentResponse>> addAttachmentLink(
            @PathVariable UUID id,
            @RequestParam("name") String name,
            @RequestParam("size") Long size,
            @RequestParam("filePath") String filePath) {
        Task task = taskRepository.findById(id)
                .filter(t -> t.getVoided() == null || !t.getVoided())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công việc với ID: " + id));

        TaskAttachment attachment = new TaskAttachment();
        attachment.setTask(task);
        attachment.setName(name);
        attachment.setSize(size);
        attachment.setFilePath(filePath);
        attachment.setVoided(false);

        TaskAttachment savedAttachment = taskAttachmentRepository.save(attachment);
        return ResponseEntity.ok(ApiResponse.success("Lưu tập tin đính kèm thành công", new TaskAttachmentResponse(savedAttachment)));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable UUID attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tập tin đính kèm với ID: " + attachmentId));
        attachment.setVoided(true);
        taskAttachmentRepository.save(attachment);
        return ResponseEntity.ok(ApiResponse.success("Xóa tập tin đính kèm thành công", null));
    }
}
