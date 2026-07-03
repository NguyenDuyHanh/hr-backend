package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetStaffProfileTool implements AiTool {

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public String getName() {
        return "getStaffProfile";
    }

    @Override
    public String getDescription() {
        return "Lấy thông tin hồ sơ chi tiết của một nhân viên cụ thể theo mã số nhân viên (staffCode).";
    }

    @Override
    public String getParametersJson() {
        return """
        {
          "type": "object",
          "properties": {
            "staffCode": {
              "type": "string",
              "description": "Mã số nhân viên cần tra cứu (ví dụ: NV001, NV002)."
            }
          },
          "required": ["staffCode"]
        }
        """;
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        String staffCode = arguments.has("staffCode") ? arguments.get("staffCode").asText() : "";
        if (staffCode == null || staffCode.trim().isEmpty()) {
            return "{\"error\": \"Mã nhân viên không hợp lệ\"}";
        }

        // RBAC: HR_EMPLOYEE chỉ được phép xem profile của chính mình
        boolean isManagerOrAdmin = currentUser.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getName())
                        || "HR_MANAGER".equals(ur.getRole().getName()));

        if (!isManagerOrAdmin) {
            Staff myStaff = currentUser.getStaff();
            if (myStaff == null || !myStaff.getStaffCode().trim().equalsIgnoreCase(staffCode.trim())) {
                return "{\"status\": \"error\", \"message\": \"Bạn không có quyền xem thông tin của nhân viên này.\"}";
            }
        }

        List<Staff> staffs = staffRepository.findAll();
        Staff foundStaff = null;
        for (Staff s : staffs) {
            if (s.getStaffCode() != null && s.getStaffCode().trim().equalsIgnoreCase(staffCode.trim())) {
                foundStaff = s;
                break;
            }
        }

        if (foundStaff == null) {
            return "{\"status\": \"error\", \"message\": \"Không tìm thấy nhân viên với mã: " + staffCode + "\"}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"staffCode\":\"").append(escapeJson(foundStaff.getStaffCode())).append("\",");
        sb.append("\"displayName\":\"").append(escapeJson(foundStaff.getDisplayName())).append("\",");
        sb.append("\"email\":\"").append(escapeJson(foundStaff.getEmail())).append("\",");
        sb.append("\"phoneNumber\":\"").append(escapeJson(foundStaff.getPhoneNumber())).append("\",");
        sb.append("\"gender\":\"").append(foundStaff.getGender() != null ? escapeJson(foundStaff.getGender().name()) : "").append("\",");
        sb.append("\"workingStatus\":\"").append(foundStaff.getWorkingStatus() != null ? escapeJson(foundStaff.getWorkingStatus().name()) : "").append("\",");
        sb.append("\"birthDate\":\"").append(foundStaff.getBirthDate() != null ? foundStaff.getBirthDate().toString() : "—").append("\",");
        sb.append("\"startDate\":\"").append(foundStaff.getStartDate() != null ? foundStaff.getStartDate().toString() : "—").append("\",");
        sb.append("\"currentAddress\":\"").append(foundStaff.getCurrentAddress() != null ? escapeJson(foundStaff.getCurrentAddress()) : "—").append("\",");
        sb.append("\"department\":\"").append(foundStaff.getDepartment() != null ? escapeJson(foundStaff.getDepartment().getName()) : "—").append("\",");
        sb.append("\"position\":\"").append(foundStaff.getPosition() != null ? escapeJson(foundStaff.getPosition().getName()) : "—").append("\"");
        sb.append("}");
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
