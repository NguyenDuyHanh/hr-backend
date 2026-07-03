package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Department;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.DepartmentRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetDepartmentsTool implements AiTool {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public String getName() {
        return "getDepartments";
    }

    @Override
    public String getDescription() {
        return "Lấy danh sách tất cả các phòng ban trong hệ thống.";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{}}";
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        List<Department> depts = departmentRepository.findAll();
        List<Department> activeDepts = new ArrayList<>();
        for (Department d : depts) {
            if (d.getIsDeleted() == null || !d.getIsDeleted()) {
                activeDepts.add(d);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\": \"success\", \"count\": ").append(activeDepts.size()).append(", \"departments\": [");
        for (int i = 0; i < activeDepts.size(); i++) {
            Department d = activeDepts.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"code\":\"").append(escapeJson(d.getCode())).append("\",");
            sb.append("\"name\":\"").append(escapeJson(d.getName())).append("\",");
            sb.append("\"description\":\"").append(d.getDescription() != null ? escapeJson(d.getDescription()) : "").append("\"");
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
