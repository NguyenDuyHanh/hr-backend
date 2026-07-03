package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SearchStaffTool implements AiTool {

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public String getName() {
        return "searchStaff";
    }

    @Override
    public String getDescription() {
        return "Tìm kiếm danh sách nhân viên theo các tiêu chí: họ tên, tên phòng ban, tên chức danh. Sử dụng khi người dùng hỏi danh sách nhân sự của một phòng ban cụ thể hoặc muốn tìm nhân viên theo tên.";
    }

    @Override
    public String getParametersJson() {
        return """
        {
          "type": "object",
          "properties": {
            "name": {
              "type": "string",
              "description": "Họ tên hoặc một phần tên nhân viên cần tìm kiếm."
            },
            "departmentName": {
              "type": "string",
              "description": "Tên phòng ban cần lọc (ví dụ: CNTT, Nhân sự, IT, Hành chính)."
            },
            "positionName": {
              "type": "string",
              "description": "Tên chức danh/vị trí cần lọc (ví dụ: Trưởng phòng, Nhân viên, Giám đốc)."
            }
          }
        }
        """;
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        String name = arguments.has("name") ? arguments.get("name").asText() : null;
        String departmentName = arguments.has("departmentName") ? arguments.get("departmentName").asText() : null;
        String positionName = arguments.has("positionName") ? arguments.get("positionName").asText() : null;

        List<Staff> staffs = staffRepository.findAll();
        List<Staff> filtered = new ArrayList<>();
        
        // RBAC: HR_EMPLOYEE chỉ tìm thấy chính mình hoặc danh sách nhân viên khác nhưng giới hạn thông tin nhạy cảm.
        // Ở đây để đơn giản và an toàn, HR_EMPLOYEE vẫn được tìm kiếm mọi người nhưng thông tin nhạy cảm sẽ không được trả về ở execute.
        boolean isManagerOrAdmin = currentUser.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getName())
                        || "HR_MANAGER".equals(ur.getRole().getName()));

        for (Staff s : staffs) {
            if (s.getIsDeleted() != null && s.getIsDeleted()) {
                continue;
            }
            boolean match = true;
            if (name != null && !name.trim().isEmpty()) {
                if (s.getDisplayName() == null || !s.getDisplayName().toLowerCase().contains(name.toLowerCase().trim())) {
                    match = false;
                }
            }
            if (departmentName != null && !departmentName.trim().isEmpty()) {
                if (s.getDepartment() == null || s.getDepartment().getName() == null || 
                        !s.getDepartment().getName().toLowerCase().contains(departmentName.toLowerCase().trim())) {
                    match = false;
                }
            }
            if (positionName != null && !positionName.trim().isEmpty()) {
                if (s.getPosition() == null || s.getPosition().getName() == null || 
                        !s.getPosition().getName().toLowerCase().contains(positionName.toLowerCase().trim())) {
                    match = false;
                }
            }
            if (match) {
                filtered.add(s);
            }
        }

        if (filtered.isEmpty()) {
            return "{\"status\": \"success\", \"message\": \"Không tìm thấy nhân viên nào phù hợp tiêu chí tìm kiếm.\", \"staffs\": []}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\": \"success\", \"count\": ").append(filtered.size()).append(", \"staffs\": [");
        for (int i = 0; i < filtered.size(); i++) {
            Staff s = filtered.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"staffCode\":\"").append(escapeJson(s.getStaffCode())).append("\",");
            sb.append("\"displayName\":\"").append(escapeJson(s.getDisplayName())).append("\",");
            sb.append("\"department\":\"").append(s.getDepartment() != null ? escapeJson(s.getDepartment().getName()) : "—").append("\",");
            sb.append("\"position\":\"").append(s.getPosition() != null ? escapeJson(s.getPosition().getName()) : "—").append("\"");
            
            // Chỉ trả về email, sđt nếu là Manager/Admin
            if (isManagerOrAdmin) {
                sb.append(",\"email\":\"").append(s.getEmail() != null ? escapeJson(s.getEmail()) : "—").append("\",");
                sb.append("\"phoneNumber\":\"").append(s.getPhoneNumber() != null ? escapeJson(s.getPhoneNumber()) : "—").append("\"");
            }
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
