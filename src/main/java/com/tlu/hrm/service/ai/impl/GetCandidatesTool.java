package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Candidate;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.CandidateRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetCandidatesTool implements AiTool {

    @Autowired
    private CandidateRepository candidateRepository;

    @Override
    public String getName() {
        return "getCandidates";
    }

    @Override
    public String getDescription() {
        return "Lấy danh sách các ứng viên tuyển dụng trong hệ thống.";
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

        List<Candidate> all = candidateRepository.findAll();
        List<Candidate> active = new ArrayList<>();
        for (Candidate c : all) {
            if (c.getIsDeleted() == null || !c.getIsDeleted()) {
                active.add(c);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\": \"success\", \"count\": ").append(active.size()).append(", \"candidates\": [");
        for (int i = 0; i < active.size(); i++) {
            Candidate c = active.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"candidateCode\":\"").append(c.getCandidateCode() != null ? escapeJson(c.getCandidateCode()) : "").append("\",");
            sb.append("\"displayName\":\"").append(escapeJson(c.getDisplayName())).append("\",");
            sb.append("\"gender\":\"").append(c.getGender() != null ? escapeJson(c.getGender().name()) : "").append("\",");
            sb.append("\"email\":\"").append(c.getEmail() != null ? escapeJson(c.getEmail()) : "").append("\",");
            sb.append("\"phoneNumber\":\"").append(c.getPhoneNumber() != null ? escapeJson(c.getPhoneNumber()) : "").append("\",");
            sb.append("\"status\":\"").append(c.getStatus() != null ? escapeJson(c.getStatus().name()) : "").append("\",");
            sb.append("\"recruitment\":\"").append(c.getRecruitment() != null ? escapeJson(c.getRecruitment().getName()) : "").append("\"");
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
