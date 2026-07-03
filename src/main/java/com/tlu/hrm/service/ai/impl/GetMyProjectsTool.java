package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Project;
import com.tlu.hrm.model.ProjectStaff;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.ProjectRepository;
import com.tlu.hrm.repository.ProjectStaffRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetMyProjectsTool implements AiTool {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectStaffRepository projectStaffRepository;

    @Override
    public String getName() {
        return "getMyProjects";
    }

    @Override
    public String getDescription() {
        return "Lấy danh sách các dự án trong hệ thống. Người quản lý/admin sẽ thấy tất cả dự án, nhân viên thông thường chỉ thấy các dự án họ tham gia.";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{}}";
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        boolean isManagerOrAdmin = currentUser.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getName())
                        || "HR_MANAGER".equals(ur.getRole().getName()));

        List<Project> activeProjects = new ArrayList<>();

        if (isManagerOrAdmin) {
            List<Project> all = projectRepository.findAll();
            for (Project p : all) {
                if (p.getIsDeleted() == null || !p.getIsDeleted()) {
                    activeProjects.add(p);
                }
            }
        } else {
            Staff staff = currentUser.getStaff();
            if (staff != null) {
                List<ProjectStaff> projectStaffs = projectStaffRepository.findByStaffId(staff.getId());
                for (ProjectStaff ps : projectStaffs) {
                    Project p = ps.getProject();
                    if (p != null && (p.getIsDeleted() == null || !p.getIsDeleted())) {
                        // Tránh thêm trùng lặp
                        if (activeProjects.stream().noneMatch(proj -> proj.getId().equals(p.getId()))) {
                            activeProjects.add(p);
                        }
                    }
                }
            }
        }

        if (activeProjects.isEmpty()) {
            return "{\"status\": \"success\", \"message\": \"Bạn không tham gia dự án nào hoặc hệ thống chưa có dự án.\", \"projects\": []}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\": \"success\", \"count\": ").append(activeProjects.size()).append(", \"projects\": [");
        for (int i = 0; i < activeProjects.size(); i++) {
            Project p = activeProjects.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"code\":\"").append(escapeJson(p.getCode())).append("\",");
            sb.append("\"name\":\"").append(escapeJson(p.getName())).append("\",");
            sb.append("\"description\":\"").append(p.getDescription() != null ? escapeJson(p.getDescription()) : "").append("\",");
            sb.append("\"startDate\":\"").append(p.getStartDate() != null ? p.getStartDate().toString() : "—").append("\",");
            sb.append("\"endDate\":\"").append(p.getEndDate() != null ? p.getEndDate().toString() : "—").append("\",");
            sb.append("\"isFinished\":").append(p.getIsFinished() != null ? p.getIsFinished() : "false");
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
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
