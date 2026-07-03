package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Project;
import com.tlu.hrm.model.ProjectStaff;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.Task;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.ProjectStaffRepository;
import com.tlu.hrm.repository.TaskRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GetMyTasksTool implements AiTool {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectStaffRepository projectStaffRepository;

    @Override
    public String getName() {
        return "getMyTasks";
    }

    @Override
    public String getDescription() {
        return "Lấy danh sách các công việc/nhiệm vụ (tasks) trong hệ thống. Có thể lọc theo mã dự án (projectCode) hoặc tên dự án (projectName) nếu muốn.";
    }

    @Override
    public String getParametersJson() {
        return """
        {
          "type": "object",
          "properties": {
            "projectCode": {
              "type": "string",
              "description": "Mã dự án muốn lọc công việc (ví dụ: PROJ001)."
            },
            "projectName": {
              "type": "string",
              "description": "Tên hoặc một phần tên dự án muốn lọc."
            }
          }
        }
        """;
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        String projectCode = arguments.has("projectCode") ? arguments.get("projectCode").asText() : null;
        String projectName = arguments.has("projectName") ? arguments.get("projectName").asText() : null;

        boolean isManagerOrAdmin = currentUser.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getName())
                        || "HR_MANAGER".equals(ur.getRole().getName()));

        List<Task> allTasks = taskRepository.findAll();
        List<Task> filteredTasks = new ArrayList<>();

        if (isManagerOrAdmin) {
            // Manager/Admin see all tasks matching filter
            for (Task t : allTasks) {
                if (t.getIsDeleted() != null && t.getIsDeleted()) {
                    continue;
                }
                if (matchProjectFilter(t.getProject(), projectCode, projectName)) {
                    filteredTasks.add(t);
                }
            }
        } else {
            Staff staff = currentUser.getStaff();
            if (staff != null) {
                // Find all projects of this staff
                List<ProjectStaff> projectStaffs = projectStaffRepository.findByStaffId(staff.getId());
                List<UUID> myProjectIds = projectStaffs.stream()
                        .map(ps -> ps.getProject().getId())
                        .collect(Collectors.toList());

                for (Task t : allTasks) {
                    if (t.getIsDeleted() != null && t.getIsDeleted()) {
                        continue;
                    }
                    // Staff can only see tasks:
                    // 1. Assigned to them
                    // 2. OR within a project they are member of
                    boolean isAssignee = t.getAssignee() != null && t.getAssignee().getId().equals(staff.getId());
                    boolean inMyProject = t.getProject() != null && myProjectIds.contains(t.getProject().getId());

                    if ((isAssignee || inMyProject) && matchProjectFilter(t.getProject(), projectCode, projectName)) {
                        filteredTasks.add(t);
                    }
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            return "{\"status\": \"success\", \"message\": \"Không tìm thấy công việc nào phù hợp.\", \"tasks\": []}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\": \"success\", \"count\": ").append(filteredTasks.size()).append(", \"tasks\": [");
        for (int i = 0; i < filteredTasks.size(); i++) {
            Task t = filteredTasks.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"id\":\"").append(t.getId()).append("\",");
            sb.append("\"code\":\"").append(t.getCode() != null ? escapeJson(t.getCode()) : "").append("\",");
            sb.append("\"name\":\"").append(escapeJson(t.getName())).append("\",");
            sb.append("\"description\":\"").append(t.getDescription() != null ? escapeJson(t.getDescription()) : "").append("\",");
            sb.append("\"priority\":").append(t.getPriority() != null ? t.getPriority() : 2).append(",");
            sb.append("\"project\":\"").append(t.getProject() != null ? escapeJson(t.getProject().getName()) : "—").append("\",");
            sb.append("\"assignee\":\"").append(t.getAssignee() != null ? escapeJson(t.getAssignee().getDisplayName()) : "Chưa giao").append("\",");
            sb.append("\"status\":\"").append(t.getStatus() != null ? escapeJson(t.getStatus().getName()) : "Mới").append("\",");
            sb.append("\"startTime\":\"").append(t.getStartTime() != null ? t.getStartTime().toString() : "—").append("\",");
            sb.append("\"endTime\":\"").append(t.getEndTime() != null ? t.getEndTime().toString() : "—").append("\"");
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private boolean matchProjectFilter(Project project, String code, String name) {
        if (project == null) {
            return code == null && name == null;
        }
        if (code != null && !code.trim().isEmpty()) {
            if (project.getCode() == null || !project.getCode().equalsIgnoreCase(code.trim())) {
                return false;
            }
        }
        if (name != null && !name.trim().isEmpty()) {
            if (project.getName() == null || !project.getName().toLowerCase().contains(name.toLowerCase().trim())) {
                return false;
            }
        }
        return true;
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
