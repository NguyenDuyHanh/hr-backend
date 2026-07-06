package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Position;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.PositionRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetPositionsTool implements AiTool {

    @Autowired
    private PositionRepository positionRepository;

    @Override
    public String getName() {
        return "getPositions";
    }

    @Override
    public String getDescription() {
        return "Lấy danh sách tất cả các chức danh/vị trí công việc đang có trong hệ thống.";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{}}";
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        List<Position> positions = positionRepository.findAll();
        List<Position> active = new ArrayList<>();
        for (Position p : positions) {
            if (p.getIsDeleted() == null || !p.getIsDeleted()) {
                active.add(p);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\": \"success\", \"count\": ").append(active.size()).append(", \"positions\": [");
        for (int i = 0; i < active.size(); i++) {
            Position p = active.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"code\":\"").append(escapeJson(p.getCode())).append("\",");
            sb.append("\"name\":\"").append(escapeJson(p.getName())).append("\",");
            sb.append("\"description\":\"").append(p.getDescription() != null ? escapeJson(p.getDescription()) : "").append("\",");
            sb.append("\"department\":\"").append(p.getDepartment() != null ? escapeJson(p.getDepartment().getName()) : "").append("\"");
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
