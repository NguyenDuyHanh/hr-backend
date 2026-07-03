package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlu.hrm.model.User;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AiServiceImpl {

    private final List<AiTool> aiTools;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public AiServiceImpl(List<AiTool> aiTools) {
        this.aiTools = aiTools;
    }

    /**
     * Lấy danh sách toàn bộ AI Tools đăng ký trong hệ thống
     */
    public List<AiTool> getTools() {
        return this.aiTools;
    }

    /**
     * Tìm và thực thi một tool cụ thể theo tên và đối số truyền vào
     */
    public String executeTool(String toolName, String argumentsStr, User currentUser) {
        Optional<AiTool> targetTool = aiTools.stream()
                .filter(t -> t.getName().equalsIgnoreCase(toolName))
                .findFirst();

        if (targetTool.isEmpty()) {
            return "{\"status\": \"error\", \"message\": \"Công cụ '" + toolName + "' không tồn tại trong hệ thống.\"}";
        }

        try {
            JsonNode arguments = objectMapper.readTree(argumentsStr);
            return targetTool.get().execute(arguments, currentUser);
        } catch (Exception e) {
            System.err.println("Lỗi khi thực thi công cụ " + toolName + ": " + e.getMessage());
            return "{\"status\": \"error\", \"message\": \"Lỗi thực thi công cụ: " + e.getMessage() + "\"}";
        }
    }

    /**
     * Tạo chuỗi JSON định nghĩa các tools phục vụ OpenAI/Groq API
     */
    public String buildToolsJsonMetadata() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < aiTools.size(); i++) {
                AiTool tool = aiTools.get(i);
                if (i > 0) sb.append(",");
                
                sb.append("{");
                sb.append("\"type\":\"function\",");
                sb.append("\"function\":{");
                sb.append("\"name\":\"").append(tool.getName()).append("\",");
                sb.append("\"description\":\"").append(escapeJson(tool.getDescription())).append("\",");
                sb.append("\"parameters\":").append(tool.getParametersJson());
                sb.append("}");
                sb.append("}");
            }
            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            return "[]";
        }
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
