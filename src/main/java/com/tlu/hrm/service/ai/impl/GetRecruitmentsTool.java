package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Recruitment;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.RecruitmentRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetRecruitmentsTool implements AiTool {

    @Autowired
    private RecruitmentRepository recruitmentRepository;

    @Override
    public String getName() {
        return "getRecruitments";
    }

    @Override
    public String getDescription() {
        return "Lấy danh sách các tin tuyển dụng (vị trí tuyển dụng) hiện có trong hệ thống.";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{}}";
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        boolean hasAccess = currentUser.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getName())
                        || "HR_MANAGER".equals(ur.getRole().getName())
                        || "HR_RECRUITMENT".equals(ur.getRole().getName()));

        if (!hasAccess) {
            return "{\"status\": \"error\", \"message\": \"Bạn không có quyền thực hiện hành động này.\"}";
        }

        List<Recruitment> all = recruitmentRepository.findAll();
        List<Recruitment> active = new ArrayList<>();
        for (Recruitment r : all) {
            if (r.getIsDeleted() == null || !r.getIsDeleted()) {
                active.add(r);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\": \"success\", \"count\": ").append(active.size()).append(", \"recruitments\": [");
        for (int i = 0; i < active.size(); i++) {
            Recruitment r = active.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"code\":\"").append(escapeJson(r.getCode())).append("\",");
            sb.append("\"name\":\"").append(escapeJson(r.getName())).append("\",");
            sb.append("\"description\":\"").append(r.getDescription() != null ? escapeJson(r.getDescription()) : "").append("\",");
            sb.append("\"status\":\"").append(r.getStatus() != null ? escapeJson(r.getStatus().getLabel()) : "Mới").append("\"");
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
